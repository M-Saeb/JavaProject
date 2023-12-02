package exceptions;

public class ChargingStationNotFoundException extends LogException {
    public ChargingStationNotFoundException () {

    }

    public ChargingStationNotFoundException (String message) {
        super (message);
    }

    public ChargingStationNotFoundException (Throwable cause) {
        super (cause);
    }

    public ChargingStationNotFoundException (String message, Throwable cause) {
        super (message, cause);
    }
}