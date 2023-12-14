import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class ChargingStation {
    private final ChargingSlots slots;
    private final List<Car> waitingQueue;
    private final Lock stationLock;
    private final int id;

    public ChargingStation(int id, int numberOfSlots) {
        this.id = id;
        this.slots = new ChargingSlots(numberOfSlots);
        this.waitingQueue = new ArrayList<>();
        this.stationLock = new ReentrantLock();
    }

    public void chargeCar(Car car) {
        try {
            stationLock.lock();
            if (slots.tryAcquireSlot(car)) {
                // Charging logic here
                System.out.println(car.getType() + " car " + car.getId() + " is charging at Station " + id + ".");
                slots.releaseSlot(car);
            } else {
                // If slots are full, enqueue the car with entry time
                car.setEntryTime(System.currentTimeMillis());
                System.out.println(car.getType() + " car " + car.getId() + " is waiting in the queue at Station " + id + ".");
                waitingQueue.add(car);
            }
        } finally {
            stationLock.unlock();
        }
    }

    public void releaseQueuedCars() {
        try {
            stationLock.lock();
            List<Car> carsToCharge = new ArrayList<>(waitingQueue);
            waitingQueue.clear();

            for (Car car : carsToCharge) {
                long waitingTime = System.currentTimeMillis() - car.getEntryTime();
                if (waitingTime <= 15 * 1000) { // 15 seconds in milliseconds
                    chargeCar(car); // Try to charge the car again
                } else {
                    System.out.println(car.getType() + " car " + car.getId() + " left the queue at Station " + id +
                            " after waiting too long.");
                }
            }
        } finally {
            stationLock.unlock();
        }
    }
}

class ChargingSlots {
    private final Semaphore semaphore;

    public ChargingSlots(int numberOfSlots) {
        this.semaphore = new Semaphore(numberOfSlots, true);
    }

    public boolean tryAcquireSlot(Car car) {
        try {
            System.out.println(car.getType() + " car " + car.getId() + " is trying to acquire a slot.");
            return semaphore.tryAcquire();
        } catch (Exception e) {
            return false;
        }
    }

    public void releaseSlot(Car car) {
        System.out.println(car.getType() + " car " + car.getId() + " released the slot.");
        semaphore.release();
    }
}

class Car {
    private final String type;
    private final int id;
    private long entryTime;

    public Car(int id, String type) {
        this.id = id;
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public int getId() {
        return id;
    }

    public long getEntryTime() {
        return entryTime;
    }

    public void setEntryTime(long entryTime) {
        this.entryTime = entryTime;
    }

    public void charge(ChargingStation chargingStation) {
        chargingStation.chargeCar(this);
    }
}

class ChargingStationRunnable implements Runnable {
    private final ChargingStation chargingStation;

    public ChargingStationRunnable(ChargingStation chargingStation) {
        this.chargingStation = chargingStation;
    }

    @Override
    public void run() {
        while (true) {
            chargingStation.releaseQueuedCars();
            try {
                Thread.sleep(5000); // Simulating a delay
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

class CarChargingRunnable implements Runnable {
    private final Car car;
    private final ChargingStation chargingStation;

    public CarChargingRunnable(Car car, ChargingStation chargingStation) {
        this.car = car;
        this.chargingStation = chargingStation;
    }

    @Override
    public void run() {
        car.charge(chargingStation);
    }
}

public class Main {
    public static void main(String[] args) {
        int numberOfSlotsPerStation = 5;
        int numberOfStations = 2;
        int numberOfCars = 10;

        List<ChargingStation> chargingStations = new ArrayList<>();

        for (int i = 0; i < numberOfStations; i++) {
            ChargingStation chargingStation = new ChargingStation(i + 1, numberOfSlotsPerStation);
            chargingStations.add(chargingStation);

            Thread stationThread = new Thread(new ChargingStationRunnable(chargingStation));
            stationThread.start();
        }

        for (int i = 0; i < numberOfCars; i++) {
            String carType = i % 2 == 0 ? "Electric" : "Gas";
            Car car = new Car(i + 1, carType);

            ChargingStation chargingStation = chargingStations.get(i % numberOfStations);

            Thread chargingThread = new Thread(new CarChargingRunnable(car, chargingStation));
            chargingThread.start();
        }
    }
}
