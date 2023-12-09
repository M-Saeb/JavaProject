package api;

import java.lang.Math;
import java.util.Arrays;
import java.util.Comparator;
import java.util.logging.Level;
import java.util.logging.Logger;

import annotations.Readonly;
import annotations.Mutable;

import stations.ChargingStation;
import car.*;

import exceptions.InvalidGPSLatitudeException;
import exceptions.InvalidGPSLongitudeException;
import exceptions.InvalidGPSValueException;
import exceptions.InvalidGPSObject;


public class LocationAPI
{
    ChargingStation[] class_chargingStation;
    Car class_carCar;

    public LocationAPI(ChargingStation[] class_chargingStation)
    {
        this.class_chargingStation = class_chargingStation;
    }

    public LocationAPI(ChargingStation[] class_chargingStation, Car class_car)
    {
        this.class_chargingStation = class_chargingStation;
        this.class_carCar = class_car;
    }

    /**
     * Constructor that will be able to throw an exception.
     * This will only occurs, when the assignation of the proper
     * constructor it is not called.
     */
    public LocationAPI() throws InvalidGPSObject
    {
        if(this.class_chargingStation == null) {
        	throw new InvalidGPSObject("Charging Station List not provided to constructor...");
        }
    }
    

    @Readonly
	public ChargingStation[] getChargingStation() {
		return class_chargingStation;
	}

    /**
    * Assign the class ChargingStation to the API so all the functions can use this object.
    */
    @Mutable
    public void setChargingStations(ChargingStation[] varChargingStation)
    {
        this.class_chargingStation = varChargingStation;
    }
    /**
    * Calculate the nearest station regarding the auto current location that is given to the function
    */
    @SuppressWarnings("unused")
    @Readonly
	protected static int[] sortNearestStation(GPSValues gpsValues, ChargingStation[] class_chargingStation, Car class_carObject) throws InvalidGPSObject, InvalidGPSValueException
    {
        float LattitudDiff = gpsValues.getLatitude();
        float LongitudDiff = gpsValues.getLongitude();
        int[][] totalDistance = new int[class_chargingStation.length][2];
        int[] sortedArray = new int[totalDistance.length];
       
        /* Method where the function 'setChargingStations' was called */
        if(class_chargingStation == null)
        {
        	throw new InvalidGPSObject("No Object has been initialiyed...");
        }
        /* Method where the function 'setChargingStations' was not called */
        else
        {
            for(int i = 0; i < class_chargingStation.length; i++)
            {
            	try {
    		        /*
    		        Calculate the distance between the 2 points
    		        Where x2 is the station latitude and x1 is the car location
    		        sqrt[ (x2 - x1)^2 + (y2 - y1)^2 ]
    		        */
                    LattitudDiff = (float) Math.pow((class_chargingStation[i].getGPSLatitude() - gpsValues.getLatitude()), 2);
                    LongitudDiff = (float) Math.pow((class_chargingStation[i].getGPSLongitude() - gpsValues.getLongitude()), 2);
                    totalDistance[i][0] = (int) class_chargingStation[i].getChargingStationID();
                    totalDistance[i][1] = (int) Math.sqrt(LattitudDiff + LongitudDiff);
					
                    /*
                     * Exception bound to happen if invalid values are assigned into the operation.
                     */
				} catch (Exception arithmeticException) {
					try {
						throw new InvalidGPSValueException("Invalid values to calculate distance");
					} catch (Exception chainedException) {
						System.out.println("Chained exception happend...");
						chainedException.printStackTrace();
					}
					
				}

            }
            /* Compare elements regarding the second position of the array and sort them from shortest to longest */
            Arrays.sort(totalDistance, Comparator.comparingInt(arr -> arr[1]));
            for(int i = 0; i<sortedArray.length; i++)
            {
            	if((class_carObject instanceof GasCar) && (class_chargingStation[i].getAvailableGasSlots() > 0))
            	{
            		sortedArray[i] = totalDistance[i][0];
            	}
            	else if((class_carObject instanceof ElectricCar) && (class_chargingStation[i].getAvailableElectricSlots() > 0)) 
            	{
            		sortedArray[i] = totalDistance[i][0];
				}
            	else {
					
				}
            }
        }
        /* Return the station ID that it is closest to the station */
        return sortedArray;
    }

    /**
    * Calculate the nearest station regarding the auto current location that is given to the function
    */
    @Readonly
    public static ChargingStation[] calculateNearestStation(GPSValues gpsValues, ChargingStation[] class_chargingStation, Car class_car) throws InvalidGPSValueException
    {
        Logger logger = Logger.getLogger("LocationAPI");
        int[] varSortedArray = new int[class_chargingStation.length];
        ChargingStation[] sortedStations = new ChargingStation[class_chargingStation.length];

        try {
			varSortedArray = sortNearestStation(gpsValues, class_chargingStation, class_car);
		} catch (InvalidGPSObject ex1) {
			ex1.printStackTrace();
		}
        for(int i = 0; i < varSortedArray.length; i++)
        {
            int currentID = varSortedArray[i];
            for(int j = 0; j < class_chargingStation.length; j++)
            {
                ChargingStation tempChargingStation = class_chargingStation[j];
                if(tempChargingStation.getChargingStationID() == currentID)
                {
                    sortedStations[i] = tempChargingStation;
                    /* Exit current cycle as we already found a match and updated the list */
                    break;
                }
                else
                {
                    continue;
                }
            }
        }
        if (logger.getLevel() == Level.FINEST){
            logger.finest(String.format(
                "Sorted arrays for %s. Result was %s",
                gpsValues.toString(),
                Arrays.toString(sortedStations)
            ));
        }
        return sortedStations;
    }
    /**
    * Validates that the GPS values are a valid value
    */
    @Readonly
	public static void checkGPSValues(GPSValues gpsValues)
        throws InvalidGPSLatitudeException, InvalidGPSLongitudeException, InvalidGPSValueException {
		float latitude = gpsValues.getLatitude();
        float longitude = gpsValues.getLongitude();
        if (latitude < -90.0 || latitude > 90) {
			throw new InvalidGPSLatitudeException("Invalid latitude: " + latitude);
		}
		if (longitude < -180.0 || longitude> 180) {
			throw new InvalidGPSLongitudeException("Invalid longtitude: " + longitude);
		}
        if (latitude == 0 && longitude == 0){
            throw new InvalidGPSValueException("You can't be in the ocean.");
        }
		
	}
    /**
    * print the stations in API class
    */
    @Readonly
    public static void printArray(ChargingStation[] array) {
        for (ChargingStation station : array) {
            System.out.println(station);
        }
        System.out.println();
    }
}
