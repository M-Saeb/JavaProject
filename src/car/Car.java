package car;

import api.GPSValues;
import api.LocationAPI;
import stations.ChargingStation;
import exceptions.ChargingStationNotFoundException;
import exceptions.InvalidGPSValueException;

public abstract class Car
{
	protected String carNumber;
	private float tankCapacity;
	private float waitDuration;
	protected LocationAPI api;
    protected GPSValues currentGPS;
	
	public Car(String carNumber, float tankCapacity, float waitDuration, LocationAPI api, GPSValues currentGPS)
	{
		this.carNumber = carNumber;
		this.tankCapacity = tankCapacity;
		this.waitDuration = waitDuration;
		this.api = api;
		this.currentGPS = currentGPS;
	}
	
	public float getChargingTime(ChargingStation station)
	{
		return tankCapacity / station.getOutputPerSecond();
	}

	
	public String getCarNumber()
	{
		return carNumber;
	}

	public void setCarNumber(String carNumber)
	{
		this.carNumber = carNumber;
	}

	public float getTankCapacity()
	{
		return tankCapacity;
	}

	public void setTankCapacity(float tankCapacity)
	{
		this.tankCapacity = tankCapacity;
	}

	public float getWaitDuration()
	{
		return waitDuration;
	}

	public void setWaitDuration(float waitDuration)
	{
		this.waitDuration = waitDuration;
	}

	public LocationAPI getApi()
	{
		return api;
	}

	public void setApi(LocationAPI api)
	{
		this.api = api;
	}

	public abstract int getNearestFreeChargingStation() throws ChargingStationNotFoundException, InvalidGPSValueException;
}
