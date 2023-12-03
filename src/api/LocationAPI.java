package api;

import stations.ChargingStation;
import car.*;

import java.lang.Math;
import java.util.Arrays;
import java.util.Comparator;

import exceptions.InvalidGPSLatitudeException;
import exceptions.InvalidGPSLongitudeException;
import exceptions.InvalidGPSValueException;
import exceptions.InvalidGPSObject;

public class LocationAPI
{
    ChargingStation[] class_chargingStation;
    Car class_carCar;

    /*
    Constructor for the API object
    Input:
    - ChargingStation[] -> List of objects called 'ChargingStation' that will be available/existing in the area
     */
    public LocationAPI(ChargingStation[] class_chargingStation)
    {
        this.class_chargingStation = class_chargingStation;
    }
    /* 
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
    


	public ChargingStation[] getChargingStation() {
		return class_chargingStation;
	}
    /* Local functions */
    /*
    Function: setChargingStations
    Description: Assign the class ChargingStation to the API so all the functions can use this object.
    Input:
    -varChargingStation -> List of Charging stations in the area
    Return: Void
    Note:
    Important to use before 'calculateNearestStation'
     */
    public void setChargingStations(ChargingStation[] varChargingStation)
    {
        this.class_chargingStation = varChargingStation;
    }
    /*
    Function: sortNearestStation
    Description: Calculate the nearest station regarding the auto current location that is given to the function
    Input:
    -varLatitud -> Current coordinates of the car
    -varLongitud -> Current coordinates of the car
    Return: Nearest Charging Station ID
     */
    @SuppressWarnings("unused")
	protected static int[] sortNearestStation(GPSValues gpsValues, ChargingStation[] class_chargingStation) throws InvalidGPSObject, InvalidGPSValueException
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
            try {
    			checkGPSValues(gpsValues);
    		} catch (InvalidGPSLatitudeException e) {
    			e.printStackTrace();
    		} catch (InvalidGPSLongitudeException e) {
    			e.printStackTrace();
    		} catch (InvalidGPSValueException e) {
    			e.printStackTrace();
    		}
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
            	sortedArray[i] = totalDistance[i][0];
            }
        }
        /* Return the station ID that it is closest to the station */
        return sortedArray;
    }

    /*
    Function: calculateNearestStation
    Description: Calculate the nearest station regarding the auto current location that is given to the function
    Input:
    -varSortedArray[] -> sorted array, so we know the order of the stations
    -varArrStations[] -> array or list of all the stations in the area
    Return: Nearest Charging Station ID
     */
    public static ChargingStation[] calculateNearestStation(GPSValues gpsValues, ChargingStation[] class_chargingStation) throws InvalidGPSValueException
    {
        int[] varSortedArray = new int[class_chargingStation.length];
        ChargingStation[] sortedStations = new ChargingStation[class_chargingStation.length];

        try {
			varSortedArray = sortNearestStation(gpsValues, class_chargingStation);
		} catch (InvalidGPSObject e) {
			e.printStackTrace();
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

        return sortedStations;
    }
    /*
    Function: checkGPSValues
    Description: Validates that the GPS values are a valid value
    Input:
    - List of available Stations
    Return:
    - void
     */
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
    /*
    Function: printArray
    Description: 
    Input:
    - List of available Stations
    Return:
    - Sorted list of available stations order from closest to farthest away
     */
    public static void printArray(ChargingStation[] array) {
        for (ChargingStation station : array) {
            System.out.println(station);
        }
        System.out.println();
    }

}
