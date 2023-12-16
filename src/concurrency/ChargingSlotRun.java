package concurrency;

import java.util.concurrent.CountDownLatch;

import car.Car;
import stations.ChargingSlot;
import stations.ChargingStation;

public class ChargingSlotRun implements Runnable {
	
	private int slotID;
	private ChargingSlot chargingSlot;
	
	public ChargingSlotRun(int varSlotID, ChargingSlot varChargingSlot)
	{
		this.slotID = varSlotID;
		this.chargingSlot = varChargingSlot;
	}
	
	@Override
	public void run()
	{
		while(true)
		{
			try {
				this.chargingSlot.getSemaphore(this.slotID).acquire();
				Thread.sleep(500);
			} catch (Exception e) {
				Thread.currentThread().interrupt();
				break;
			}
			finally {
				//this.chargingSlot.getSemaphore(this.slotID).release();
			}
			
			ChargingSlot.SlotAssigment slotAssigment = chargingSlot.getSlotAssigment(this.slotID);
			
			if(slotAssigment.isAssigned())
			{
				System.out.println(""
						+ "ChargingRun" + " " +
						"@Car... " + slotAssigment.getCar().getCarNumber() + " " +
						"Is charging... " + " " +
						"@Slot... " + this.slotID);
			}
		}
	}
}
