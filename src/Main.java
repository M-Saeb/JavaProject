import api.GPSValues;
import api.LocationAPI;
import car.*;
import exceptions.ChargingStationNotFoundException;
import exceptions.InvalidGPSLatitudeException;
import exceptions.InvalidGPSLongitudeException;
import exceptions.InvalidGPSValueException;
import stations.*;

public class Main {

	public static void main(String[] args) {
		ChargingStation[] stations = new ChargingStation[4];
		
		// create pool of charging stations
		try {
			stations[0] = new GasStation(1, new GPSValues(44, 96), 3, 10);
			stations[1] = new GasStation(2, new GPSValues(10, 50), 2, 5);
			stations[2] = new ElectricStation(3, new GPSValues(-50, 150), 4, 15);
			stations[3] = new ElectricStation(4, new GPSValues(-70, 44), 1, 5);
		} catch (InvalidGPSLatitudeException | InvalidGPSLongitudeException | InvalidGPSValueException e) {
			System.out.println(e.toString());
			return;
		}
		
		// create pool of cars
		Car[] cars = {
				new GasCar("Ford Mustang", (float) 60.9, (float) 120.0, new LocationAPI(stations), new GPSValues(40, 20)),
				new ElectricCar("Toyota Prius", (float) 13.0, (float) 30.0, new LocationAPI(stations), new GPSValues(-50, 50)),
				new GasCar("Peugeot 206", (float) 49.0, (float) 30.0, new LocationAPI(stations), new GPSValues(24, 140)),
				new ElectricCar("BW i5", (float) 83.9, (float) 70.0, new LocationAPI(stations), new GPSValues(80, 95)),
				new GasCar("Audi A3", (float) 48.0, (float) 90.0, new LocationAPI(stations), new GPSValues(40, 10))
		};
		System.out.println("Created cars and charging stations.");
		
		// create pool of threads
		
		// send cars to charging stations

	}
}
