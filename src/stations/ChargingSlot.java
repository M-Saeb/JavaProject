package stations;

import java.time.LocalDateTime;
import java.util.logging.Logger;

import byteStream.ByteStreamHandler;

import annotations.Mutable;
import annotations.Readonly;
import car.Car;
import exceptions.ChargingSlotFullException;


abstract public class ChargingSlot {
	private int id;
	protected ChargingStation chargingStation;
	protected Car currentCar = null;
	private LocalDateTime nextFreeTime;
	protected Logger logger;
	
	public ChargingSlot(ChargingStation chargingStation, int id) {
		if (chargingStation == null) {
			throw new IllegalArgumentException("Supplied charging station is null.");
		}
		this.chargingStation = chargingStation;
		this.id = id;
		this.logger = Logger.getLogger(
			String.format(
				"%s.%s",
				chargingStation.toString(),
				this.toString()));
		this.logger.addHandler(new ByteStreamHandler("logs/byteStreamLog.log"));
		this.logger.fine("Initiated Charging Slot " + this.id);
	}

	@Mutable
	public void connectCar(Car car) throws ChargingSlotFullException {
		// if there is a car already docked in this slot, raise an exception
		if (this.currentCar != null) {
			throw new ChargingSlotFullException("Slot " + this.id + " is full.");
		}
		this.currentCar = car;
		
		// calculate when the charging finishes and the slot is available
		this.nextFreeTime = this.calculateNextFreeTime();
		this.logger.info("Plugged in " + this.currentCar.toString());
		this.logger.fine("Slot will be free at " + this.nextFreeTime.toString());
	}


	abstract public void chargeCar();

	@Mutable
	public void disconnectCar(){
		this.currentCar = null;
	}

	@Readonly
	public int getId(){
		return this.id;
	}

	@Readonly
	public Car getCurrentCar() {
		return this.currentCar;
	}
	
	@Readonly
	public LocalDateTime getNextFreeTime() {
		return this.nextFreeTime;
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
	
