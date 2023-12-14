import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

public class main {
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

class ChargingStation {
    private final ChargingSlots chargingSlots;
    private final List<Car> waitingQueue;
    private final int id;

    public ChargingStation(int id, int numberOfSlots) {
        this.id = id;
        this.chargingSlots = new ChargingSlots(numberOfSlots);
        this.waitingQueue = new ArrayList<>();
    }

    public void chargeCar(Car car) {
        synchronized (car) {
            if (chargingSlots.tryAcquireSlot(car)) {
                // Charging logic with a random delay
                System.out.println(car.getType() + " car " + car.getId() + " is charging at Station " + id +
                        " at Semaphore " + car.getAssignedSemaphoreId() + ".");

                // Introduce a random delay during charging
                try {
                    int randomDelay = (int) (Math.random() * 1000); // Random delay between 0 and 1000 milliseconds
                    Thread.sleep(randomDelay);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }

                chargingSlots.releaseSlot(car); // Release the slot when charging is complete
                car.notify(); // Notify waiting threads that charging is complete
            } else {
                // If slots are full, enqueue the car with entry time
                car.setEntryTime(System.currentTimeMillis());
                System.out.println(car.getType() + " car " + car.getId() + " is waiting in the queue at Station " + id + ".");
                waitingQueue.add(car);
            }
        }
    }

    public void releaseQueuedCars() {
        synchronized (waitingQueue) {
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
        }
    }

    public boolean isCarInQueue(Car car) {
        synchronized (waitingQueue) {
            return waitingQueue.contains(car);
        }
    }

    public int getId() {
        return id;
    }

    public ChargingSlots getChargingSlots() {
        return chargingSlots;
    }
}

class ChargingSlots {
    private final SlotAssignment[] slotAssignments;
    private final Semaphore[] semaphores;
    private final int[] semaphoreIds; // Added array to store semaphore IDs

    public ChargingSlots(int numberOfSlots) {
        this.slotAssignments = new SlotAssignment[numberOfSlots];
        this.semaphores = new Semaphore[numberOfSlots];
        this.semaphoreIds = new int[numberOfSlots]; // Initialize array for semaphore IDs

        for (int i = 0; i < numberOfSlots; i++) {
            slotAssignments[i] = new SlotAssignment(i);
            semaphores[i] = new Semaphore(1); // Initialize Semaphore for each slot
            semaphoreIds[i] = i + 1; // Initialize semaphore IDs (e.g., 1, 2, 3, ...)
        }

        // Initialize and start threads for each slot
        for (int i = 0; i < numberOfSlots; i++) {
            Thread slotThread = new Thread(new ChargingSlotRunnable(i));
            slotThread.start();
        }
    }

    public boolean tryAcquireSlot(Car car) {
        synchronized (slotAssignments) {
            for (int i = 0; i < slotAssignments.length; i++) {
                if (slotAssignments[i].tryAcquire(car, semaphores[i], semaphoreIds[i])) {
                    return true;
                }
            }
            return false;
        }
    }

    public void releaseSlot(Car car) {
        synchronized (slotAssignments) {
            for (SlotAssignment assignment : slotAssignments) {
                assignment.release(car);
            }
        }
    }

    public SlotAssignment getAssignedSlot(Car car) {
        synchronized (slotAssignments) {
            for (SlotAssignment assignment : slotAssignments) {
                if (assignment.isAssigned(car)) {
                    return assignment;
                }
            }
            return null;
        }
    }

    public boolean isChargingComplete(Car car) {
        synchronized (slotAssignments) {
            for (SlotAssignment assignment : slotAssignments) {
                if (assignment.isAssigned(car)) {
                    return false;
                }
            }
            return true;
        }
    }

    private class ChargingSlotRunnable implements Runnable {
        private final int slotId;

        public ChargingSlotRunnable(int slotId) {
            this.slotId = slotId;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    semaphores[slotId].acquire(); // Acquire the semaphore to simulate mutual exclusion
                    Thread.sleep(500); // Simulate charging time
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } finally {
                    semaphores[slotId].release(); // Release the semaphore after charging
                }

                SlotAssignment assignment = slotAssignments[slotId];
                if (assignment.isAssigned()) {
                    Car car = assignment.getCar();
                    int semaphoreId = semaphoreIds[slotId];

                    System.out.println(car.getType() + " car " + car.getId() +
                            " is charging at Slot " + slotId +
                            " at Semaphore " + semaphoreId);
                }
            }
        }
    }

    public class SlotAssignment {
        private Car car;
        private final int slotId;

        public SlotAssignment(int slotId) {
            this.slotId = slotId;
        }

        public boolean tryAcquire(Car car, Semaphore semaphore, int semaphoreId) {
            try {
                semaphore.acquire();
                if (this.car == null) {
                    this.car = car;
                    car.setAssignedSemaphoreId(semaphoreId);
                    return true;
                }
                return false;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            } finally {
                semaphore.release();
            }
        }

        public void release(Car car) {
            if (this.car != null && this.car.equals(car)) {
                this.car = null;
            }
        }

        public boolean isAssigned() {
            return car != null;
        }

        public boolean isAssigned(Car car) {
            return this.car != null && this.car.equals(car);
        }

        public Car getCar() {
            return car;
        }

        public int getSlotId() {
            return slotId;
        }
    }
}

class Car {
    private final String type;
    private final int id;
    private long entryTime;
    private int assignedSemaphoreId;

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

    public int getAssignedSemaphoreId() {
        return assignedSemaphoreId;
    }

    public void setAssignedSemaphoreId(int assignedSemaphoreId) {
        this.assignedSemaphoreId = assignedSemaphoreId;
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
        ChargingSlots chargingSlots = chargingStation.getChargingSlots();

        // Ensure that charging is complete before checking the assigned slot
        synchronized (car) {
            while (!chargingSlots.isChargingComplete(car)) {
                try {
                    car.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.err.println("Thread interrupted while waiting for charging to complete.");
                    return;
                }
            }
        }

        ChargingSlots.SlotAssignment assignedSlot = chargingSlots.getAssignedSlot(car);

        // Check if the car was ever assigned a slot
        if (assignedSlot != null) {
            int slotId = assignedSlot.getSlotId();
            System.out.println(car.getType() + " car " + car.getId() +
                    " is charging at Slot " + slotId +
                    " at Station " + chargingStation.getId() +
                    " at Semaphore " + car.getAssignedSemaphoreId());
        } else {
            // Avoid printing any message if the car was never assigned a slot
            System.out.println("Warning: Car " + car.getId() + " finished charging, but no assigned slot found. " +
                    "This might be due to the car leaving the queue after waiting for too long.");
        }
    }
}
