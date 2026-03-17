import java.util.*;
import java.util.concurrent.*;

// Custom exception for booking errors
class InvalidBookingException extends Exception {
    public InvalidBookingException(String message) {
        super(message);
    }
}

// Reservation class
class Reservation {
    private String reservationId;
    private String guestName;
    private String roomType;
    private String roomId;

    public Reservation(String reservationId, String guestName, String roomType, String roomId) {
        this.reservationId = reservationId;
        this.guestName = guestName;
        this.roomType = roomType;
        this.roomId = roomId;
    }

    public String getReservationId() { return reservationId; }
    public String getGuestName() { return guestName; }
    public String getRoomType() { return roomType; }
    public String getRoomId() { return roomId; }

    @Override
    public String toString() {
        return "Reservation ID: " + reservationId + ", Guest: " + guestName +
                ", Room Type: " + roomType + ", Room ID: " + roomId;
    }
}

// Inventory service with synchronized allocation
class InventoryService {
    private final Map<String, Integer> inventory = new HashMap<>();

    public InventoryService() {
        inventory.put("STANDARD", 3);
        inventory.put("DELUXE", 2);
        inventory.put("SUITE", 1);
    }

    // Synchronized room allocation to prevent double-booking
    public synchronized String allocateRoom(String roomType) throws InvalidBookingException {
        int available = inventory.getOrDefault(roomType, 0);
        if (available <= 0) {
            throw new InvalidBookingException("No rooms available for type: " + roomType);
        }
        inventory.put(roomType, available - 1);
        return roomType.substring(0,3).toUpperCase() + "-" + UUID.randomUUID().toString().substring(0,5);
    }

    public synchronized void releaseRoom(String roomType) {
        inventory.put(roomType, inventory.getOrDefault(roomType, 0) + 1);
    }

    public synchronized Map<String, Integer> getInventoryStatus() {
        return new HashMap<>(inventory);
    }
}

// Booking history with thread-safe access
class BookingHistory {
    private final List<Reservation> confirmedReservations = Collections.synchronizedList(new ArrayList<>());

    public void addReservation(Reservation reservation) {
        confirmedReservations.add(reservation);
        System.out.println("✅ Added to booking history: " + reservation.getReservationId());
    }

    public List<Reservation> getAllReservations() {
        synchronized(confirmedReservations) {
            return new ArrayList<>(confirmedReservations);
        }
    }
}

// Runnable task for concurrent booking
class BookingTask implements Runnable {
    private final String guestName;
    private final String roomType;
    private final InventoryService inventoryService;
    private final BookingHistory bookingHistory;
    private static int reservationCounter = 101;

    public BookingTask(String guestName, String roomType,
                       InventoryService inventoryService, BookingHistory bookingHistory) {
        this.guestName = guestName;
        this.roomType = roomType;
        this.inventoryService = inventoryService;
        this.bookingHistory = bookingHistory;
    }

    @Override
    public void run() {
        try {
            // Allocate room safely (critical section)
            String roomId = inventoryService.allocateRoom(roomType);

            String reservationId;
            synchronized (BookingTask.class) {
                reservationId = "RES-" + reservationCounter++;
            }

            Reservation reservation = new Reservation(reservationId, guestName, roomType, roomId);
            bookingHistory.addReservation(reservation);

            System.out.println("Reservation successful for " + guestName);

        } catch (InvalidBookingException e) {
            System.out.println("❌ Booking failed for " + guestName + ": " + e.getMessage());
        }
    }
}

public class BookMyStayApp {

    public static void main(String[] args) throws InterruptedException {

        InventoryService inventoryService = new InventoryService();
        BookingHistory bookingHistory = new BookingHistory();

        // Simulate multiple guests submitting requests concurrently
        List<BookingTask> tasks = List.of(
                new BookingTask("Alice", "STANDARD", inventoryService, bookingHistory),
                new BookingTask("Bob", "DELUXE", inventoryService, bookingHistory),
                new BookingTask("Charlie", "STANDARD", inventoryService, bookingHistory),
                new BookingTask("David", "SUITE", inventoryService, bookingHistory),
                new BookingTask("Emma", "STANDARD", inventoryService, bookingHistory),
                new BookingTask("Frank", "STANDARD", inventoryService, bookingHistory) // may fail if inventory exhausted
        );

        // Use an ExecutorService to run tasks concurrently
        ExecutorService executor = Executors.newFixedThreadPool(3);

        for (BookingTask task : tasks) {
            executor.submit(task);
        }

        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);

        // Display final booking history and inventory
        System.out.println("\n--- Final Booking History ---");
        bookingHistory.getAllReservations().forEach(System.out::println);

        System.out.println("\n--- Final Inventory Status ---");
        inventoryService.getInventoryStatus().forEach((type, count) -> System.out.println(type + ": " + count));
    }
}