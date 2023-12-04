import api.GPSValues;
import api.LocationAPI;
import car.*;
import exceptions.ChargingStationNotFoundException;
import exceptions.InvalidGPSLatitudeException;
import exceptions.InvalidGPSLongitudeException;
import exceptions.InvalidGPSValueException;
import stations.ChargingStation;
import byteStream.ByteStreamHandler;

import java.util.concurrent.TimeUnit;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class Main {

	static {
		try {
			Files.createDirectories(Paths.get("logs"));
		} catch (final IOException e) {
			Logger.getAnonymousLogger().severe("Couldn't great logs folder.");
			Logger.getAnonymousLogger().severe(e.getMessage());
		}
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
		ChargingStation[] stations = new ChargingStation[4];
		ChargingStation[] sortedStations = new ChargingStation[4];

		// create pool of charging stations
		try {
			stations[0] = new ChargingStation(
				1,
				new GPSValues(44, 96),
				2,
				2,
				5,
				5
			);
			stations[1] = new ChargingStation(
				2,
				new GPSValues(10, 50),
				2,
				0,
				5,
				0
			);
			stations[2] = new ChargingStation(
				3,
				new GPSValues(-50, 150),
				1,
				1,
				10,
				5
			);
			stations[3] = new ChargingStation(
				4,
				new GPSValues(-70, 44),
				4,
				0,
				10,
				0
			);
		} catch (InvalidGPSLatitudeException | InvalidGPSLongitudeException | InvalidGPSValueException e) {
			logger.severe(e.getStackTrace().toString());
			e.printStackTrace();
			return;
		}
		logger.info("Created pool of charging stations.");

		// create pool of cars
		LocationAPI locationAPI = new LocationAPI(stations);
		Car[] cars = {
				new GasCar(
					"Ford Mustang",
					(float) 20.0,
					(float) 60.9,
					(float) 120.0,
					locationAPI,
					new GPSValues(40, 20)
				),
				new ElectricCar(
					"Toyota Prius",
					(float) 5.0,
					(float) 13.0,
					(float) 30.0,
					locationAPI,
					new GPSValues(-50, 50)
				),
					
				new GasCar(
					"Peugeot 206",
					(float) 12.0,
					(float) 49.0,
					(float) 30.0,
					locationAPI,
					new GPSValues(24, 140)
				),
				new ElectricCar(
					"BW i5",
					(float) 70.0,
					(float) 83.9,
					(float) 70.0,
					locationAPI,
					new GPSValues(80, 95)
				),
				new GasCar(
					"Audi A3",
					(float) 10,
					(float) 48.0,
					(float) 90.0,
					locationAPI,
					new GPSValues(40, 10)
				)
		};
		logger.info("Created pool of cars.");

		// create pool of threads

		// send cars to charging stations
		int numCharged = 0;
		while (numCharged < cars.length){
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
