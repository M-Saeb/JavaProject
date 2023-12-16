package stations;

import java.time.LocalDateTime;
import java.util.logging.Logger;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;

import annotations.Mutable;
import annotations.Readonly;
import car.Car;
import concurrency.ChargingSlotRun;


public class ChargingSlot {
	private int id;
	private int totalSlots;
	protected ChargingStation chargingStation;
	protected Car currentCar = null;
	protected Logger logger;
	
	private SlotAssigment[] slotAssigment;
	private Semaphore[] semaphore;
	private int[] semaphoreID;
	
	public ChargingSlot(int numSlots) 
	{		
		this.totalSlots = numSlots;
		/* Initialize Semaphore */
		this.semaphore = new Semaphore[numSlots];	
		this.slotAssigment = new SlotAssigment[numSlots];
		this.semaphoreID = new int[numSlots];
		
		for(int i = 0; i < numSlots; i++)
		{
			this.slotAssigment[i] = new SlotAssigment(i);
			this.semaphore[i] = new Semaphore(i);
			this.semaphoreID[i] = i++;
		}
		
		for(int i = 0; i < numSlots; i++)
		{
			slotAssigment[i] = new SlotAssigment(i);
			Thread slotThread = new Thread(new ChargingSlotRun(numSlots, this));
			slotThread.start();
		}
	}
	
	
	public int getSemaphoreID()
	{
		return System.identityHashCode(this);
	}
	public Semaphore getSemaphore(int varSlotID)
	{
		return this.semaphore[varSlotID];
	}
	public SlotAssigment getSlotAssigment(int varSlotID)
	{
		return this.slotAssigment[varSlotID];
	}
	
	/* Whole purpose of this class is to just implement a way of tracking which car goes into which slot */
	public static class SlotAssigment
	{
		private Car car;
		private int slotID;
		
		public SlotAssigment(int varSlotID)
		{
			this.slotID = varSlotID;
		}
		
		public int getSlotID()
		{
			return this.slotID;
		}
		
		public Car getCar()
		{
			return car;
		}
		public void setCar(Car car)
		{
			this.car = car;
		}
		
		public boolean tryAquire(Car varCar, Semaphore varSemaphore, int varSemaphoreID)
		{
			try {
				varSemaphore.tryAcquire();
				if(this.car == null)
				{
					this.car = varCar;
					try {
						this.car.setAssignedSemaphoreID(varSemaphoreID);
						return true;
					} catch (Exception e) {
						e.printStackTrace();
						return false;
					}
				}
				else {
					return false;
				}
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
			finally {
				varSemaphore.release();
			}
		}
		
		public void release(Car varCar)
		{
			if((this.car != null) && (this.car.equals(varCar)))
			{
				this.car = null;
			}
			else {
				/*
				 * Do Nothing
				 */
			}
		}
		
        public boolean isAssigned() 
        {
            return (this.car != null);
        }

        public boolean isAssigned(Car varCar) 
        {
            return (this.car != null && this.car.equals(varCar));
        }
	}
	
	@Mutable
	public boolean tryAquireSlot(Car varCar)
	{
		boolean slotAquired = false;
		synchronized (slotAssigment) {
			for(int i = 0; i < slotAssigment.length; i++)
			{
				slotAquired = slotAssigment[i].tryAquire(varCar, semaphore[i], semaphoreID[i]);
				if(slotAquired);
				{
					return true;
				}
			}
			return false;
		}
	}
	public void releaseAquiredSlot(Car varCar)
	{
		synchronized (slotAssigment) {
			for(SlotAssigment tempAssigment : slotAssigment)
			{
				tempAssigment.release(varCar);
			}
			
		}
	}
	
	public SlotAssigment getAquiredSlot(Car varCar)
	{
		synchronized (slotAssigment) {
			for(SlotAssigment tempAssigment : slotAssigment)
			{
				if(tempAssigment.isAssigned(varCar))
				{
					return tempAssigment;
				}
				else {
					/*
					 * Do Nothing
					 */
				}
			}
			return null;
			
		}
	}
	
	public boolean isChargingComplete(Car varCar)
	{
		synchronized (slotAssigment) {
			for(SlotAssigment tempAssigment : slotAssigment)
			{
				if(tempAssigment.isAssigned(varCar))
				{
					return false;
				}
			}
			return true;
		}
	}

	@Readonly
	public Car getCurrentCar() {
		return this.currentCar;
	}
	
	@Readonly
	public int getTotalSlots()
	{
		return this.totalSlots;
	}

	@Readonly
	private LocalDateTime calculateNextFreeTime() {
		long chargingTime =  (long) this.currentCar.getChargingTime(this.chargingStation);
		return LocalDateTime.now().plusSeconds(chargingTime);
	}

	@Readonly
	public String toString() {
		return String.format("Charging Slot %s", this.id);
	} 
	
}	
