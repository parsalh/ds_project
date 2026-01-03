package gr.hua.dit.project.core.service.impl;

import gr.hua.dit.project.core.model.*;
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

    public CustomerOrderServiceImpl(final CustomerOrderMapper customerOrderMapper,
                                    final CustomerOrderRepository customerOrderRepository,
                                    final PersonRepository personRepository,
                                    final RestaurantRepository restaurantRepository,
                                    final MenuItemRepository menuItemRepository,
                                    final CurrentUserProvider currentUserProvider) {
        this.customerOrderMapper = customerOrderMapper;
        this.customerOrderRepository = customerOrderRepository;
        this.personRepository = personRepository;
        this.restaurantRepository = restaurantRepository;
        this.menuItemRepository = menuItemRepository;
        this.currentUserProvider = currentUserProvider;
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

        final CustomerOrder order = new CustomerOrder();
        order.setCustomer(customer);
        order.setRestaurant(restaurant);
        order.setOrderStatus(OrderStatus.PENDING);

        ServiceType type = request.serviceType();
        if (type == null) type = ServiceType.DELIVERY;
        order.setServiceType(type);

        Address deliveryAddr = new Address();

        if (type == ServiceType.PICKUP) {
            deliveryAddr.setStreet("PICKUP");
            if (restaurant.getAddressInfo() != null) {
                deliveryAddr.setLatitude(restaurant.getAddressInfo().getLatitude());
                deliveryAddr.setLongitude(restaurant.getAddressInfo().getLongitude());
            }
        } else {
            String requestAddrStr = request.deliveryAddress();
            if (requestAddrStr == null || requestAddrStr.trim().isEmpty()) {
                requestAddrStr = "Address not provided";
            }
            deliveryAddr.setStreet(requestAddrStr);

            if (request.deliveryAddress() != null) {
                String targetAddr = request.deliveryAddress();

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
        }
        order.setDeliveryAddress(deliveryAddr);

        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal itemsTotal = BigDecimal.ZERO;

        if (request.items() != null && !request.items().isEmpty()) {
            for (CreateOrderItemRequest itemRequest : request.items()) {
                MenuItem menuItem = menuItemRepository.findById(itemRequest.menuItemId())
                        .orElseThrow(() -> new EntityNotFoundException("Menu item not found: " + itemRequest.menuItemId()));

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
        if (type == ServiceType.DELIVERY && restaurant.getDeliveryFee() != null) {
            deliveryFee = restaurant.getDeliveryFee();
        }
        order.setTotalPrice(itemsTotal.add(deliveryFee));

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
    }
}