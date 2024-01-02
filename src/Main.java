import java.util.Random;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Formatter;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import api.LocationAPI;
import byteStream.ByteStreamHandler;
import byteStream.ByteStreamInputCars;
import byteStream.ByteStreamInputChargingStations;
import car.Car;
import stations.ChargingStation;
import utils.Utils;

/* Folder creation for logs */
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Main {

	static {
		LocalDate currentDate = LocalDate.now();
		DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		String dateFormatted = currentDate.format(dateFormat);
		
		
		Path currentPath = Paths.get("logs");
		String folderString = currentPath.toAbsolutePath().toString();
		File folder = new File(folderString + "/" + dateFormatted);
		System.out.println("Current absolute path is: " + folder);
		if(!folder.exists())
		{
			folder.mkdir();
			System.out.println("Folder created");
		}
		else
		{
			System.out.println("Folder already exists");
		}
		
		Path logsPath = Paths.get("logs" + "/" + dateFormatted);
		// Create logs dir
		try {
			Files.createDirectories(logsPath);
		} catch (final IOException e) {
			Logger.getAnonymousLogger().severe("Couldn't create logs folder.");
			Logger.getAnonymousLogger().severe(e.getMessage());
		}

		// Import logging configurations
		try {
			final InputStream inputStream = Main.class.getResourceAsStream("/logging.properties");
			LogManager.getLogManager().readConfiguration(inputStream);
		} catch (final IOException e) {
			Logger.getAnonymousLogger().severe("Could not load default logging.properties file");
			Logger.getAnonymousLogger().severe(e.getMessage());
		}
		Logger.getLogger("").addHandler(new ByteStreamHandler("logs/byteStreamLog.log"));

		/*
		 * Add a FileHandler and associate it with a Filter for every type of
		 * log file that we need.
		 */

		// get today's date for log filename
		String todaysDate = Utils.getTodaysDate();

		Formatter ourFormatter = Utils.getGlobalFormatter();

		/*
		 * Add logging file handler for solar and power grid energy sources,
		 * as well as for the whole system.
		 */
		try {		
			Logger.getLogger("").addHandler(
				Utils.generateFileHandler(
					String.format("%s/%s - %s.log", logsPath.toString(), todaysDate, "system"),
					ourFormatter
					)
			);
			Logger.getLogger("Solar").addHandler(
				Utils.generateFileHandler(
					String.format("%s/%s - %s.log", logsPath.toString(), todaysDate, "solar"),
					ourFormatter
					)
			);
			Logger.getLogger("PowerGrid").addHandler(
				Utils.generateFileHandler(
					String.format("%s/%s - %s.log", logsPath.toString(), todaysDate, "power grid"),
					ourFormatter
					)
			);
		} catch (SecurityException | IOException e) {
			e.printStackTrace();
		}

	}

	public static void main(String[] args) {
		// initiate logger
		Logger logger = Logger.getLogger("system");
		
		// Create pool of stations
		ChargingStation[] stations = ByteStreamInputChargingStations.getChargingStations("objectLists/chargingStationsList.txt");
		logger.info("---------------------------------------");
		logger.info("Created pool of charging stations.");
		logger.info("---------------------------------------");
		LocationAPI locationAPI = new LocationAPI(stations);
		
		// Create pool of cars	
		Car[] cars = ByteStreamInputCars.getCars("objectLists/carsList.txt", locationAPI);
		logger.info("---------------------------------------");
		logger.info("Created pool of cars.");
		logger.info("---------------------------------------");

		// create pool of threads
		logger.info("---------------------------------------");
		logger.info("Starting threads.");
		logger.info("---------------------------------------");

		for (ChargingStation station: stations){
			Thread thread = new Thread(station);
			thread.start();
		}
		
		for(int i = 0; i < cars.length; i++)
		{
			Random random = new Random();
        	int delayTime = random.nextInt(3) + 1;
			try{
				Thread.sleep(delayTime * 1000);
			} catch (Exception e){
				e.printStackTrace();
			}
			Car car = cars[i];
			logger.info(String.format("--- Deploying next car: %s ---", car.toString()));
			Thread carThread = new Thread(car);
			carThread.start();
		}
		logger.info("-------All cars are deployed.-------");
	}
}