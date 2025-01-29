package Server.Listeners;

import Server.Server;

import java.util.List;
import java.util.Map;

public interface ClientListChangeListener {

    void onClientListChange(Map<String, Server.ClientHandler> clients);

    void onBannedNamesListChange(List<String> bannedNames);
    void onBannedIpsListChange(List<String> bannedIps);
}
