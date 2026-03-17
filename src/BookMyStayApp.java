import java.util.*;

// Custom exception for invalid bookings
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

// Validates booking inputs
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

// Manages room inventory safely
class InventoryService {

    private Map<String, Integer> inventory = new HashMap<>();

    public InventoryService() {
        inventory.put("STANDARD", 3);
        inventory.put("DELUXE", 2);
        inventory.put("SUITE", 1);
    }

    public void checkAvailability(String roomType) throws InvalidBookingException {
        int available = inventory.getOrDefault(roomType, 0);
        if (available <= 0) {
            throw new InvalidBookingException("No available rooms for type: " + roomType);
        }
    }

    public void allocateRoom(String roomType) throws InvalidBookingException {
        checkAvailability(roomType);
        inventory.put(roomType, inventory.get(roomType) - 1);

        if (inventory.get(roomType) < 0) {
            throw new InvalidBookingException("Inventory cannot be negative for " + roomType);
        }
    }

    public Map<String, Integer> getInventoryStatus() {
        return Collections.unmodifiableMap(inventory);
    }
}

// Manages booking history safely
class BookingHistory {

    private List<Reservation> confirmedReservations = new ArrayList<>();

    public void addReservation(Reservation reservation) {
        confirmedReservations.add(reservation);
        System.out.println("✅ Added to booking history: " + reservation.getReservationId());
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

        // Sample booking requests (some invalid)
        List<Map<String, String>> bookingRequests = List.of(
                Map.of("guestName", "Alice", "roomType", "STANDARD"),
                Map.of("guestName", "Bob", "roomType", "DELUXE"),
                Map.of("guestName", "", "roomType", "STANDARD"),         // Invalid guest name
                Map.of("guestName", "Charlie", "roomType", "PENTHOUSE") // Invalid room type
        );

        System.out.println("Processing Booking Requests...\n");

        for (Map<String, String> request : bookingRequests) {
            String guestName = request.get("guestName");
            String roomType = request.get("roomType");

            try {
                // Validate inputs
                InvalidBookingValidator.validateGuestName(guestName);
                InvalidBookingValidator.validateRoomType(roomType);

                // Allocate room safely
                inventoryService.allocateRoom(roomType);

                // Create reservation
                String reservationId = "RES-" + reservationCounter++;
                String roomId = roomType.substring(0,3).toUpperCase() + "-" + UUID.randomUUID().toString().substring(0,5);
                Reservation reservation = new Reservation(reservationId, guestName, roomType, roomId);

                // Add to booking history
                bookingHistory.addReservation(reservation);

                System.out.println("Reservation successful for " + guestName + "\n");

            } catch (InvalidBookingException e) {
                System.out.println("❌ Booking failed for " + guestName + ": " + e.getMessage() + "\n");
            } catch (Exception e) {
                System.out.println("❌ Unexpected error: " + e.getMessage() + "\n");
            }
        }

        // Display final booking history
        System.out.println("\nFinal Booking History:");
        bookingHistory.getAllReservations().forEach(System.out::println);

        System.out.println("\nCurrent Inventory Status:");
        inventoryService.getInventoryStatus().forEach((type, count) ->
                System.out.println(type + ": " + count + " rooms available"));
    }
}