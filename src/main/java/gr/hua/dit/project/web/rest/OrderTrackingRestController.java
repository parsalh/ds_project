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
@RequestMapping("/tracking-api")
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

        // ευρεση παραγγελιας
        CustomerOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        OrderStatus status = order.getOrderStatus();

        // ελεγχος αν η κατασταση επιτρεπει tracking
        // αν ειναι PENDING, REJECTED ή CANCELLED δεν δειχνουμε χαρτη/χρονο
        if (status != OrderStatus.ACCEPTED &&
                status != OrderStatus.IN_PROGRESS &&
                status != OrderStatus.READY_FOR_PICKUP &&
                status != OrderStatus.OUT_FOR_DELIVERY) {
            return ResponseEntity.ok(new OrderTrackingView(status,null,null,null));
        }

        // συντεταγμενες εστιατοριου
        Restaurant restaurant = order.getRestaurant();
        double restaurantLat = restaurant.getAddressInfo() != null ? restaurant.getAddressInfo().getLatitude() : 0.0;
        double restaurantLon = restaurant.getAddressInfo() != null ? restaurant.getAddressInfo().getLongitude() : 0.0;

        // συντεταγμενες πελατη και υπολογισμος ETA
        int eta;
        double[] customerCoords;

        if (order.getServiceType() == gr.hua.dit.project.core.model.ServiceType.PICKUP) {
            // pickup
            eta = 15;

            customerCoords = new double[]{restaurantLat, restaurantLon};

        } else {
            // delivery

            Double orderLat = null;
            Double orderLon = null;

            if (order.getDeliveryAddress() != null) {
                orderLat = order.getDeliveryAddress().getLatitude();
                orderLon = order.getDeliveryAddress().getLongitude();
            }

            // ελεγχος εγκυροτητας συντεταγμενων
            if (orderLat != null && orderLon != null && orderLat != 0.0) {
                customerCoords = new double[]{orderLat, orderLon};

                // υπολογισμος διαδρομης με ORS (external)
                var metrics = distanceService.getDistanceAndDuration(restaurantLat, restaurantLon, orderLat, orderLon);

                if (metrics.isPresent()) {
                    int travelTimeMins = (int) Math.round(metrics.get().durationSeconds() / 60.0);
                    eta = 20 + travelTimeMins;
                } else {
                    // fallback αν αποτυχει το routing service
                    eta = 30; // 15 prep + 15 average travel
                }
            } else {
                // fallback αν για καποιο λογο δεν υπαρχουν συντεταγμενες στην παραγγελια
                customerCoords = new double[]{restaurantLat, restaurantLon};
                eta = 30;
            }
        }

        return ResponseEntity.ok(new OrderTrackingView(
                status,
                eta,
                new double[]{restaurantLat,restaurantLon},
                customerCoords
        ));
    }

//    private String getFullAddress(gr.hua.dit.project.core.model.Address address) {
//        if (address == null) return "";
//        String street = address.getStreet() != null ? address.getStreet() : "";
//        String num = address.getNumber() != null ? " " + address.getNumber() : "";
//        String zip = address.getZipCode() != null ? ", " + address.getZipCode() : "";
//        return (street + num + zip).trim();
//    }
}
