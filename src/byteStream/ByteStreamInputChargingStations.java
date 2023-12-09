package byteStream;

import java.io.FileInputStream;
import java.io.IOException;

import api.GPSValues;
import api.LocationAPI;
import stations.ChargingStation;

/**
 * 
 */
public class ByteStreamInputChargingStations 
{
	private int chargingStationID;
	private GPSValues gpsValues;
	private int numGasSlots;
	private int numElectricSlots;
	private float gasOutputPerSecondoutputPerSecond;
	private float electricityOutputPerSecond;
	private float levelOfGasStorage;
	private float levelOfElectricityStorage;
	
	ChargingStation[] listStations;
	
	public ChargingStation[] getListStations() {
		return listStations;
	}

	private void setListStations(ChargingStation[] listStations) {
		this.listStations = listStations;
	}

	public int getChargingStationID() {
		return chargingStationID;
	}

	private void setChargingStationID(int chargingStationID) {
		this.chargingStationID = chargingStationID;
	}

	public GPSValues getGpsValues() {
		return gpsValues;
	}

	private void setGpsValues(GPSValues gpsValues) {
		this.gpsValues = gpsValues;
	}

	public int getNumGasSlots() {
		return numGasSlots;
	}

	private void setNumGasSlots(int numGasSlots) {
		this.numGasSlots = numGasSlots;
	}

	public int getNumElectricSlots() {
		return numElectricSlots;
	}

	private void setNumElectricSlots(int numElectricSlots) {
		this.numElectricSlots = numElectricSlots;
	}

	public float getGasOutputPerSecondoutputPerSecond() {
		return gasOutputPerSecondoutputPerSecond;
	}

	private void setGasOutputPerSecondoutputPerSecond(float gasOutputPerSecondoutputPerSecond) {
		this.gasOutputPerSecondoutputPerSecond = gasOutputPerSecondoutputPerSecond;
	}

	public float getElectricityOutputPerSecond() {
		return electricityOutputPerSecond;
	}

	private void setElectricityOutputPerSecond(float electricityOutputPerSecond) {
		this.electricityOutputPerSecond = electricityOutputPerSecond;
	}

	private void setLevelOfElectricityStorage(float LevelOfElectricityStorage) {
		this.levelOfElectricityStorage = LevelOfElectricityStorage;
	}

	private void setLevelOfGasStorage(float LevelOfGasStorage) {
		this.levelOfGasStorage = LevelOfGasStorage;
	}

	private float getLevelOfElectricityStorage() {
		return this.levelOfElectricityStorage;
	}

	private float getLevelOfGasStorage() {
		return this.levelOfGasStorage;
	}


