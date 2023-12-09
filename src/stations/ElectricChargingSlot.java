package stations;

import annotations.Mutable;
import car.ElectricCar;
import exceptions.ChargingSlotFullException;

public class ElectricChargingSlot extends ChargingSlot {
    public ElectricChargingSlot(ChargingStation chargingStation, int id) {
        super(chargingStation, id);
    }

    @Mutable
    public void connectCar(ElectricCar car) throws ChargingSlotFullException {
        super.connectCar(car);
    }

    @Mutable
    public void chargeCar() {
        this.logger.info("Charging vehicle in slot...");
        this.currentCar.addFuel(this.chargingStation.getElectricityOutputPerSecond());
    }
}
