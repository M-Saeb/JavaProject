import api.GPSValues;
import api.LocationAPI;
import car.*;
import exceptions.ChargingStationNotFoundException;
import exceptions.InvalidGPSLatitudeException;
import exceptions.InvalidGPSLongitudeException;
import exceptions.InvalidGPSValueException;
import stations.ChargingStation;
import byteStream.ByteStreamHandler;
import byteStream.ByteStreamInputCars;
import byteStream.ByteStreamInputChargingStations;

import java.util.concurrent.TimeUnit;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class Main {

	static {
		Path logsPath = Paths.get("logs");
		// Create logs dir
		try {
			Files.createDirectories(logsPath);
		} catch (final IOException e) {
			Logger.getAnonymousLogger().severe("Couldn't create logs folder.");
			Logger.getAnonymousLogger().severe(e.getMessage());
		}

		// Delete old logs
		try {
			for(File file: logsPath.toFile().listFiles()){ 
				if (!file.isDirectory()){
					file.delete();
				}
			}
		} catch (Exception e) {
			Logger.getAnonymousLogger().severe("Couldn't delete old logs.");
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

	}

	public static void main(String[] args) {
		// initiate logger
		Logger logger = Logger.getLogger("Main");
		ChargingStation[] sortedStations = new ChargingStation[4];
		
		ChargingStation[] stations = ByteStreamInputChargingStations.getChargingStations("objectLists/chargingStationsList.txt");
		logger.info("Created pool of charging stations.");

		// create pool of cars
		LocationAPI locationAPI = new LocationAPI(stations);
		
		Car[] cars = ByteStreamInputCars.getCars("objectLists/carsList.txt", locationAPI);
		
		logger.info("Created pool of cars.");

		// create pool of threads

		// send cars to charging stations
		int numCharged = 0;
		while (numCharged < cars.length){
			logger.fine("New round of checks begins.");
			for (Car car: cars){
				// if car is looking, find a suitable station and join its queue
				if (car.isLooking()){
					logger.fine(car.toString() + " is not charged yet.");
						try {
							ChargingStation suitableStation = car.getNearestFreeChargingStation();
							car.joinStationQueue(suitableStation);
							logger.fine(car.toString() + " joined " + suitableStation.toString());
						} catch (ChargingStationNotFoundException e) {
							logger.severe("No charging station found for " + car.toString() + ". Setting at as charged.");
							car.leaveMap();
							++numCharged;
						}
				
					// if car is alrady in queue, check if station is still suitable
					// If not, search for another station.
					} else if (car.isInQueue()){
					boolean isStationStillSuitable = car.checkCurrentStation();
					if (!isStationStillSuitable){
						logger.fine(car.toString() + " left " + car.getCurrentStation().toString() + " as it was not suitable anymore.");
						car.leaveStationQueue();
						try {
							ChargingStation suitableStation = car.getNearestFreeChargingStation();
							car.joinStationQueue(suitableStation);
							logger.fine(car.toString() + " joined " + suitableStation.toString());
						} catch (ChargingStationNotFoundException e) {
							logger.severe("No charging station found for " + car.toString() + ". Setting at as charged.");
							car.leaveMap();
							++numCharged;
						}
					}
				// if car is charging, see if it's fully charged.
				// If it is, leave the station. Yohoo, it's charged!
				} else if (car.isCharging()){
					if (car.getCurrentCapacity() == car.getTankCapacity()){
						car.leaveStation();
						logger.fine(car.toString() + " is charged and left the station.");
						++numCharged;
					}
				}
			}
		for (ChargingStation station: stations){
			station.sendCarsToFreeSlots();
			logger.info("Sent cars of " + station.toString() + " to slots.");
			station.chargeCarsInSlots();
			logger.info("Charged cars of " + station.toString());
		}

		try {
			TimeUnit.SECONDS.sleep(1);
		} catch (InterruptedException e) {
			e.printStackTrace();
			return;
		}

		}

		logger.info("Finished charging rounds.");
		int numReallyCharged = 0;
		for (Car car: cars){
			if (car.getCurrentCapacity() != car.getTankCapacity()){
				logger.warning(car.toString() + " didn't get charged.");
			} else {
				++numReallyCharged;
			}
		}
		logger.info(String.format("Managed to charge %d/%d cars.", numReallyCharged, cars.length));

	}
}
