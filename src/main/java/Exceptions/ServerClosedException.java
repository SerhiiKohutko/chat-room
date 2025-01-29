package Exceptions;

public class ServerClosedException extends RuntimeException {
    public ServerClosedException() {
        super("Server.Server closed.");
    }
}
