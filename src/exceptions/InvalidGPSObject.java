package exceptions;

public class InvalidGPSObject extends LogException 
{
    public InvalidGPSObject () {

    }

    public InvalidGPSObject (String message) {
        super (message);
    }

    public InvalidGPSObject (Throwable cause) {
        super (cause);
    }

    public InvalidGPSObject (String message, Throwable cause) {
        super (message, cause);
    }
}
