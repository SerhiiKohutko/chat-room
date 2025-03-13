package Client;

import Exceptions.BannedFromServerByIpException;
import Exceptions.BannedFromServerByNameException;
import Exceptions.KickedByServerException;
import Exceptions.ServerClosedException;
import javafx.application.Platform;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;


public class Client {
    private Socket connection;
    private BufferedReader br;
    private PrintWriter pw;
    private ClientController guiController;

    public void start(String host, int port, String username, ClientController guiController) throws Exception {
        this.guiController = guiController;

        try {
            connection = new Socket(host, port);
            br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            pw = new PrintWriter(connection.getOutputStream(), true);
            pw.println(username);

            String response = br.readLine();
            checkServerResponse(response, host, port);

            getReadThread().start();
        } catch (Exception e) {
            close();
            throw new Exception(e.getMessage());
        }
    }

    private void checkServerResponse(String response, String host, int port) throws Exception {
        switch(response) {
            case "USERNAME_NULL" -> throw new Exception("USERNAME_NULL");
            case "USERNAME_TAKEN" -> throw new Exception("USERNAME_TAKEN");
            case "USERNAME_RESERVED" -> throw new Exception("USERNAME_RESERVED");
            case "USERNAME_BANNED" -> throw new Exception("USERNAME_BANNED");
            case "USER_IP_IS_BANNED" -> throw new Exception("USER_IP_IS_BANNED");
            default -> System.out.println("Success\nConnected to " + host + ":" + port);
        }
    }

    private Thread getReadThread() {
        Thread readThread = new Thread(() -> {
            try {
                String serverMessage;
                while ((serverMessage = br.readLine()) != null) {
                    switch (serverMessage) {
                        case "_SERVER_SERVICE_CLOSED_" -> throw new ServerClosedException();
                        case "_SERVER_SERVICE_KICKED_" -> throw new KickedByServerException();
                        case "_SERVER_SERVICE_BAN_BY_NAME", "_SERVER_SERVICE_BAN_BY_IP" -> throw new BannedFromServerByIpException();
                    }

                    readMessage(serverMessage);
                }
            } catch (IOException e) {
                System.out.println("Connection to server lost: " + e.getMessage());
            } catch (ServerClosedException e) {
                Platform.runLater(() -> {
                    ClientDialogProvider.showErrorDialog("Server.Server closed", e.getMessage());
                    Platform.exit();
                });
            } catch (KickedByServerException e) {
                Platform.runLater(() -> {
                    ClientDialogProvider.showErrorDialog("Kicked by server", e.getMessage());
                    Platform.exit();
                });
            } catch (BannedFromServerByNameException | BannedFromServerByIpException e) {
                Platform.runLater(() -> {
                    ClientDialogProvider.showErrorDialog("Banned from server", e.getMessage());
                    Platform.exit();
                });
            }
        });
        readThread.setDaemon(true);
        return readThread;
    }


    public void sendMessage(String message){
        pw.println(message);
    }

    public void readMessage(String message){
        if(message.equals("__SERVICE__PONG")){
            System.out.println("Received __SERVICE__PONG from the server.");
        } else {
            guiController.writeMessage(message);
        }
    }


    private void close() throws IOException {
        if (br != null) {
            br.close();
        }
        if (pw != null) {
            pw.close();
        }
        if (connection != null) {
            connection.close();
        }
    }

}

