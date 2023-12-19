package stations;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import annotations.APIMethod;
import annotations.Mutable;
import annotations.Readonly;
import api.GPSValues;
import api.LocationAPI;
import car.Car;
import car.CarState;
import car.ElectricCar;
import car.GasCar;
import exceptions.InvalidGPSLatitudeException;
import exceptions.InvalidGPSLongitudeException;
import exceptions.InvalidGPSValueException;
import weather.WeatherState;
import weather.weather;


public class ChargingStation implements Runnable {	
	private Logger logger;
	/* Charging Station Info */
	private int chargingStationID;
	
	private GPSValues gpsValues;
	private float gasOutputPerSecond;
	private float electricityOutputPerSecond;
	private float levelOfElectricityStorage;
	private float levelOfGasStorage;
	
	private weather stationWeatherState = new weather();
	private EnergySource stationEnergySource = new EnergySource();
	private EnergyState currentEnergySource;
	private Semaphore gasSemaphore;
	private Semaphore electricitySemaphore;
	
	
	private ArrayList<Car> waitingQueue = new ArrayList<Car>();
	private ArrayList<ChargingSlot> electricSlots = new ArrayList<ChargingSlot>();
	private ArrayList<ChargingSlot> gasSlots = new ArrayList<ChargingSlot>();
	

