package stations;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;


import annotations.APIMethod;
import annotations.Mutable;
import annotations.Readonly;
import api.GPSValues;
import api.LocationAPI;
import car.Car;
import car.ElectricCar;
import car.GasCar;
import exceptions.ChargingSlotFullException;
import exceptions.InvalidGPSLatitudeException;
import exceptions.InvalidGPSLongitudeException;
import exceptions.InvalidGPSValueException;

import java.util.concurrent.locks.*;

public class ChargingStation extends Thread {	
	private Logger logger;
	private Lock stationLock = new ReentrantLock();
	/* Charging Station Info */
	private int chargingStationID;
	
	private int numberOfAvailableSlots;
	private GPSValues gpsValues;
	private float gasOutputPerSecond;
	private float electricityOutputPerSecond;
	private float LevelOfElectricityStorage;
	private float LevelOfGasStorage;
	
	
	private ArrayList<Car> waitingQueue = new ArrayList<Car>();
	private ChargingSlot electricSlots;
	private ChargingSlot gasSlots;
	private float waitTime = 0;

	@APIMethod
	public ChargingStation(
			int chargingStationID,
			GPSValues gpsValues,
			int numGasSlots,
			int numElectricSlots,
			float gasOutputPerSecond,
			float electricityOutputPerSecond,
			float LevelOfElectricityStorage,
			float LevelOfGasStorage)
			throws InvalidGPSLatitudeException, InvalidGPSLongitudeException, InvalidGPSValueException {
		
		{
			this.chargingStationID = chargingStationID;
			this.logger = Logger.getLogger(this.toString());
			try {
				LocationAPI.checkGPSValues(gpsValues);
			} catch (InvalidGPSLatitudeException | InvalidGPSLongitudeException e) {
				this.logger.severe(e.getStackTrace().toString());
				throw e;
			} catch (Exception e) {
				this.logger.severe(e.getStackTrace().toString());
				throw e;
			}
			this.gpsValues = gpsValues;
			
			if ((numGasSlots == 0) && (numElectricSlots == 0)) {
				throw new IllegalArgumentException("Station can't have 0 slots");
			} else if (numGasSlots < 0) {
				throw new IllegalArgumentException("Station can't have fewer than 0 gas slots.");
			} else if (numElectricSlots < 0) {
				throw new IllegalArgumentException("Station can't have fewer than 0 electirc slots.");
			}
		}
		
		{
			if (numGasSlots > 0) {
				this.electricSlots = new ChargingSlot(numElectricSlots);
			}
			if (numElectricSlots > 0) {
				this.gasSlots = new ChargingSlot(numGasSlots);
			}

			if (gasOutputPerSecond < 0 || electricityOutputPerSecond < 0) {
				throw new IllegalArgumentException("Charging station output can't be fewer than 0.");
			}
			if (numGasSlots == 0 && gasOutputPerSecond > 0) {
				throw new IllegalArgumentException("Station can't have 0 gas slots and still have gas output potential.");
			} else if (numElectricSlots == 0 && electricityOutputPerSecond > 0) {
				throw new IllegalArgumentException(
						"Station can't have 0 electricity slots and still have electricity output potential.");
			}
			this.gasOutputPerSecond = gasOutputPerSecond;
			this.electricityOutputPerSecond = electricityOutputPerSecond;

			if (LevelOfElectricityStorage < 0 || LevelOfGasStorage < 0) {
				throw new IllegalArgumentException("Charging station storage can't be fewer than 0.");
			} else if (LevelOfElectricityStorage == 0 && LevelOfGasStorage == 0){
				throw new IllegalArgumentException("Station can't have 0 storage of any kind");
			}
			if (numGasSlots == 0 && LevelOfGasStorage > 0) {
				throw new IllegalArgumentException("Station can't have 0 gas slots and still have gas output potential.");
			} else if (numElectricSlots == 0 && LevelOfElectricityStorage > 0) {
				throw new IllegalArgumentException(
						"Station can't have 0 electricity slots and still have electricity output potential.");
			}
			this.LevelOfElectricityStorage = LevelOfElectricityStorage;
			this.LevelOfGasStorage = LevelOfGasStorage;
		}

		this.logger.fine("Initiated " + this.toString());
	}

