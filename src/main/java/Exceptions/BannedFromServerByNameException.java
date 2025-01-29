package Exceptions;

public class BannedFromServerByNameException extends RuntimeException {
    public BannedFromServerByNameException() {
        super("You were banned from server by admin.");
    }
}
