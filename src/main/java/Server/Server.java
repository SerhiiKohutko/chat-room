package Server;

import DBTesting.BannedIp;
import DBTesting.BannedName;
import DBTesting.HibernateUtility;
import Server.Enums.ServerReservedNames;
import Server.Enums.ServerSTATUS;
import Server.Listeners.ClientListChangeListener;
import Server.Listeners.LogListener;
import org.hibernate.HibernateError;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;


public final class Server {

    @Testing
    private final ConcurrentHashMap<String, ClientHandler> clientsMap = new ConcurrentHashMap<>();
    @Testing
    private final CopyOnWriteArrayList<String> bannedNames = new CopyOnWriteArrayList<>();
    @Testing
    private final CopyOnWriteArrayList<String> bannedIps = new CopyOnWriteArrayList<>();


    private ServerSTATUS status;
    private final LogListener logListener;
    private final ClientListChangeListener clientListChangeListener;
    private final ServerSocket serverSocket;
    private volatile boolean running = true;

    public class ClientHandler implements Runnable {
        private final BufferedReader in;
        private final PrintWriter out;
        private final String username;
        private final Socket connection;
        private volatile boolean closed = false;

        public ClientHandler(Socket socket, String username) throws Exception {

            out = new PrintWriter(socket.getOutputStream(), true);

            userConnectionValidation(out, username, socket);

            this.username = username;
            connection = socket;
            connection.setSoTimeout(100);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            addClient(username, this);
        }

        @Override
        public void run() {
            try {
                while (!closed) {
                    String message;
                    try {
                        if ((message = in.readLine()) == null) {
                            break;
                        }

                        log("Received from " + username + ": " + message);
                        String timeStamp = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date());
                        writeMessageToAll("[" + timeStamp + "] " + username + " : " + message, this);
                    }catch (SocketTimeoutException ignored) {
                        System.out.println("Socket timed out");
                    }catch (IOException e) {
                        break;
                    }
                }
            } finally {
                System.out.println("Closing connection");
                removeClient(this);
            }
        }

        public Socket getConnection() {
            return connection;
        }
        public PrintWriter getWriter(){
            return out;
        }
        public void removeClient(ClientHandler currClient) {
                clientsMap.remove(currClient.username);
                clientListChangeListener.onClientListChange(clientsMap);

            if (status.equals(ServerSTATUS.CLOSED)) {
                out.println("_SERVER_SERVICE_CLOSED_");
            }

            try{
                if (in != null) {
                    in.close();
                }
                if (out != null) {
                    out.close();
                }
                writeMessageToAll(username + " has left the room", currClient);
                clientRemovedServerSide(connection);
            } catch (IOException e) {
                log("Error while closing connection" + e.getMessage());
            }

        }
        private void addClient(String username, ClientHandler currClient) {
                clientsMap.put(username, currClient);
                clientListChangeListener.onClientListChange(clientsMap);


            writeMessageToAll(username + " has joined the room", currClient);
            clientConnectedServerSide(connection);
            clientConnectedClientSide(out);
        }
        public void shutdown(){
            closed = true;
        }
    }

    public void kickClient(String username) {
        ClientHandler currClient = clientsMap.get(username);
        currClient.getWriter().println("_SERVER_SERVICE_KICKED_");
        log("Client " + username + " kicked from the server");
        writeMessageToAll(username + " kicked from the server", currClient);
        currClient.shutdown();
    }

    public void banClientByName(String username) {
        ClientHandler currClient = clientsMap.get(username);
        bannedNames.add(username);
        addBannedNameToDb(username);
        clientListChangeListener.onBannedNamesListChange(bannedNames);
        currClient.getWriter().println("_SERVER_SERVICE_BAN_BY_NAME");
        log("Client " + username + " banned from the server");
        writeMessageToAll(username + " banned from the server", currClient);
        currClient.shutdown();
    }

    public void banClientByIp(String username) {
        ClientHandler currClient = clientsMap.get(username);
        bannedIps.add(currClient.getConnection().getLocalAddress().toString());
        addBannedIpToDb(username);
        clientListChangeListener.onBannedIpsListChange(bannedIps);
        currClient.getWriter().println("_SERVER_SERVICE_BAN_BY_IP");
        log("Client " + username + " banned from the server");
        writeMessageToAll(username + " banned from the server", currClient);
        currClient.shutdown();
    }

    public void unbanClient(String clientInfo, boolean unbanIp) {
        if (unbanIp) {
            bannedIps.remove(clientInfo);
            clientListChangeListener.onBannedIpsListChange(bannedIps);
        } else {
            bannedNames.remove(clientInfo);
            removeBannedNameFromDb(clientInfo);
            clientListChangeListener.onBannedNamesListChange(bannedNames);
        }
    }

    public Server(int port, LogListener logListener, ClientListChangeListener clientListChangeListener) throws IOException {
        serverSocket = new ServerSocket(port);
        this.logListener = logListener;
        this.clientListChangeListener = clientListChangeListener;
        initializeLists();
        clientListChangeListener.onBannedNamesListChange(bannedNames);
        clientListChangeListener.onBannedIpsListChange(bannedIps);
    }

    //DB SECTION
    private void initializeLists(){
        Transaction tx = null;
        try(Session session = HibernateUtility.getSessionFactory().openSession()){
            tx = session.beginTransaction();

            List<BannedName> resNames = new ArrayList<BannedName>();
            List<BannedIp> resIps = new ArrayList<>();
            resIps.addAll(session.createQuery("from BannedIp").list());
            resNames.addAll(session.createQuery("from BannedName").list());

            bannedNames.addAll(resNames.stream()
                    .map(BannedName::toString)
                    .toList());

            bannedIps.addAll(resIps.stream()
                    .map(BannedIp::toString)
                    .toList());

            tx.commit();

        }catch (HibernateError e){
            if (tx != null) {
                tx.rollback();
            }
        }
    }

    private void addBannedNameToDb(String name){
        Transaction tx = null;
        try(Session session = HibernateUtility.getSessionFactory().openSession()){
            tx = session.beginTransaction();

            BannedName newBannedName = new BannedName();
            newBannedName.setBannedName(name);
            session.save(newBannedName);
            tx.commit();
        } catch (HibernateException e){
            if (tx != null){
                tx.rollback();
            }
        }
    }

    private void removeBannedNameFromDb(String name){
        Transaction tx = null;
        try(Session session = HibernateUtility.getSessionFactory().openSession()){
            tx = session.beginTransaction();

            BannedName newBannedName = (BannedName) session.createQuery("FROM BannedName WHERE bannedName = :name")
                    .setParameter("name", name)
                    .getSingleResult();
            session.remove(newBannedName);
            tx.commit();
        } catch (HibernateException e){
            if (tx != null){
                tx.rollback();
            }
        }
    }


    private void addBannedIpToDb(String ip){
        Transaction tx = null;
        try(Session session = HibernateUtility.getSessionFactory().openSession()){
            tx = session.beginTransaction();

            BannedIp newBannedIp = new BannedIp();
            newBannedIp.setIp(ip);
            session.save(newBannedIp);
            tx.commit();
        } catch (HibernateException e){
            if (tx != null){
                tx.rollback();
            }
        }
    }
