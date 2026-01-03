package gr.hua.dit.project.web.rest;

import gr.hua.dit.project.core.model.CustomerOrder;
import gr.hua.dit.project.core.model.OrderStatus;
import gr.hua.dit.project.core.model.Restaurant;
import gr.hua.dit.project.core.model.ServiceType;
import gr.hua.dit.project.core.port.DistanceService;
import gr.hua.dit.project.core.port.GeocodingService;
import gr.hua.dit.project.core.repository.CustomerOrderRepository;
import gr.hua.dit.project.web.rest.dto.OrderTrackingView;
import org.hibernate.query.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/tracking")
public class OrderTrackingRestController {

    private final CustomerOrderRepository orderRepository;
    private final GeocodingService geocodingService;
    private final DistanceService distanceService;

    public OrderTrackingRestController(CustomerOrderRepository orderRepository,
                                       GeocodingService geocodingService,
                                       DistanceService distanceService) {
        this.orderRepository = orderRepository;
        this.geocodingService = geocodingService;
        this.distanceService = distanceService;
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderTrackingView> trackOrder(@PathVariable("orderId") Long orderId){

        // 1. Εύρεση Παραγγελίας
        CustomerOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        OrderStatus status = order.getOrderStatus();

        // 2. Έλεγχος αν η κατάσταση επιτρέπει Tracking
        // Αν είναι PENDING, REJECTED, ή CANCELLED δεν δείχνουμε χάρτη/χρόνο
        if (status != OrderStatus.ACCEPTED &&
                status != OrderStatus.IN_PROGRESS &&
                status != OrderStatus.READY_FOR_PICKUP &&
                status != OrderStatus.OUT_FOR_DELIVERY) {
            return ResponseEntity.ok(new OrderTrackingView(status,null,null,null));
        }

        // 3. Συντεταγμένες Εστιατορίου
        Restaurant restaurant = order.getRestaurant();
        double restaurantLat = restaurant.getAddressInfo() != null ? restaurant.getAddressInfo().getLatitude() : 0.0;
        double restaurantLon = restaurant.getAddressInfo() != null ? restaurant.getAddressInfo().getLongitude() : 0.0;

        // 4. Συντεταγμένες Πελάτη & Υπολογισμός ETA
        int eta;
        double[] customerCoords;

        if (order.getServiceType() == gr.hua.dit.project.core.model.ServiceType.PICKUP) {
            // --- ΠΕΡΙΠΤΩΣΗ PICKUP ---
            // Ο χρόνος είναι σταθερός (μόνο προετοιμασία)
            eta = 15;

            // Για Pickup, βάζουμε τις συντεταγμένες του εστιατορίου και στον "πελάτη"
            // ώστε ο χάρτης να ζεντράρει στο μαγαζί και να μην ψάχνει διαδρομές στο κενό.
            customerCoords = new double[]{restaurantLat, restaurantLon};

        } else {
            // --- ΠΕΡΙΠΤΩΣΗ DELIVERY ---

            // Διαβάζουμε τις συντεταγμένες απευθείας από την παραγγελία (τις αποθηκεύσαμε στο createOrder)
            Double orderLat = null;
            Double orderLon = null;

            if (order.getDeliveryAddress() != null) {
                orderLat = order.getDeliveryAddress().getLatitude();
                orderLon = order.getDeliveryAddress().getLongitude();
            }

            // Έλεγχος εγκυρότητας συντεταγμένων
            if (orderLat != null && orderLon != null && orderLat != 0.0) {
                customerCoords = new double[]{orderLat, orderLon};

                // Υπολογισμός Διαδρομής μέσω External Service (OpenRouteService)
                var metrics = distanceService.getDistanceAndDuration(restaurantLat, restaurantLon, orderLat, orderLon);

                if (metrics.isPresent()) {
                    // ETA = 15 λεπτά (προετοιμασία) + Χρόνος Διαδρομής
                    int travelTimeMins = (int) Math.round(metrics.get().durationSeconds() / 60.0);
                    eta = 15 + travelTimeMins;
                } else {
                    // Fallback αν αποτύχει το Routing Service
                    eta = 30; // 15 prep + 15 average travel
                }
            } else {
                // Fallback αν για κάποιο λόγο δεν υπάρχουν συντεταγμένες στην παραγγελία
                customerCoords = new double[]{restaurantLat, restaurantLon};
                eta = 30;
            }
        }

        // 5. Επιστροφή Αποτελέσματος
        return ResponseEntity.ok(new OrderTrackingView(
                status,
                eta,
                new double[]{restaurantLat,restaurantLon},
                customerCoords
        ));
    }

    private String getFullAddress(gr.hua.dit.project.core.model.Address address) {
        if (address == null) return "";
        String street = address.getStreet() != null ? address.getStreet() : "";
        String num = address.getNumber() != null ? " " + address.getNumber() : "";
        String zip = address.getZipCode() != null ? ", " + address.getZipCode() : "";
        return (street + num + zip).trim();
    }
}
