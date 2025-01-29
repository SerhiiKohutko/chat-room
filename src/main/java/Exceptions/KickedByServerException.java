package Exceptions;

public class KickedByServerException extends RuntimeException {
    public KickedByServerException() {
        super("You were kicked by admin");
    }
}
