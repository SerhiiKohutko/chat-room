package Exceptions;

public class BannedFromServerByIpException extends RuntimeException {
    public BannedFromServerByIpException() {
        super("Your ip was banned from the server by admin.");
    }
}
