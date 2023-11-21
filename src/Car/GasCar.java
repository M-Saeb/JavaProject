package Car;
import API.GPSValues;
import API.LocationAPI;
import Stations.ChargingStation;
import Stations.GasStation;
import exceptions.ChargingStationNotFoundException;

public class GasCar extends Car
{

	public GasCar(String carNumber, float tankCapacity, float waitDuration, LocationAPI api, GPSValues currentGPS)
	{
		super(carNumber, tankCapacity, waitDuration, api, currentGPS);
		// TODO Auto-generated constructor stub
	}

	@Override
	public ChargingStation getNearestFreeChargingStation() throws ChargingStationNotFoundException
	{
		//Getting the nearest station from the LocationAPI
		ChargingStation[] nearestStations = api.calculateNearestStation(currentGPS);
		
		//Checking if it returned any stations. Throwing exception when not
		if (nearestStations.length == 0)
		{
			throw new ChargingStationNotFoundException("Car: " + carNumber + "; LocationAPI returned no close stations.");
		}
		
		//Iterating over the found stations and checking for empty slots
		for(int i = 0; i < nearestStations.length; i++)
		{
			if(nearestStations[i].getAvailableSlots() >= 1 && nearestStations[i] instanceof GasStation)
			{
				return nearestStations[i];
			}
		}
		
		throw new ChargingStationNotFoundException("Car: " + carNumber + " could not find a free station.");
	}
	
}
