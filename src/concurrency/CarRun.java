package concurrency;

import car.Car;
import car.ElectricCar;
import stations.ChargingSlot;
import stations.ChargingStation;

public class CarRun implements Runnable
{
	private ChargingStation chargingStation;
	private Car car;
	
	public CarRun(ChargingStation varChargingStation, Car varCar)
	{
		this.chargingStation = varChargingStation;
		this.car = varCar;
	}
	
	@Override
	public void run() {
		this.car.isCharging(this.chargingStation);
		if(this.car instanceof ElectricCar)
		{
			ChargingSlot availableSlots = this.chargingStation.getAvailableSlots(this.car);
			synchronized (this.car) {
				while(!availableSlots.isChargingComplete(this.car))
				{
					try {
						// Wait until thread is synchronized
						this.car.wait();
					} catch (Exception e) {
						Thread.currentThread();
	                    System.err.println("Thread interrupted while waiting for charging to complete.");
	                    return;
					}
				}
			}
			
			ChargingSlot.SlotAssigment currentAssignedSlot = availableSlots.getAquiredSlot(this.car);
			
			if(currentAssignedSlot != null)
			{
				System.out.println(""
						+ "Car... " + this.car.getCarNumber() + " " +
						"@Slot... " + currentAssignedSlot.getSlotID() + " " +
						"@Station... " + chargingStation.getChargingStationID());
			}
			else {
				System.out.println("----------------------------------\\n"
						+ "Warning... " + this.car.getCarNumber() + " " +
						"left due to excesive waiting... ");
				System.out.println(""
						+ "Car... " + this.car.getCarNumber() + " \n" +
						"@Station... " + this.chargingStation.getChargingStationID() + " \n" +
						"EXIT the queue for waiting too much time \n");
			}
		}
	}
}
