package byteStream;

import java.io.FileInputStream;
import java.io.IOException;

import api.GPSValues;
import api.LocationAPI;
import car.*;

/**
 * 
 */
public class ByteStreamInputCars 
{
	private String gasOrElectricCar;
	private String carNumber;
	private float currentCapacity;
	private float tankCapacity;
	private float waitDuration;
	private LocationAPI api;
	private GPSValues gpsValues;
	private boolean priorityFlag;
	
	Car[] listCars;
	
	public Car[] getListCars() {
		return listCars;
	}
	private void setListCars(Car[] listCars) {
		this.listCars = listCars;
	}
	
	
	public String getGasOrElectricCar() {
		return gasOrElectricCar;
	}
	private void setGasOrElectricCar(String gasOrElectricCar) {
		this.gasOrElectricCar = gasOrElectricCar;
	}
	public String getCarNumber() {
		return this.carNumber;
	}
	private void setCarNumber(String carNumber) {
		this.carNumber = carNumber;
	}
	public float getCurrentCapacity() {
		return currentCapacity;
	}
	private void setCurrentCapacity(float currentCapacity) {
		this.currentCapacity = currentCapacity;
	}
	public float getTankCapacity() {
		return tankCapacity;
	}
	private void setTankCapacity(float tankCapacity) {
		this.tankCapacity = tankCapacity;
	}
	public float getWaitDuration() {
		return waitDuration;
	}
	private void setWaitDuration(float waitDuration) {
		this.waitDuration = waitDuration;
	}
	public LocationAPI getApi() {
		return api;
	}
	private void setApi(LocationAPI api) {
		this.api = api;
	}
	public GPSValues getGpsValues() {
		return gpsValues;
	}
	private void setGpsValues(GPSValues gpsValues) {
		this.gpsValues = gpsValues;
	}
	
	private void setPriorityFlag(boolean priorityFlag) {
		this.priorityFlag = priorityFlag;
	}
	
	private boolean getPriorityFlag() {
		return priorityFlag;
	}
	
	
	/*
	 * 
	 */
	public void carsInputByteStream(String filePath, LocationAPI api) throws IOException
	{
		int indexArray = 0;
		int byteData = 0;
		int carsCounter = 0;
		char recorveredParameterCharTemp;
		try (FileInputStream reader = new FileInputStream(filePath)) {
			while((byteData = reader.read()) != -1)
			{
				recorveredParameterCharTemp = (char)byteData;
				if(recorveredParameterCharTemp == '\n')
				{
					carsCounter++;
				}
			}
			// carsCounter++;
			/* Reader needs a reset, so we can go to the beginning of the file again */
			reader.close();
			Car[] cars = new Car[carsCounter++];
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
						Car tempCar;
						recorveredParameterString = recoverText.toString();
						String[] recorveredParameterStrings = recorveredParameterString.split(" ");
						
						setGasOrElectricCar(recorveredParameterStrings[0]);
						setCarNumber(recorveredParameterStrings[1]);
						setCurrentCapacity(Float.parseFloat(recorveredParameterStrings[2]));
						setTankCapacity(Float.parseFloat(recorveredParameterStrings[3]));
						setWaitDuration(Float.parseFloat(recorveredParameterStrings[4]));
						setGpsValues(new GPSValues(Float.parseFloat(recorveredParameterStrings[5]), Float.parseFloat(recorveredParameterStrings[6])));
						setPriorityFlag(Boolean.parseBoolean(recorveredParameterStrings[7]));

						if(getGasOrElectricCar().equals("GasCar"))
						{
							tempCar = new GasCar(getCarNumber(), getCurrentCapacity(), getTankCapacity(), getWaitDuration(), api, getGpsValues(), getPriorityFlag());

						}
						else if(getGasOrElectricCar().equals("ElectricCar")) {
							tempCar = new ElectricCar(getCarNumber(), getCurrentCapacity(), getTankCapacity(), getWaitDuration(), api, getGpsValues(), getPriorityFlag());

						}
						else
						{
							throw new IOException("Invalid Data Type...");
						}
						
						cars[indexArray] = tempCar;
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
					Car tempCar;
					recorveredParameterString = recoverText.toString();
					String[] recorveredParameterStrings = recorveredParameterString.split(" ");
					
					setGasOrElectricCar(recorveredParameterStrings[0]);
					setCarNumber(recorveredParameterStrings[1]);
					setCurrentCapacity(Float.parseFloat(recorveredParameterStrings[2]));
					setTankCapacity(Float.parseFloat(recorveredParameterStrings[3]));
					setWaitDuration(Float.parseFloat(recorveredParameterStrings[4]));
					setGpsValues(new GPSValues(Float.parseFloat(recorveredParameterStrings[5]), Float.parseFloat(recorveredParameterStrings[6])));
					setPriorityFlag(Boolean.parseBoolean(recorveredParameterStrings[7]));
					
					if(getGasOrElectricCar().equals("GasCar"))
					{
						tempCar = new GasCar(getCarNumber(), getCurrentCapacity(), getTankCapacity(), getWaitDuration(), api, getGpsValues(), getPriorityFlag());

					}
					else if(getGasOrElectricCar().equals("ElectricCar")) {
						tempCar = new ElectricCar(getCarNumber(), getCurrentCapacity(), getTankCapacity(), getWaitDuration(), api, getGpsValues(), getPriorityFlag());

					}
					else
					{
						throw new IOException("Invalid Data Type...");
					}
					
					cars[indexArray] = tempCar;
					recoverText = new StringBuilder(); 
				}
				setListCars(cars);

			} catch (Exception e) {
				e.printStackTrace();
			}
		} catch (Exception e) {
			System.out.println("An error occurred: " + e.getMessage());
		}
	}	
	public static Car[] getCars(String filePath, LocationAPI api)
	{
		ByteStreamInputCars objectByteStreamInput = new ByteStreamInputCars();
		
		try {
			objectByteStreamInput.carsInputByteStream(filePath, api);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return objectByteStreamInput.getListCars();
	}
}
