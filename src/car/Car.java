package car;

import api.GPSValues;
import api.LocationAPI;
import annotations.Readonly;

import java.util.logging.Level;
import java.util.logging.Logger;

import annotations.APIMethod;
import annotations.Mutable;
import exceptions.ChargingStationNotFoundException;
import exceptions.InvalidGPSValueException;
import stations.ChargingStation;

public abstract class Car{

	private final double FEASIBLE_WAITING_TIME = 900.0;

	protected String carNumber;
	private float currentCapacity;
	private float tankCapacity;
	private float waitDuration; // the maximum accepted waiting duration for the car
	private long enterStationTime; // time when the car entered the queue and is set to wait.
	protected LocationAPI api;
	protected GPSValues currentGPS;
	private ChargingStation currentChargingStation;
	private CarState currState;
	private boolean priorityFlag;
	private Logger logger;
	private int semaphoreID;

	public Car(String carNumber, float currentCapacity, float tankCapacity, float waitDuration, LocationAPI api,
			GPSValues currentGPS) {
		this.carNumber = carNumber;
		this.currentCapacity = currentCapacity;
		this.tankCapacity = tankCapacity;
		this.waitDuration = waitDuration;
		this.api = api;
		this.currentGPS = currentGPS;
		if (currentCapacity < tankCapacity) {
			this.currState = CarState.looking;
		} else {
			this.currState = CarState.charged;
		}
		this.priorityFlag = false;
		this.logger = Logger.getLogger(this.toString());
	}

	@Override
	public String toString() {
		return String.format("%s %s", this.getClass().getSimpleName(), this.carNumber);
	}
	
	public void setEnterStationTime(long varEnterStationTime)
	{
		this.enterStationTime = varEnterStationTime;
	}
	
	public long getEnterStationTime()
	{
		return this.enterStationTime;
	}

	@Readonly
	public float getCurrentCapacity() {
		return currentCapacity;
	}

	@Mutable
	public void setCurrentCapacity(float currentCapacity) {
		this.currentCapacity = currentCapacity;
	}

	@Readonly
	abstract public float getChargingTime(ChargingStation station);

	@Readonly
	public String getCarNumber() {
		return carNumber;
	}

	@Mutable
	public void setCarNumber(String carNumber) {
		this.carNumber = carNumber;
	}

	@Readonly
	public float getTankCapacity() {
		return tankCapacity;
	}

	@Mutable
	public void setTankCapacity(float tankCapacity) {
		this.tankCapacity = tankCapacity;
	}

	@Readonly
	public float getWaitDuration() {
		return waitDuration;
	}

	@Mutable
	public void setWaitDuration(float waitDuration) {
		this.waitDuration = waitDuration;
	}

	@Readonly
	public LocationAPI getApi() {
		return api;
	}

	@Mutable
	public void setApi(LocationAPI api) {
		this.api = api;
	}

	@Mutable
	public void setCurrState(CarState currState) {
		this.currState = currState;
	}

	public boolean isPriority() {
		return priorityFlag;
	}

	public void setPriorityFlag(boolean priorityFlag) {
		this.priorityFlag = priorityFlag;
	}
	
	public void setAssignedSemaphoreID(int varSemaphoreID)
	{
		this.semaphoreID = varSemaphoreID;
	}

	public int getAssignedSemaphoreID()
	{
		return this.semaphoreID;
	}
	
	/**
	 * This method should return the nearest charging station based on the following
	 * criteria and order: - Location of the station (nearest is better) - Waiting
	 * time (station's waiting time should be lower than car's waiting time) -
	 * Station's capacity (station should have enough fuel left for this car)
	 */
	@Readonly
	@APIMethod
	public ChargingStation getNearestFreeChargingStation() throws ChargingStationNotFoundException {
		// Getting the nearest station from the LocationAPI
		this.logger.finer("Finding nearest charging station...");
		ChargingStation[] nearestStations;
		try {
			nearestStations = LocationAPI.calculateNearestStation(currentGPS, api.getChargingStation(), this);
		} catch (InvalidGPSValueException e) {
			throw new ChargingStationNotFoundException(
					"Car: " + carNumber + "; LocationAPI returned no close stations.");
		}

		// Checking if it returned any stations. Throwing exception when not
		if (nearestStations.length == 0) {
			throw new ChargingStationNotFoundException(
					"Car: " + carNumber + "; LocationAPI returned no close stations.");
		}

		// Iterating over the found stations and checking for empty slots and if the
		// type is matching
		for (int i = 0; i < nearestStations.length; i++) {
			double totalWaitingTime;
			float tankLeftOver;
			if (this instanceof ElectricCar) {
				ChargingStation currentStation = nearestStations[i];
				if (currentStation == null) {
					continue;
				}

			} else { // GasCar
				ChargingStation currentStation = nearestStations[i];
			}

			this.logger.finest(nearestStations[i].toString() + " is a match.");
			return nearestStations[i];
		}

		throw new ChargingStationNotFoundException("Car: " + carNumber + " could not find a free station.");
	}

	/**
	 * This method will add the car to the station's queue.
	 */
	@Mutable
	public void joinStationQueue(ChargingStation station) {
		this.logger.finer("Joining queue of " + station.toString());
		station.addCarToWaitingQueue(this);
		currentChargingStation = station;
		currState = CarState.charging;
		this.logger.finest("Joined queue of " + station.toString());
	}

	/**
	 * return boolean values corresponding to if it's in 'looking' state or not.
	 */
	@Readonly
	public boolean isLooking() {
		if (currState == CarState.looking) {
			return true;
		}
		return false;
	}

	@Readonly
	public boolean isInQueue() {
		if (currState == CarState.inQueue) {
			return true;
		}
		return false;
	}

	@Readonly
	public boolean isCharging() {
		if (currState == CarState.charging) {
			return true;
		}
		return false;
	}

	@Readonly
	public boolean isCharged() {
		if (currState == CarState.charged) {
			return true;
		}
		return false;
	}
	
	/**
	 * Return the station the car joined to.
	 */
	@Readonly
	public ChargingStation getCurrentStation() {
		return this.currentChargingStation;
	};

	/**
	 * Leave station since the current station isn't suitable anymore. Set car state
	 * to looking.
	 */
	@Mutable
	public void leaveStationQueue() {
		setCurrState(CarState.looking);
		currentChargingStation.leaveStationwaitingQueue(this);
		currentChargingStation = null;
	};

	/**
	 * Car leaves map as no suitable station is available. Set state to charged!
	 */
	@Mutable
	public void leaveMap() {
		currState = CarState.charged;
		this.logger.info(
				this.toString() + " left the map because it couldn't find a station with acceptable waiting time");
	};

	/**
	 * Add the amount of fuel to the car's current capacity.
	 */
	@Mutable
	public void addFuel(double amount) {
		currentCapacity += amount;
		this.logger.fine(String.format("Received %f fuel. Current capacity: %f - Tank Capacity: %f", amount,
				this.currentCapacity, this.tankCapacity));
	}

	/**
	 * Returns the amount of fuel that is missing until the tank is full
	 */
	@Readonly
	public float getMissingAmountOfFuel() {
		return tankCapacity - currentCapacity;
	}
	
	public void isCharging(ChargingStation varChargingStation)
	{
		varChargingStation.chargeCar(this);
	}
}
