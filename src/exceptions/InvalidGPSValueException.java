package exceptions;

public class InvalidGPSValueException extends LogException {
    public InvalidGPSValueException () {

    }

    public InvalidGPSValueException (String message) {
        super (message);
    }

    public InvalidGPSValueException (Throwable cause) {
        super (cause);
    }

    public InvalidGPSValueException (String message, Throwable cause) {
        super (message, cause);
    }
}
