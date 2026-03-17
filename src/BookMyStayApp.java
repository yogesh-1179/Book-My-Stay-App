import java.util.*;

/*
 * Use Case 8: Booking History & Reporting
 * Maintains booking history and generates reports
 */

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

    public String getReservationId() {
        return reservationId;
    }

    public String getGuestName() {
        return guestName;
    }

    public String getRoomType() {
        return roomType;
    }

    public String getRoomId() {
        return roomId;
    }

    @Override
    public String toString() {
        return "Reservation ID: " + reservationId + ", Guest: " + guestName +
                ", Room Type: " + roomType + ", Room ID: " + roomId;
    }
}

class BookingHistory {

    // List to keep booking history in order
    private List<Reservation> confirmedReservations = new ArrayList<>();

    // Add a confirmed reservation to history
    public void addReservation(Reservation reservation) {
        confirmedReservations.add(reservation);
        System.out.println("Added to booking history: " + reservation.getReservationId());
    }

    // Retrieve all reservations
    public List<Reservation> getAllReservations() {
        return Collections.unmodifiableList(confirmedReservations);
    }
}

class BookingReportService {

    // Generate a summary report of bookings by room type
    public void generateRoomTypeReport(List<Reservation> reservations) {
        System.out.println("\nBooking Report: Reservations by Room Type");

        Map<String, Integer> countByRoomType = new HashMap<>();

        for (Reservation res : reservations) {
            countByRoomType.put(res.getRoomType(),
                    countByRoomType.getOrDefault(res.getRoomType(), 0) + 1);
        }

        for (String roomType : countByRoomType.keySet()) {
            System.out.println(roomType + ": " + countByRoomType.get(roomType));
        }
    }

    // Display all reservations for audit
    public void displayAllReservations(List<Reservation> reservations) {
        System.out.println("\nComplete Booking History:");

        if (reservations.isEmpty()) {
            System.out.println("No reservations found.");
            return;
        }

        for (Reservation res : reservations) {
            System.out.println(res);
        }
    }
}

public class BookMyStayApp {

    public static void main(String[] args) {

        BookingHistory bookingHistory = new BookingHistory();
        BookingReportService reportService = new BookingReportService();

        // Simulate confirmed reservations being added
        bookingHistory.addReservation(new Reservation("RES-101", "Alice", "STANDARD", "STA-1001"));
        bookingHistory.addReservation(new Reservation("RES-102", "Bob", "DELUXE", "DEL-1002"));
        bookingHistory.addReservation(new Reservation("RES-103", "Charlie", "STANDARD", "STA-1003"));
        bookingHistory.addReservation(new Reservation("RES-104", "David", "SUITE", "SUI-1004"));
        bookingHistory.addReservation(new Reservation("RES-105", "Emma", "STANDARD", "STA-1005"));

        // Admin requests booking history display
        List<Reservation> allReservations = bookingHistory.getAllReservations();
        reportService.displayAllReservations(allReservations);

        // Admin requests summary report
        reportService.generateRoomTypeReport(allReservations);
    }
}