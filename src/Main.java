import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class ChargingSlots {
    private final Semaphore semaphore;

    public ChargingSlots(int numberOfSlots) {
        this.semaphore = new Semaphore(numberOfSlots, true);
    }

    public boolean tryAcquireSlot(Car car) {
        try {
            System.out.println(car.getType() + " car is trying to acquire a slot.");
            return semaphore.tryAcquire();
        } catch (Exception e) {
            return false;
        }
    }

    public void releaseSlot(Car car) {
        System.out.println(car.getType() + " car released the slot.");
        semaphore.release();
    }
}

class Car {
    private final String type;
    private long entryTime;

    public Car(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
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

class ChargingStation {
    private final ChargingSlots slots;
    private final List<Car> waitingQueue;
    private final Lock stationLock;

    public ChargingStation(int numberOfSlots) {
        this.slots = new ChargingSlots(numberOfSlots);
        this.waitingQueue = new ArrayList<>();
        this.stationLock = new ReentrantLock();
    }

    public void chargeCar(Car car) {
        try {
            stationLock.lock();
            if (slots.tryAcquireSlot(car)) {
                // Charging logic here
                System.out.println(car.getType() + " car is charging.");
                slots.releaseSlot(car);
            } else {
                // If slots are full, enqueue the car with entry time
                car.setEntryTime(System.currentTimeMillis());
                System.out.println(car.getType() + " car is waiting in the queue.");
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
                    System.out.println(car.getType() + " car left the queue after waiting too long.");
                }
            }
        } finally {
            stationLock.unlock();
        }
    }
}

public class main {
    public static void main(String[] args) {
        int numberOfSlots = 5;
        ChargingStation chargingStation = new ChargingStation(numberOfSlots);

        Thread stationThread = new Thread(() -> {
            // Your logic for the charging station thread
            // For example, you might periodically release queued cars
            while (true) {
                chargingStation.releaseQueuedCars();
                try {
                    Thread.sleep(5000); // Simulating a delay
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        stationThread.start();

        int numberOfCars = 10;
        for (int i = 0; i < numberOfCars; i++) {
            String carType = i % 2 == 0 ? "Electric" : "Gas";
            Car car = new Car(carType);

            // Logic for charging cars is now in the Car class
            Thread chargingThread = new Thread(() -> car.charge(chargingStation));
            chargingThread.start();
        }
    }
}
