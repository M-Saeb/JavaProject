package car;
import api.GPSValues;
import api.LocationAPI;
import exceptions.ChargingStationNotFoundException;
import exceptions.InvalidGPSValueException;
import stations.ChargingStation;
import stations.GasStation;

public class GasCar extends Car
{

	public GasCar(String carNumber, float tankCapacity, float waitDuration, LocationAPI api, GPSValues currentGPS)
	{
		super(carNumber, tankCapacity, waitDuration, api, currentGPS);
		// TODO Auto-generated constructor stub
	}

	@Override
	public int getNearestFreeChargingStation() throws ChargingStationNotFoundException, InvalidGPSValueException {
		// Getting the nearest station from the LocationAPI
		ChargingStation[] nearestStations = LocationAPI.calculateNearestStation(currentGPS, api.getChargingStation());

		// Checking if it returned any stations. Throwing exception when not
		if (nearestStations.length == 0) {
			throw new ChargingStationNotFoundException(
					"Car: " + carNumber + "; LocationAPI returned no close stations.");
		}

		// Iterating over the found stations and checking for empty slots and if the
		// type is matching
		for (int i = 0; i < nearestStations.length; i++) {
			if (nearestStations[i].getAvailableSlots() >= 1 && nearestStations[i] instanceof GasStation) {
				return nearestStations[i].getChargingStationID();
			}
		}

		throw new ChargingStationNotFoundException("Car: " + carNumber + " could not find a free station.");
	}
	
}
