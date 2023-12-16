import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CountDownLatch;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import api.LocationAPI;
import byteStream.ByteStreamHandler;
import byteStream.ByteStreamInputCars;
import byteStream.ByteStreamInputChargingStations;
import car.Car;
import concurrency.*;
import exceptions.ChargingStationNotFoundException;
import stations.ChargingSlot;
import stations.ChargingStation;

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
		
		
		int total_slots = 0;
		for(int i = 0; i < stations.length; i++)
		{
			total_slots += (stations[i].getAvailableElectricSlots() + stations[i].getAvailableGasSlots());
		}
		
		CountDownLatch latch = new CountDownLatch(total_slots*stations.length);
		
		for(int i = 0; i < stations.length; i++)
		{
			ChargingStationRun chargingStationRun = new ChargingStationRun(stations[i]);
			Thread stationThread = new Thread(chargingStationRun);
			
			stationThread.start();
		}
		
		for(int i = 0; i < cars.length; i++)
		{
			ChargingStation nearChargingStation = null;
			try {
				nearChargingStation = cars[i].getNearestFreeChargingStation();
			} catch (ChargingStationNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			CarRun carRun = new CarRun(nearChargingStation, cars[i]);
			Thread carThread = new Thread(carRun);
			carThread.start();
		}
		logger.info("---------------------Run Ended.----------------");
	}
}