	@Readonly
	public String toString() {
		return String.format("Charging Station %d", this.chargingStationID);
	}

	@Readonly
	public float getGPSLatitude() throws InvalidGPSValueException {
		if (this.gpsValues.getLatitude() == 0) {
			try {
				throw new InvalidGPSLatitudeException("Invalid Latitud value...");
			} catch (Exception e) {
				System.out.println("Invalid Latitud value...");
				e.printStackTrace();
			}
		} else {
			/*
			 * Do nothing
			 */
		}
		return this.gpsValues.getLatitude();
	}

	@Readonly
	public float getGPSLongitude() throws InvalidGPSValueException {
		if (this.gpsValues.getLongitude() == 0) {
			try {
				throw new InvalidGPSLongitudeException("Invalid Latitud value...");
			} catch (Exception e) {
				this.logger.severe("Invalid Latitud value...");
				this.logger.severe(e.getStackTrace().toString());
			}
		} else {
			/*
			 * Do nothing
			 */
		}
		return this.gpsValues.getLongitude();
	}


	@Readonly
	public int getAvailableGasSlots() {
		return gasSlots.getTotalSlots();
	}

	@Readonly
	public int getAvailableElectricSlots() {
		return electricSlots.getTotalSlots();
	}


	@Mutable
	public void setChargingStationID(int chargingStationID) {
		this.chargingStationID = chargingStationID;
	}

	@Readonly
	public int getChargingStationID() {
		return chargingStationID;
	}

	@Readonly
	public float getGasOutputPerSecond() {
		return gasOutputPerSecond;
	}

	@Readonly
	public float getElectricityOutputPerSecond() {
		return electricityOutputPerSecond;
	}

	@Readonly
	public float getLevelOfElectricityStorage() {
		return LevelOfElectricityStorage;
	}

	@Mutable
	public void setLevelOfElectricityStorage(float levelOfElectricityStorage) {
		LevelOfElectricityStorage = levelOfElectricityStorage;
	}

	@Readonly
	public float getLevelOfGasStorage() {
		return LevelOfGasStorage;
	}

	@Mutable
	public void setLevelOfGasStorage(float levelOfGasStorage) {
		LevelOfGasStorage = levelOfGasStorage;
	}

	@Mutable
	public void addCarToWaitingQueue(Car car) {
		this.logger.finer(String.format("Adding %s to waitingQueue.", car.toString()));
		// Adding it and returning, if the waitingQueue is empty
		if (waitingQueue.isEmpty()) {
			waitingQueue.add(car);
			car.setEnterStationTime(System.currentTimeMillis());
			this.logger.finer("waitingQueue was empty. Added car.");
			return;
		}

		// If car is prioritized, add it after the last prioritized car
		if (car.isPriority()) {
			this.logger.finer("Car is priority. Adding it to the top of the waitingQueue.");

			for (int i = 0; i < waitingQueue.size(); i++) {
				if (!waitingQueue.get(i).isPriority()) {
					waitingQueue.add(i, car);
					car.setEnterStationTime(System.currentTimeMillis());
					this.logger.finer("Added priority car at position " + i);
					return;
				}
			}
		}

		// Otherwise add normal
		waitingQueue.add(car);
		this.logger.fine(String.format("Added %s to waitingQueue.", car.toString()));
	}

	/**
	 * Remove car from station waitingQueue.
	 */
	@Mutable
	public void leaveStationwaitingQueue(Car car) {
		waitingQueue.remove(car);
		this.logger.fine(String.format("Removed %s from waitingQueue.", car));
	}
	
