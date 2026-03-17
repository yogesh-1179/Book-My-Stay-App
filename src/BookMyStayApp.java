import java.util.*;

// Custom exception for invalid booking operations
class InvalidBookingException extends Exception {
    public InvalidBookingException(String message) {
        super(message);
    }
}

// Represents a room booking/reservation
class Reservation {
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

// Validates booking input and system state
class InvalidBookingValidator {
    private static final Set<String> VALID_ROOM_TYPES = Set.of("STANDARD", "DELUXE", "SUITE");

    public static void validateRoomType(String roomType) throws InvalidBookingException {
        if (!VALID_ROOM_TYPES.contains(roomType)) {
            throw new InvalidBookingException("Invalid room type: " + roomType);
        }
    }

    public static void validateGuestName(String guestName) throws InvalidBookingException {
        if (guestName == null || guestName.isBlank()) {
            throw new InvalidBookingException("Guest name cannot be empty.");
        }
    }
}

// Manages room inventory and allocation
class InventoryService {
    private Map<String, Integer> inventory = new HashMap<>();
    private Stack<String> releasedRoomIds = new Stack<>();

    public InventoryService() {
        inventory.put("STANDARD", 3);
        inventory.put("DELUXE", 2);
        inventory.put("SUITE", 1);
    }

    public void checkAvailability(String roomType) throws InvalidBookingException {
        int available = inventory.getOrDefault(roomType, 0);
        if (available <= 0) {
            throw new InvalidBookingException("No rooms available for type: " + roomType);
        }
    }

    public String allocateRoom(String roomType) throws InvalidBookingException {
        checkAvailability(roomType);
        inventory.put(roomType, inventory.get(roomType) - 1);

        String roomId = roomType.substring(0,3).toUpperCase() + "-" + UUID.randomUUID().toString().substring(0,5);
        return roomId;
    }

    public void releaseRoom(String roomType, String roomId) {
        inventory.put(roomType, inventory.getOrDefault(roomType, 0) + 1);
        releasedRoomIds.push(roomId);
    }

    public Map<String, Integer> getInventoryStatus() {
        return Collections.unmodifiableMap(inventory);
    }

    public Stack<String> getReleasedRoomIds() {
        return releasedRoomIds;
    }
}

// Tracks confirmed bookings
class BookingHistory {
    private List<Reservation> confirmedReservations = new ArrayList<>();

    public void addReservation(Reservation reservation) {
        confirmedReservations.add(reservation);
        System.out.println("✅ Added to booking history: " + reservation.getReservationId());
    }

    public void cancelReservation(String reservationId, InventoryService inventoryService) throws InvalidBookingException {
        boolean found = false;

        for (Reservation res : confirmedReservations) {
            if (res.getReservationId().equals(reservationId)) {
                if (res.isCancelled()) {
                    throw new InvalidBookingException("Reservation already cancelled: " + reservationId);
                }
                res.cancel();
                inventoryService.releaseRoom(res.getRoomType(), res.getRoomId());
                System.out.println("🛑 Reservation cancelled: " + reservationId);
                found = true;
                break;
            }
        }

        if (!found) {
            throw new InvalidBookingException("Reservation not found: " + reservationId);
        }
    }

    public List<Reservation> getAllReservations() {
        return Collections.unmodifiableList(confirmedReservations);
    }
}

public class BookMyStayApp {

    private static int reservationCounter = 101;

    public static void main(String[] args) {

        InventoryService inventoryService = new InventoryService();
        BookingHistory bookingHistory = new BookingHistory();

        // Simulated bookings
        try {
            String roomId1 = inventoryService.allocateRoom("STANDARD");
            bookingHistory.addReservation(new Reservation("RES-" + reservationCounter++, "Alice", "STANDARD", roomId1));

            String roomId2 = inventoryService.allocateRoom("DELUXE");
            bookingHistory.addReservation(new Reservation("RES-" + reservationCounter++, "Bob", "DELUXE", roomId2));

            String roomId3 = inventoryService.allocateRoom("STANDARD");
            bookingHistory.addReservation(new Reservation("RES-" + reservationCounter++, "Charlie", "STANDARD", roomId3));

        } catch (InvalidBookingException e) {
            System.out.println("❌ Booking failed: " + e.getMessage());
        }

        System.out.println("\n--- Current Bookings ---");
        bookingHistory.getAllReservations().forEach(System.out::println);
        System.out.println("\n--- Inventory Status ---");
        inventoryService.getInventoryStatus().forEach((type, count) -> System.out.println(type + ": " + count));

        // Perform cancellation
        System.out.println("\n--- Processing Cancellations ---");
        try {
            bookingHistory.cancelReservation("RES-102", inventoryService); // Cancel Bob's booking
            bookingHistory.cancelReservation("RES-999", inventoryService); // Invalid reservation
        } catch (InvalidBookingException e) {
            System.out.println("❌ Cancellation failed: " + e.getMessage());
        }

        System.out.println("\n--- Final Booking History ---");
        bookingHistory.getAllReservations().forEach(System.out::println);

        System.out.println("\n--- Final Inventory Status ---");
        inventoryService.getInventoryStatus().forEach((type, count) -> System.out.println(type + ": " + count));

        System.out.println("\n--- Released Room IDs (Rollback Stack) ---");
        System.out.println(inventoryService.getReleasedRoomIds());
    }
}