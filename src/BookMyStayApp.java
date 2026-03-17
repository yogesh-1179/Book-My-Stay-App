import java.util.*;

/*
 * Use Case 6: Reservation Confirmation & Room Allocation
 * Demonstrates safe room allocation using Queue, Set, and HashMap
 */

class BookingRequest {
    String guestName;
    String roomType;

    public BookingRequest(String guestName, String roomType) {
        this.guestName = guestName;
        this.roomType = roomType;
    }
}

class InventoryService {

    // Room type -> available count
    private Map<String, Integer> inventory = new HashMap<>();

    // Room type -> allocated room IDs
    private Map<String, Set<String>> allocatedRooms = new HashMap<>();

    // Global set to guarantee uniqueness
    private Set<String> allocatedRoomIds = new HashSet<>();

    public InventoryService() {
        inventory.put("STANDARD", 3);
        inventory.put("DELUXE", 2);
        inventory.put("SUITE", 1);

        allocatedRooms.put("STANDARD", new HashSet<>());
        allocatedRooms.put("DELUXE", new HashSet<>());
        allocatedRooms.put("SUITE", new HashSet<>());
    }

    public boolean allocateRoom(String roomType, String guestName) {

        int available = inventory.getOrDefault(roomType, 0);

        if (available <= 0) {
            System.out.println("❌ No rooms available for type: " + roomType);
            return false;
        }

        // Generate unique room ID
        String roomId = generateRoomId(roomType);

        // Ensure uniqueness
        if (allocatedRoomIds.contains(roomId)) {
            System.out.println("Duplicate room ID detected!");
            return false;
        }

        // Record allocation
        allocatedRoomIds.add(roomId);
        allocatedRooms.get(roomType).add(roomId);

        // Update inventory immediately
        inventory.put(roomType, available - 1);

        System.out.println("✅ Reservation Confirmed");
        System.out.println("Guest: " + guestName);
        System.out.println("Room Type: " + roomType);
        System.out.println("Room ID: " + roomId);
        System.out.println();

        return true;
    }

    private String generateRoomId(String roomType) {
        return roomType.substring(0, 3).toUpperCase() + "-" + UUID.randomUUID().toString().substring(0, 5);
    }
}

public class BookMyStayApp {

    public static void main(String[] args) {

        // FIFO Queue for booking requests
        Queue<BookingRequest> bookingQueue = new LinkedList<>();

        // Sample requests
        bookingQueue.add(new BookingRequest("Alice", "STANDARD"));
        bookingQueue.add(new BookingRequest("Bob", "DELUXE"));
        bookingQueue.add(new BookingRequest("Charlie", "STANDARD"));
        bookingQueue.add(new BookingRequest("David", "SUITE"));
        bookingQueue.add(new BookingRequest("Emma", "STANDARD"));

        InventoryService inventoryService = new InventoryService();

        System.out.println("Processing Booking Requests...\n");

        // Process requests FIFO
        while (!bookingQueue.isEmpty()) {

            BookingRequest request = bookingQueue.poll();

            System.out.println("Processing request for: " + request.guestName);

            inventoryService.allocateRoom(request.roomType, request.guestName);
        }

        System.out.println("All booking requests processed.");
    }
}