	public void chargeCar(Car varCar)
	{
		boolean aquiredSlot = false;
		synchronized (varCar) {
			if(varCar instanceof ElectricCar)
			{
				aquiredSlot = electricSlots.tryAquireSlot(varCar);
				if(aquiredSlot)
				{
					int energy2charge = (int) (varCar.getTankCapacity() - varCar.getCurrentCapacity());
					int stationResources = (int) getLevelOfElectricityStorage();
					if(energy2charge < stationResources)
					{
						stationResources -= energy2charge;
						setLevelOfElectricityStorage(stationResources);
						
						System.out.println("-------------Charging Station------------- \n"
								+ "Car... " + varCar.getCarNumber() + " \n"
								+ "Consumed... " + energy2charge + " units" + " \n"
								+ "@Station... " + getChargingStationID() + " \n"
								+ "Remaining... " + stationResources);
						try {
	                        int randomDelay = (int) (Math.random() * 5000);
	                        Thread.sleep(randomDelay);
	                    } catch (InterruptedException e) {
	                        Thread.currentThread().interrupt();
	                    }
						
						varCar.notify();
					}
					else {
						/*
						 * TODO: Add logic for pop because there are not enough resources
						 */
						if(!waitingQueue.isEmpty())
						{
							waitingQueue.remove(0);
						}
					}
				}
				else {
					/*
					 * TODO: Add logic for pop because too much time has passed
					 */
					waitingQueue.add(varCar);
					varCar.setEnterStationTime(System.currentTimeMillis() + varCar.getEnterStationTime());
				}
			}
			else //GasCar
			{
				aquiredSlot = gasSlots.tryAquireSlot(varCar);
				if(aquiredSlot)
				{
					int gas2charge = (int) (varCar.getTankCapacity() - varCar.getCurrentCapacity());
					int stationResources = (int) getLevelOfGasStorage();
					if(gas2charge < stationResources)
					{
						stationResources -= gas2charge;
						setLevelOfGasStorage(stationResources);
						
						System.out.println("-------------Charging Station------------- \n"
								+ "Car... " + varCar.getCarNumber() + " \n"
								+ "Consumed... " + gas2charge + " units" + " \n"
								+ "@Station... " + getChargingStationID() + " \n"
								+ "Remaining... " + stationResources);
						
						try {
	                        int randomDelay = (int) (Math.random() * 5000);
	                        Thread.sleep(randomDelay);
	                    } catch (InterruptedException e) {
	                        Thread.currentThread().interrupt();
	                    }
						
						gasSlots.releaseAquiredSlot(varCar);
						varCar.notify();
					}
					else {
						/*
						 * TODO: Add logic for pop because there are not enough resources
						 */
						if(!waitingQueue.isEmpty())
						{
							waitingQueue.remove(0);
						}
					}
				}
				else {
					/*
					 * TODO: Add logic for pop because too much time has passed
					 */
					waitingQueue.add(varCar);
					varCar.setEnterStationTime(System.currentTimeMillis() + varCar.getEnterStationTime());
				}
			}
		}
	}
	
	public void releaseQueue()
	{
		synchronized (waitingQueue) {
			for(Car tempCar : waitingQueue)
			{
				long currentWaitingTime = System.currentTimeMillis() - tempCar.getEnterStationTime();
				if(currentWaitingTime <= 15*1000)
				{
					chargeCar(tempCar);
				}
				else {
					/*
					 * Car exit the queue because it waited too much time
					 */
					System.out.println("----------------------------------\n"
							+ "Car... " + tempCar.getCarNumber() + " \n" +
							"@Station... " + getChargingStationID() + " \n" +
							"EXIT the queue for waiting too much time \n");
					waitingQueue.remove(0);
				}
			}
		}
	}
	
	public boolean isCarQueued(Car varCar)
	{
		synchronized (waitingQueue) {
			return waitingQueue.contains(varCar);
		}
	}
	
	public ChargingSlot getAvailableSlots(Car varCar)
	{
		if(varCar instanceof ElectricCar)
		{
			return electricSlots;
		}
		else
		{
			return gasSlots;
		}
	}
}
