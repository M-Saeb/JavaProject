package exceptions;

public class InvalidGPSLongitudeException extends LogException {
    public InvalidGPSLongitudeException () {

    }

    public InvalidGPSLongitudeException (String message) {
        super (message);
    }

    public InvalidGPSLongitudeException (Throwable cause) {
        super (cause);
    }

    public InvalidGPSLongitudeException (String message, Throwable cause) {
        super (message, cause);
    }
}
