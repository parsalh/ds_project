package gr.hua.dit.project.web.rest;

import gr.hua.dit.project.core.model.CustomerOrder;
import gr.hua.dit.project.core.model.OrderStatus;
import gr.hua.dit.project.core.model.Restaurant;
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

        CustomerOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        OrderStatus status = order.getOrderStatus();

        if (status != OrderStatus.ACCEPTED &&
            status != OrderStatus.IN_PROGRESS &&
            status != OrderStatus.READY_FOR_PICKUP &&
            status != OrderStatus.OUT_FOR_DELIVERY) {

            return ResponseEntity.ok(new OrderTrackingView(status,null,null,null));
        }

        Restaurant restaurant = order.getRestaurant();
        double restaurantLat = restaurant.getAddressInfo() != null ? restaurant.getAddressInfo().getLatitude() : 0.0;
        double restaurantLon = restaurant.getAddressInfo() != null ? restaurant.getAddressInfo().getLongitude() : 0.0;

        String customerAddressStr = getFullAddress(order.getDeliveryAddress());

        double[] customerCoords = geocodingService.getCoordinates(customerAddressStr)
                .orElse(new double[]{restaurantLat, restaurantLon});

        int eta = 20; //default
        var metrics = distanceService.getDistanceAndDuration(restaurantLat,restaurantLon,customerCoords[0],customerCoords[1]);

        if (metrics.isPresent()) {
            eta = (int) Math.round(metrics.get().durationSeconds()/60)+15;
        }

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
