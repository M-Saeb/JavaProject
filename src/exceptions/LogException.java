package exceptions;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.io.IOException;

public class LogException extends Exception{

    private static final Logger LOGGER = Logger.getLogger(LogException.class.getName());

    public LogException () {
        super();
        this.logMessage("");
    }

    public LogException (String message) {
        super(message);
        this.logMessage(message);
    }

    public LogException (Throwable cause) {
        super(cause);
        this.logMessage(cause.getMessage());
    }

    public LogException (String message, Throwable cause) {
        super (message, cause);
        this.logMessage(message);
    }

    private void logMessage(String message){
        try {
            FileHandler fileHandler = new FileHandler("app.log");
            SimpleFormatter formatter = new SimpleFormatter();
            fileHandler.setFormatter(formatter);

            // Adding the FileHandler to the LOGGER
            LOGGER.addHandler(fileHandler);

            // Logging messages
            LOGGER.severe("Error " + this.getClass().getName() + " occurred: " + message);

        } catch (IOException e) {
            LOGGER.severe("Error occurred while creating log file: " + e.getMessage());
        }
    }
}
