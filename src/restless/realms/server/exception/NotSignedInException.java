package restless.realms.server.exception;

@SuppressWarnings("serial")
public class NotSignedInException extends RuntimeException {
    public NotSignedInException(String message) {
        super(message);
    }
}
