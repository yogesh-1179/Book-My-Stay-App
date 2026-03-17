import java.io.*;
import java.util.*;

// Serializable Reservation class
class Reservation implements Serializable {
    private static final long serialVersionUID = 1L;
    private String reservationId;
    private String guestName;
    private String roomType;
    private String roomId;
    private boolean isCancelled;

    public Reservation(String reservationId, String guestName, String roomType, String roomId) {
        this.reservationId = reservationId;
        this.guestName = guestName;
        this.roomType = roomType;
        this.roomId = roomId;
        this.isCancelled = false;
    }

    public String getReservationId() { return reservationId; }
    public String getGuestName() { return guestName; }
    public String getRoomType() { return roomType; }
    public String getRoomId() { return roomId; }
    public boolean isCancelled() { return isCancelled; }
    public void cancel() { this.isCancelled = true; }

    @Override
    public String toString() {
        return "Reservation ID: " + reservationId +
                ", Guest: " + guestName +
                ", Room Type: " + roomType +
                ", Room ID: " + roomId +
                ", Status: " + (isCancelled ? "CANCELLED" : "CONFIRMED");
    }
}

// Inventory service with serializable state
class InventoryService implements Serializable {
    private static final long serialVersionUID = 1L;
    private Map<String, Integer> inventory = new HashMap<>();

    public InventoryService() {
        inventory.put("STANDARD", 3);
        inventory.put("DELUXE", 2);
        inventory.put("SUITE", 1);
    }

    public synchronized void allocateRoom(String roomType) throws Exception {
        int available = inventory.getOrDefault(roomType, 0);
        if (available <= 0) throw new Exception("No rooms available for type: " + roomType);
        inventory.put(roomType, available - 1);
    }

    public synchronized void releaseRoom(String roomType) {
        inventory.put(roomType, inventory.getOrDefault(roomType, 0) + 1);
    }

    public Map<String, Integer> getInventoryStatus() {
        return Collections.unmodifiableMap(inventory);
    }
}

// Booking history with serializable state
class BookingHistory implements Serializable {
    private static final long serialVersionUID = 1L;
    private List<Reservation> confirmedReservations = new ArrayList<>();

    public void addReservation(Reservation res) {
        confirmedReservations.add(res);
        System.out.println("✅ Added to booking history: " + res.getReservationId());
    }

    public void cancelReservation(String reservationId, InventoryService inventoryService) throws Exception {
        boolean found = false;
        for (Reservation res : confirmedReservations) {
            if (res.getReservationId().equals(reservationId)) {
                if (res.isCancelled()) throw new Exception("Already cancelled: " + reservationId);
                res.cancel();
                inventoryService.releaseRoom(res.getRoomType());
                System.out.println("🛑 Reservation cancelled: " + reservationId);
                found = true;
                break;
            }
        }
        if (!found) throw new Exception("Reservation not found: " + reservationId);
    }

    public List<Reservation> getAllReservations() {
        return Collections.unmodifiableList(confirmedReservations);
    }
}

// Persistence service for saving and loading state
class PersistenceService {
    private static final String FILE_PATH = "book_my_stay_data.ser";

    public static void saveState(InventoryService inventory, BookingHistory history) {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(FILE_PATH))) {
            out.writeObject(inventory);
            out.writeObject(history);
            System.out.println("💾 System state saved successfully.");
        } catch (IOException e) {
            System.out.println("❌ Failed to save state: " + e.getMessage());
        }
    }

    public static Object[] loadState() {
        File file = new File(FILE_PATH);
        if (!file.exists()) {
            System.out.println("⚠️ No previous state found. Starting fresh.");
            return null;
        }
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
            InventoryService inventory = (InventoryService) in.readObject();
            BookingHistory history = (BookingHistory) in.readObject();
            System.out.println("📂 System state restored successfully.");
            return new Object[]{inventory, history};
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("❌ Failed to load state: " + e.getMessage());
            return null;
        }
    }
}

// Main application
public class BookMyStayApp {
    private static int reservationCounter = 101;

    public static void main(String[] args) {
        InventoryService inventory;
        BookingHistory history;

        // Restore previous state if available
        Object[] restored = PersistenceService.loadState();
        if (restored != null) {
            inventory = (InventoryService) restored[0];
            history = (BookingHistory) restored[1];
        } else {
            inventory = new InventoryService();
            history = new BookingHistory();
        }

        try {
            // Simulated bookings
            inventory.allocateRoom("STANDARD");
            history.addReservation(new Reservation("RES-" + reservationCounter++, "Alice", "STANDARD", "STA-101"));

            inventory.allocateRoom("DELUXE");
            history.addReservation(new Reservation("RES-" + reservationCounter++, "Bob", "DELUXE", "DEL-102"));

            // Simulated cancellation
            history.cancelReservation("RES-101", inventory);

        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }

        // Display final state
        System.out.println("\n--- Booking History ---");
        history.getAllReservations().forEach(System.out::println);

        System.out.println("\n--- Inventory Status ---");
        inventory.getInventoryStatus().forEach((type, count) -> System.out.println(type + ": " + count));

        // Persist current state
        PersistenceService.saveState(inventory, history);
    }
}