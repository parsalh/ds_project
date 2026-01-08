package gr.hua.dit.project.core.service.impl;

import gr.hua.dit.project.core.model.*;
import gr.hua.dit.project.core.port.DistanceService;
import gr.hua.dit.project.core.port.SmsNotificationPort;
import gr.hua.dit.project.core.repository.CustomerOrderRepository;
import gr.hua.dit.project.core.repository.MenuItemRepository;
import gr.hua.dit.project.core.repository.PersonRepository;
import gr.hua.dit.project.core.repository.RestaurantRepository;
import gr.hua.dit.project.core.service.CustomerOrderService;
import gr.hua.dit.project.core.service.mapper.CustomerOrderMapper;
import gr.hua.dit.project.core.service.model.CreateOrderItemRequest;
import gr.hua.dit.project.core.service.model.CreateOrderRequest;
import gr.hua.dit.project.core.service.model.CustomerOrderView;
import gr.hua.dit.project.core.security.CurrentUser;
import gr.hua.dit.project.core.security.CurrentUserProvider;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class CustomerOrderServiceImpl implements CustomerOrderService {

    private final CustomerOrderMapper customerOrderMapper;
    private final CustomerOrderRepository customerOrderRepository;
    private final PersonRepository personRepository;
    private final RestaurantRepository restaurantRepository;
    private final MenuItemRepository menuItemRepository;
    private final CurrentUserProvider currentUserProvider;
    private final SmsNotificationPort smsNotificationPort;
    private final DistanceService distanceService;

    public CustomerOrderServiceImpl(final CustomerOrderMapper customerOrderMapper,
                                    final CustomerOrderRepository customerOrderRepository,
                                    final PersonRepository personRepository,
                                    final RestaurantRepository restaurantRepository,
                                    final MenuItemRepository menuItemRepository,
                                    final CurrentUserProvider currentUserProvider,
                                    final SmsNotificationPort smsNotificationPort,
                                    final DistanceService distanceService) {
        this.customerOrderMapper = customerOrderMapper;
        this.customerOrderRepository = customerOrderRepository;
        this.personRepository = personRepository;
        this.restaurantRepository = restaurantRepository;
        this.menuItemRepository = menuItemRepository;
        this.currentUserProvider = currentUserProvider;
        this.smsNotificationPort = smsNotificationPort;
        this.distanceService = distanceService;
    }

    @Override
    public CustomerOrderView createOrder(final CreateOrderRequest request) {
        final CurrentUser currentUser = currentUserProvider.requireCurrentUser();

        if (currentUser.type() != PersonType.CUSTOMER) {
            throw new SecurityException("Only customers can create orders");
        }

        final Person customer = personRepository
                .findById(currentUser.id())
                .orElseThrow(() -> new EntityNotFoundException("Customer not found"));

        final Restaurant restaurant = restaurantRepository
                .findById(request.restaurantId())
                .orElseThrow(() -> new EntityNotFoundException("Restaurant not found"));

        if (!restaurant.isOpen()) {
            throw new IllegalStateException("This restaurant is closed.");
        }

        // Elenxoume tipo restaurant
        ServiceType requestedType = request.serviceType();
        if (requestedType == null) requestedType = ServiceType.DELIVERY; // Default

        ServiceType restaurantType = restaurant.getServiceType();

        if (requestedType == ServiceType.DELIVERY && restaurantType == ServiceType.PICKUP) {
            throw new IllegalArgumentException("This restaurant does not offer delivery.");
        }
        if (requestedType == ServiceType.PICKUP && restaurantType == ServiceType.DELIVERY) {
            throw new IllegalArgumentException("This restaurant does not offer pickup.");
        }

        if (requestedType == ServiceType.DELIVERY) {
            if (request.deliveryAddress() == null || request.deliveryAddress().trim().isEmpty()) {
                throw new IllegalArgumentException("Delivery address cannot be empty");
            }
        }

        final CustomerOrder order = new CustomerOrder();
        order.setCustomer(customer);
        order.setRestaurant(restaurant);
        order.setOrderStatus(OrderStatus.PENDING);
        order.setServiceType(requestedType);

        Address deliveryAddr = new Address();

        if (requestedType == ServiceType.PICKUP) {
            deliveryAddr.setStreet("PICKUP");
            if (restaurant.getAddressInfo() != null) {
                deliveryAddr.setLatitude(restaurant.getAddressInfo().getLatitude());
                deliveryAddr.setLongitude(restaurant.getAddressInfo().getLongitude());
            }
        } else {
            String requestAddrStr = request.deliveryAddress();
            deliveryAddr.setStreet(requestAddrStr);

            String targetAddr = requestAddrStr;

            customer.getAddresses().stream()
                    .filter(addr -> {
                        String addrStr = addr.getStreet() + " " +
                                (addr.getNumber() != null ? addr.getNumber() : "") + ", " +
                                addr.getZipCode();
                        return addrStr.equals(targetAddr) || targetAddr.contains(addr.getStreet());
                    })
                    .findFirst()
                    .ifPresent(matchedAddr -> {
                        deliveryAddr.setLatitude(matchedAddr.getLatitude());
                        deliveryAddr.setLongitude(matchedAddr.getLongitude());
                    });
        }
        order.setDeliveryAddress(deliveryAddr);

        // Elenxoume gia apostash
        if (requestedType == ServiceType.DELIVERY) {
            Address rAddr = restaurant.getAddressInfo();
            Address cAddr = order.getDeliveryAddress();

            if (rAddr != null && rAddr.getLatitude() != null && rAddr.getLongitude() != null &&
                    cAddr != null && cAddr.getLatitude() != null && cAddr.getLongitude() != null) {

                var metrics = distanceService.getDistanceAndDuration(
                        rAddr.getLatitude(), rAddr.getLongitude(),
                        cAddr.getLatitude(), cAddr.getLongitude()
                );

                if (metrics.isPresent()) {
                    if (metrics.get().distanceMeters() > 5000.0) {
                        throw new IllegalArgumentException("Delivery address is too far (" + (metrics.get().distanceMeters()/1000) + "km). Max allowed is 5km.");
                    }
                }
            }
        }

        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal itemsTotal = BigDecimal.ZERO;

        if (request.items() != null && !request.items().isEmpty()) {
            for (CreateOrderItemRequest itemRequest : request.items()) {
                MenuItem menuItem = menuItemRepository.findById(itemRequest.menuItemId())
                        .orElseThrow(() -> new EntityNotFoundException("Menu item not found: " + itemRequest.menuItemId()));

                // Ownership Check
                if (!menuItem.getRestaurant().getId().equals(restaurant.getId())) {
                    throw new IllegalArgumentException("Menu item '" + menuItem.getName() + "' does not belong to the selected restaurant.");
                }

                OrderItem orderItem = new OrderItem();
                orderItem.setCustomerOrder(order);
                orderItem.setMenuItem(menuItem);
                orderItem.setName(menuItem.getName());
                orderItem.setPrice(menuItem.getPrice());

                int quantity = (itemRequest.quantity() != null && itemRequest.quantity() > 0) ? itemRequest.quantity() : 1;
                orderItem.setQuantity(quantity);

                BigDecimal lineTotal = menuItem.getPrice().multiply(BigDecimal.valueOf(quantity));
                orderItem.setSubtotal(lineTotal);

                orderItems.add(orderItem);
                itemsTotal = itemsTotal.add(lineTotal);
            }
        }
        order.setOrderItems(orderItems);

        BigDecimal deliveryFee = BigDecimal.ZERO;
        if (requestedType == ServiceType.DELIVERY && restaurant.getDeliveryFee() != null) {
            deliveryFee = restaurant.getDeliveryFee();
        }

        BigDecimal finalTotalAmount = itemsTotal.add(deliveryFee);
        order.setTotalPrice(finalTotalAmount);

        if (restaurant.getMinimumOrderAmount() != null &&
                finalTotalAmount.compareTo(restaurant.getMinimumOrderAmount()) < 0) {
            throw new IllegalArgumentException("Order doesn't cover the minimum amount of " + restaurant.getMinimumOrderAmount() + " €");
        }

        final CustomerOrder savedOrder = customerOrderRepository.save(order);
        return customerOrderMapper.toView(savedOrder);
    }

    @Override
    public Optional<CustomerOrderView> getCustomerOrder(final Long id) {
        final CurrentUser currentUser = this.currentUserProvider.requireCurrentUser();
        final CustomerOrder customerOrder = this.customerOrderRepository.findById(id).orElse(null);

        if (customerOrder == null) return Optional.empty();

        long customerOrderPersonId;
        if (currentUser.type() == PersonType.OWNER) {
            customerOrderPersonId = customerOrder.getRestaurant().getOwner().getId();
        } else {
            customerOrderPersonId = customerOrder.getCustomer().getId();
        }

        if (currentUser.id() != customerOrderPersonId) {
            return Optional.empty();
        }

        return Optional.of(customerOrderMapper.toView(customerOrder));
    }

    @Override
    public List<CustomerOrderView> getMyOrders() {
        CurrentUser currentUser = currentUserProvider.requireCurrentUser();
        if (currentUser.type() != PersonType.CUSTOMER) {
            throw new SecurityException("Only customers have personal order history");
        }

        return customerOrderRepository.findAllByCustomerIdOrderByCreatedAtDesc(currentUser.id())
                .stream()
                .map(customerOrderMapper::toView)
                .collect(Collectors.toList());
    }

    @Override
    public void updateOrderStatus(Long orderId, OrderStatus status) {
        CurrentUser currentUser = currentUserProvider.requireCurrentUser();
        CustomerOrder order = customerOrderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found"));

        if (!order.getRestaurant().getOwner().getId().equals(currentUser.id())) {
            throw new SecurityException("You are not the owner of this order");
        }

        order.setOrderStatus(status);
        customerOrderRepository.save(order);

        String phoneNumber = order.getCustomer().getMobilePhoneNumber();
        String message = null;

        if (status == OrderStatus.ACCEPTED) message = "Your order #"+order.getId()+" has been accepted and is getting ready.";
        else if (status == OrderStatus.REJECTED) message = "Your order #"+order.getId()+" has been declined.";
        else if (status == OrderStatus.READY_FOR_PICKUP) message = "Your order #"+order.getId()+" is ready for pickup!";
        else if (status == OrderStatus.OUT_FOR_DELIVERY) message = "Your order #"+order.getId()+" is getting delivered to your location.";

        if (message != null) {
            try {
                smsNotificationPort.sendSms(phoneNumber, message);
            } catch (Exception e) {
                System.err.println("Failed to send SMS: " + e.getMessage());
            }
        }
    }
}