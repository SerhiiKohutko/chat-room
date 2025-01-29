package Server;

import Server.Listeners.ClientListChangeListener;
import Server.Listeners.LogListener;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class ServerStarter implements LogListener, ClientListChangeListener {
    private Server server;
    private final int port;
    private final ServerController serverController;

    public ServerStarter(int port, ServerController serverController) {
        this.port = port;
        this.serverController = serverController;
    }

    public void startServer() throws IOException {
        server = new Server(port, this, this);
        new Thread(server::start).start();
    }

    public void stopServer() {
        server.stop();
    }

    public void sendServerMessage(String message) {
        server.sendServerMessageToAll(message);
    }
    public void kickClientFromServer(String username) {
        server.kickClient(username);
    }
    public void banClientFromServer(String clientInfo, boolean byIp) {
        if (byIp) {
            server.banClientByIp(clientInfo);
        }else{
            server.banClientByName(clientInfo);
        }
    }

    public void unbanClientFromServer(String clientInfo, boolean byIp) {
        server.unbanClient(clientInfo, byIp);
    }

    @Override
    public void logAccept(String message) {
        serverController.writeMessage(message);
    }

    @Override
    public void onClientListChange(Map<String, Server.ClientHandler> clients) {
        serverController.updateClientsList(clients);
    }

    @Override
    public void onBannedNamesListChange(List<String> bannedNames) {
        serverController.updateBannedList(bannedNames, false);
    }

    @Override
    public void onBannedIpsListChange(List<String> bannedIps) {
        serverController.updateBannedList(bannedIps, true);
    }
}
