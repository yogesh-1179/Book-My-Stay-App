import java.util.*;

/*
 * Use Case 7: Add-On Service Selection
 * Demonstrates attaching optional services to reservations
 */

class Service {
    private String serviceName;
    private double cost;

    public Service(String serviceName, double cost) {
        this.serviceName = serviceName;
        this.cost = cost;
    }

    public String getServiceName() {
        return serviceName;
    }

    public double getCost() {
        return cost;
    }

    public String toString() {
        return serviceName + " ($" + cost + ")";
    }
}

class AddOnServiceManager {

    // reservationId -> list of services
    private Map<String, List<Service>> reservationServices = new HashMap<>();

    // Add service to reservation
    public void addService(String reservationId, Service service) {

        reservationServices
                .computeIfAbsent(reservationId, k -> new ArrayList<>())
                .add(service);

        System.out.println("Service added to reservation " + reservationId +
                ": " + service.getServiceName());
    }

    // Get services for reservation
    public List<Service> getServices(String reservationId) {
        return reservationServices.getOrDefault(reservationId, new ArrayList<>());
    }

    // Calculate total add-on cost
    public double calculateTotalCost(String reservationId) {

        List<Service> services = reservationServices.get(reservationId);

        if (services == null) return 0;

        double total = 0;

        for (Service s : services) {
            total += s.getCost();
        }

        return total;
    }

    // Display services for reservation
    public void displayServices(String reservationId) {

        List<Service> services = getServices(reservationId);

        System.out.println("\nServices for Reservation: " + reservationId);

        if (services.isEmpty()) {
            System.out.println("No add-on services selected.");
            return;
        }

        for (Service s : services) {
            System.out.println("- " + s);
        }

        System.out.println("Total Add-On Cost: $" + calculateTotalCost(reservationId));
    }
}

public class BookMyStayApp {

    public static void main(String[] args) {

        AddOnServiceManager manager = new AddOnServiceManager();

        // Example reservation IDs (created in previous use case)
        String reservation1 = "RES-101";
        String reservation2 = "RES-102";

        // Create services
        Service breakfast = new Service("Breakfast", 20);
        Service airportPickup = new Service("Airport Pickup", 50);
        Service spa = new Service("Spa Access", 40);
        Service extraBed = new Service("Extra Bed", 30);

        // Guest selects services
        manager.addService(reservation1, breakfast);
        manager.addService(reservation1, spa);

        manager.addService(reservation2, airportPickup);
        manager.addService(reservation2, extraBed);

        // Display selected services
        manager.displayServices(reservation1);
        manager.displayServices(reservation2);
    }
}