//////

    public void start(){
        log("Server.Server started");
        status = ServerSTATUS.OPEN;

        while(running){
            try {
                Socket newClientSocket = serverSocket.accept();
                String clientUsername;
                BufferedReader in = new BufferedReader(new InputStreamReader(newClientSocket.getInputStream()));
                clientUsername = in.readLine();
                new Thread(new ClientHandler(newClientSocket, clientUsername)).start();
            }catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }
    public void stop(){
        running = false;
        status = ServerSTATUS.CLOSED;
        try {
            closeAllConnections();
            System.out.println("All connections closed.");
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            System.out.println("Server.Server stopped.");
        } catch (IOException e) {
            System.out.println("Error while stopping server: " + e.getMessage());
        }

    }

    public void sendServerMessageToAll(String message) {
        String timeStamp = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date());
        message = "[" + timeStamp + "] " + "ADMIN " + " : " + message;
        log(message);
        for (String key : clientsMap.keySet()){
            clientsMap.get(key).getWriter().println(message);
        }
    }

    private void closeAllConnections() {

        for (String key : clientsMap.keySet()) {
            ClientHandler currClient = clientsMap.get(key);
            currClient.removeClient(currClient);
            currClient.shutdown();
            log("Closed connection for client: " + currClient.getConnection().getInetAddress());
        }

    }
    private void writeMessageToAll(String message, ClientHandler currClient) {

        for (String key : clientsMap.keySet()){
            if (clientsMap.get(key) == currClient){
                continue;
            }
            clientsMap.get(key).getWriter().println(message);
        }

    }

    private void clientConnectedClientSide(PrintWriter pw) {
        pw.println("OK");
    }
    private void clientConnectedServerSide(Socket socket) {
        String message = "New client connected: " + socket.getInetAddress();
        log(message);
    }
    private void clientRemovedServerSide(Socket socket) throws IOException {
        String message = "Client disconnected: " + socket.getInetAddress();
        log(message);
        socket.close();
    }

    private void log(String message) {
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        System.out.println("[" + timestamp + "] " + message);
        logListener.logAccept("[" + timestamp + "] " + message);
    }

    private void userConnectionValidation(PrintWriter out, String username, Socket connection) throws Exception {
        if (username == null) {
            out.println("USERNAME_NULL");
            out.close();
            throw new Exception("Username cannot be null");
        }

        if (isNameServerReserved(username)) {
            out.println("USERNAME_RESERVED");
            out.close();
            throw new Exception("Username cannot be used because it's reserved by server-side");
        }

        if (isNameTaken(username)) {
            out.println("USERNAME_TAKEN");
            out.close();
            throw new Exception("Username is already taken");
        }

        if (isUsernameBanned(username)) {
            out.println("USERNAME_BANNED");
            out.close();
            throw new Exception("Username is already banned");
        }

        if (isUserIpInBannedList(connection)){
            out.println("USER_IP_IS_BANNED");
            out.close();
            throw new Exception("Username is already banned");
        }
    }

    private boolean isNameServerReserved(String username) {
        return Arrays.stream(ServerReservedNames.values()).map(Enum::toString).anyMatch(s -> s.equals(username));
    }
    public boolean isNameTaken(String username) {
        return clientsMap.containsKey(username);
    }
    private boolean isUsernameBanned(String username) {
        return bannedNames.contains(username);
    }
    private boolean isUserIpInBannedList(Socket socket) {
        for (String key : bannedIps) {
            if (key.equals(socket.getInetAddress().toString())) {
                return true;
            }
        }
        return false;
    }
}