	@APIMethod
	public ChargingStation(
			int chargingStationID,
			GPSValues gpsValues,
			int numGasSlots,
			int numElectricSlots,
			float gasOutputPerSecond,
			float electricityOutputPerSecond,
			float levelOfElectricityStorage,
			float levelOfGasStorage)
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
			stationWeatherState.getRandomWeather();
		}
		
		{
			if (numElectricSlots > 0) {
				for(int i=0; i < numElectricSlots; i++){
					electricitySemaphore = new Semaphore(numElectricSlots, true);
					
					String name = String.format("%s-ElecticSlot-%s ", this.toString(), i+1 );
					ChargingSlot slot = new ChargingSlot(name, this);
					this.electricSlots.add(slot);
					Thread slotThread = new Thread(slot);
					slotThread.start();
				}
			}
			if (numGasSlots > 0) {
				for(int i=0; i < numGasSlots; i++){
					gasSemaphore = new Semaphore(numGasSlots, true);
					
					String name = String.format("%s-GasSlot-%s ", this.toString(), i+1 );
					ChargingSlot slot = new ChargingSlot(name, this);
					this.gasSlots.add(slot);
					Thread slotThread = new Thread(slot);
					slotThread.start();
				}
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

			if (levelOfElectricityStorage < 0 || levelOfGasStorage < 0) {
				throw new IllegalArgumentException("Charging station storage can't be fewer than 0.");
			} else if (levelOfElectricityStorage == 0 && levelOfGasStorage == 0){
				throw new IllegalArgumentException("Station can't have 0 storage of any kind");
			}
			if (numGasSlots == 0 && levelOfGasStorage > 0) {
				throw new IllegalArgumentException("Station can't have 0 gas slots and still have gas output potential.");
			} else if (numElectricSlots == 0 && levelOfElectricityStorage > 0) {
				throw new IllegalArgumentException(
						"Station can't have 0 electricity slots and still have electricity output potential.");
			}
			this.levelOfElectricityStorage = levelOfElectricityStorage;
			this.levelOfGasStorage = levelOfGasStorage;
		}

		if (levelOfElectricityStorage < 0 || levelOfGasStorage < 0) {
			throw new IllegalArgumentException("Charging station storage can't be fewer than 0.");
		} else if (levelOfElectricityStorage == 0 && levelOfGasStorage == 0){
			throw new IllegalArgumentException("Station can't have 0 storage of any kind");
		}
		if (numGasSlots == 0 && levelOfGasStorage > 0) {
			throw new IllegalArgumentException("Station can't have 0 gas slots and still have gas output potential.");
		} else if (numElectricSlots == 0 && levelOfElectricityStorage > 0) {
			throw new IllegalArgumentException(
					"Station can't have 0 electricity slots and still have electricity output potential.");
		}
		this.levelOfElectricityStorage = levelOfElectricityStorage;
		this.levelOfGasStorage = levelOfGasStorage;

		if (levelOfElectricityStorage < 0 || levelOfGasStorage < 0) {
			throw new IllegalArgumentException("Charging station storage can't be fewer than 0.");
		} else if (levelOfElectricityStorage == 0 && levelOfGasStorage == 0){
			throw new IllegalArgumentException("Station can't have 0 storage of any kind");
		}
		if (numGasSlots == 0 && levelOfGasStorage > 0) {
			throw new IllegalArgumentException("Station can't have 0 gas slots and still have gas output potential.");
		} else if (numElectricSlots == 0 && levelOfElectricityStorage > 0) {
			throw new IllegalArgumentException(
					"Station can't have 0 electricity slots and still have electricity output potential.");
		}
		this.levelOfElectricityStorage = levelOfElectricityStorage;
		this.levelOfGasStorage = levelOfGasStorage;

		this.logger.fine("Initiated " + this.toString());
		this.logger.info(String.format("Weather: %s", stationWeatherState.getWeather()));
		
		/*
		 * Power Source of station
		 */
		if(stationWeatherState.getWeatherValue().ordinal() < WeatherState.cloudy.ordinal())
		{
			stationEnergySource.setSolar();
			currentEnergySource = stationEnergySource.getEnergyValue();
			currentEnergySource = stationEnergySource.getEnergyValue();
		}
		else {
			stationEnergySource.setPowerGrid();
			currentEnergySource = stationEnergySource.getEnergyValue();
			currentEnergySource = stationEnergySource.getEnergyValue();
		}
		this.logger.info(String.format("Power source: %s", currentEnergySource.toString()));
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
		return (int) gasSlots.stream()
			.filter(slot -> slot.currentCar == null).count();
	}

	@Readonly
	public int getAvailableElectricSlots() {
		return (int) electricSlots.stream()
			.filter(slot -> slot.currentCar == null).count();
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
		return levelOfElectricityStorage;
	}

	@Mutable
	public void setLevelOfElectricityStorage(float levelOfElectricityStorage) {
		this.levelOfElectricityStorage = levelOfElectricityStorage;
	}

	@Readonly
	public float getLevelOfGasStorage() {
		return levelOfGasStorage;
	}

	/**
	 * return the expected waiting time in seconds value
	 */
	@Readonly
	public float getExpectedWaitingTimeForElectricCars(){
		List<Car> electricCars = this.waitingQueue.stream()
			.filter(car -> car instanceof ElectricCar).toList();
		float totalWaitingTime = (float) 0.0;
		for (Car car: electricCars){
			float missingAmount = car.getMissingAmountOfFuel();
			float chargePerSecond = this.getElectricityOutputPerSecond();
			float totalExpectedTime = missingAmount / chargePerSecond;
			totalWaitingTime += totalExpectedTime;
		}
		return totalWaitingTime;
	}

	/**
	 * return the expected waiting time in seconds value
	 */
	@Readonly
	public float getExpectedWaitingTimeForGasCars(){
		List<Car> gasCars = this.waitingQueue.stream()
			.filter(car -> car instanceof GasCar).toList();
		float totalWaitingTime = (float) 0.0;
		for (Car car: gasCars){
			float missingAmount = car.getMissingAmountOfFuel();
			float chargePerSecond = this.getGasOutputPerSecond();
			float totalExpectedTime = missingAmount / chargePerSecond;
			totalWaitingTime += totalExpectedTime;
		}
		return totalWaitingTime;
	}

	@Mutable
	public void setLevelOfGasStorage(float levelOfGasStorage) {
		this.levelOfGasStorage = levelOfGasStorage;
	}

	@Mutable
	public void addCarToWaitingQueue(Car car) {
		// If car is prioritized, add it after the last prioritized car
		if (car.isPriority()) {
			this.logger.finer("Car is priority. Adding it to the top of the waitingQueue.");

			for (int i = 0; i < waitingQueue.size(); i++) {
				if (!waitingQueue.get(i).isPriority()) {
					waitingQueue.add(i, car);
					this.logger.finer("Added priority car at position " + i);
					return;
				}
			}
		}

		// Otherwise add normal
		waitingQueue.add(car);
		this.logger.fine(String.format(
			"Added %s to waitingQueue with number %s.",
			car.toString(), this.waitingQueue.size()
		));
		car.setEnterStationTime();
		car.setCurrentState(CarState.inQueue);
	}

	/**
	 * Remove car from station waitingQueue.
	 */
	@Mutable
	public void leaveStationWaitingQueue(Car car) {
		waitingQueue.remove(car);
		this.logger.fine(String.format("Removed %s from waitingQueue.", car));
	}

	/**
	 * Disonnect car from slot.
	 */
	@Mutable
	public void leaveSlot(Car car) throws Exception{
		this.logger.fine(String.format("%s is done charging. Removing it...", car.toString()));
		if (car instanceof ElectricCar) {
			int index = this.electricSlots.indexOf(car);
			if (index < 0){
				throw new Exception("Can't find the intended car");
			} 
			this.electricSlots.remove(index);
		}
		else if(car instanceof GasCar) { // GasCar
			int index = this.gasSlots.indexOf(car);
			if (index < 0){
				throw new Exception("Can't find the intended car");
			} 
			this.electricSlots.remove(index);		}
		else {
			logger.severe("Something went wrong: you order car numbered  " + car.toString()
			+ " out of the station, but the car is not in the station");
		}

		this.logger.fine(String.format("Removed %s from slot.", car.toString()));

	}

	@Mutable
	public void sendCarsToEmptyEletricSlots()
	{
		List<ChargingSlot> freeElecticSlots =  this.electricSlots.stream()
			.filter(slot -> slot.getCurrentCar() == null).toList();
		for (ChargingSlot slot: freeElecticSlots){
			Optional<Car> nextPossibleCar = this.waitingQueue.stream()
				.filter(car -> car instanceof ElectricCar).findFirst();
			// if (nextPossibleCars.)
			if (nextPossibleCar.isPresent()){
				try{
					Car car = nextPossibleCar.get();
					car.setCharginSlot(slot);
					this.waitingQueue.remove(car);

				} catch (Exception e){
					e.printStackTrace();
				}
			}
		}
	}

	@Mutable
	public void sendCarsToEmptyGasSlots()
	{
		List<ChargingSlot> freeGasSlots =  this.gasSlots.stream()
			.filter(slot -> slot.getCurrentCar() == null).toList();
		for (ChargingSlot slot: freeGasSlots){
			Optional<Car> nextPossibleCar = this.waitingQueue.stream()
				.filter(car -> car instanceof GasCar).findFirst();
			// if (nextPossibleCars.)
			if (nextPossibleCar.isPresent()){
				try{
					Car car = nextPossibleCar.get();
					car.setCharginSlot(slot);
					this.waitingQueue.remove(car);
				} catch (Exception e){
					e.printStackTrace();
				}
			}
		}
	}

	@Mutable
	public float consumeGas(float requestedAmount)
	{
		float amount = 0;
		
		try
		{
			gasSemaphore.tryAcquire(1000, TimeUnit.MILLISECONDS);
			
			if(levelOfElectricityStorage == 0)
			{
				this.logger.warning("Gas storage of station is empty!");
			}
			
			amount = requestedAmount;
			
			//Clipping if requested amount is too large
			if(amount > gasOutputPerSecond)
			{
				amount = gasOutputPerSecond;
			}
			
			//Checking if requested amount is larger than what is available
			if(amount > levelOfGasStorage)
			{
				amount = levelOfGasStorage;
			}
			
			levelOfGasStorage -= amount;
			this.logger.finer("Station supplied " + amount + " of gas. New level of storage: " + levelOfGasStorage);
		}
		catch (InterruptedException e)
		{
			this.logger.warning("Did not gas semaphore!");
		}
		finally
		{
			gasSemaphore.release();
		}
		
		return amount;
	}
	
	public float consumeElectricity(float requestedAmount)
	{
		float amount = 0;
		
		try
		{
			electricitySemaphore.tryAcquire(1000, TimeUnit.MILLISECONDS);
			
			if(levelOfElectricityStorage == 0)
			{
				this.logger.warning("Electricity storage of station is empty!");
			}
			
			
			amount = requestedAmount;
			
			//Clipping if requested amount is too large
			if(amount > electricityOutputPerSecond)
			{
				amount = electricityOutputPerSecond;
			}
			
			//Checking if requested amount is larger than what is available
			if(amount > levelOfElectricityStorage)
			{
				amount = levelOfElectricityStorage;
			}
			
			levelOfElectricityStorage -= amount;
			this.logger.finer("Station supplied " + amount + " of electricity. New level of storage: " + levelOfElectricityStorage);
		}
		catch (InterruptedException e)
		{
			this.logger.warning("Did not get electricity semaphore!");
		}
		finally
		{
			electricitySemaphore.release();
		}
		
		return amount;
	}
	
	@Override
	public void run() {
		while(true)
		{
			try {
				Thread.sleep(1000);
				this.sendCarsToEmptyGasSlots();
				this.sendCarsToEmptyEletricSlots();
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