	/*
	 * 
	 */
	public void chargingStationsInputByteStream(String filePath) throws IOException
	{
		int indexArray = 0;
		int byteData = 0;
		int stationsCounter = 0;
		char recorveredParameterCharTemp;
		try (FileInputStream reader = new FileInputStream(filePath)) {
			while((byteData = reader.read()) != -1)
			{
				recorveredParameterCharTemp = (char)byteData;
				if(recorveredParameterCharTemp == '\n')
				{
					stationsCounter++;
				}
			}
			stationsCounter++;
			/* Reader needs a reset, so we can go to the beginning of the file again */
			reader.close();
			ChargingStation[] stations = new ChargingStation[stationsCounter++];
			/* If object was created correctly, move to obtain the information */
			StringBuilder recoverText = new StringBuilder();
			String recorveredParameterString;
			try {
				/* Reset object reader to the beginning of the file */
				FileInputStream readerTemp = new FileInputStream(filePath);
				byteData = 0;
				while((byteData = readerTemp.read()) != -1)
				{	
					char recorveredParameterChar = (char)byteData;
					if(recorveredParameterChar == '\n')
					{
						recorveredParameterString = recoverText.toString();
						String[] recorveredParameterStrings = recorveredParameterString.split(" ");
						
						setChargingStationID(Integer.parseInt(recorveredParameterStrings[0]));
						setGpsValues(new GPSValues(Float.parseFloat(recorveredParameterStrings[1]), Float.parseFloat(recorveredParameterStrings[2])));
						setNumGasSlots(Integer.parseInt(recorveredParameterStrings[3]));
						setNumElectricSlots(Integer.parseInt(recorveredParameterStrings[4]));
						setGasOutputPerSecondoutputPerSecond(Float.parseFloat(recorveredParameterStrings[5]));
						setElectricityOutputPerSecond(Float.parseFloat(recorveredParameterStrings[6]));
						setLevelOfElectricityStorage(Float.parseFloat(recorveredParameterStrings[7]));
						setLevelOfGasStorage(Float.parseFloat(recorveredParameterStrings[8]));
						ChargingStation tempStation = new ChargingStation(getChargingStationID(), getGpsValues(), getNumGasSlots(), getNumElectricSlots(), getGasOutputPerSecondoutputPerSecond(), getElectricityOutputPerSecond(), getLevelOfElectricityStorage(), getLevelOfGasStorage());
						
						stations[indexArray] = tempStation;
						recoverText = new StringBuilder(); 
						/* Update index until the value matches the created items */
						indexArray++;
					}
					else {
						recoverText.append(recorveredParameterChar);
					}
				}
				if(recoverText.length() > 0)
				{
					recorveredParameterString = recoverText.toString();
					String[] recorveredParameterStrings = recorveredParameterString.split(" ");
					
					setChargingStationID(Integer.parseInt(recorveredParameterStrings[0]));
					setGpsValues(new GPSValues(Float.parseFloat(recorveredParameterStrings[1]), Float.parseFloat(recorveredParameterStrings[2])));
					setNumGasSlots(Integer.parseInt(recorveredParameterStrings[3]));
					setNumElectricSlots(Integer.parseInt(recorveredParameterStrings[4]));
					setGasOutputPerSecondoutputPerSecond(Float.parseFloat(recorveredParameterStrings[5]));
					setElectricityOutputPerSecond(Float.parseFloat(recorveredParameterStrings[6]));
					ChargingStation tempStation = new ChargingStation(getChargingStationID(), getGpsValues(), getNumGasSlots(), getNumElectricSlots(), getGasOutputPerSecondoutputPerSecond(), getElectricityOutputPerSecond(), getLevelOfElectricityStorage(), getLevelOfGasStorage());
					
					stations[indexArray] = tempStation;
					recoverText = new StringBuilder(); 
				}
				setListStations(stations);

			} catch (Exception e) {
				System.out.println("End of File reached " + e.getMessage());
				throw new Exception("Invalid data type...");
			}
		} catch (Exception e) {
			System.out.println("An error occurred: " + e.getMessage());
			throw new IOException("Failed to open the file...");
		}
	}	
	public static ChargingStation[] getChargingStations(String filePath)
	{
		ByteStreamInputChargingStations objectByteStreamInput = new ByteStreamInputChargingStations();
		
		try {
			objectByteStreamInput.chargingStationsInputByteStream(filePath);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return objectByteStreamInput.getListStations();
	}
	
	public void printChargingStations()
	{
		ByteStreamInputChargingStations objectByteStreamInput = new ByteStreamInputChargingStations();
		ChargingStation[] tempChargingStations = objectByteStreamInput.getListStations();
		LocationAPI.printArray(tempChargingStations);
		
        System.out.println("Charging Station ID: " + objectByteStreamInput.getChargingStationID());
        System.out.println("Latitude: " + objectByteStreamInput.getGpsValues().getLatitude());
        System.out.println("Longitude: " + objectByteStreamInput.getGpsValues().getLongitude());
        System.out.println("Gas Slots: " + objectByteStreamInput.getNumGasSlots());
        System.out.println("Electric Slots: " + objectByteStreamInput.getNumElectricSlots());
        System.out.println("Gas Output: " + objectByteStreamInput.getGasOutputPerSecondoutputPerSecond());
        System.out.println("Electric Output: " + objectByteStreamInput.getElectricityOutputPerSecond());
        System.out.println("Electric Storage: " + objectByteStreamInput.getLevelOfElectricityStorage());
		System.out.println("Gas Storage: " + objectByteStreamInput.getLevelOfGasStorage());
		System.out.println();
	}
}
