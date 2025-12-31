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

        // --- 1. Service Type Logic (Από τον συμφοιτητή σου) ---
        ServiceType type = request.serviceType();
        if (type == null) type = ServiceType.DELIVERY;
        order.setServiceType(type);

        // --- 2. Address Logic (ΠΡΟΣΑΡΜΟΣΜΕΝΟ για το νέο Address Object) ---
        Address deliveryAddr = new Address(); // Δημιουργούμε το αντικείμενο

        if (type == ServiceType.PICKUP) {
            deliveryAddr.setStreet("PICKUP"); // Βάζουμε το 'PICKUP' στο street
        } else {
            String addressStr = request.deliveryAddress();
            if (addressStr == null || addressStr.trim().isEmpty()) {
                addressStr = "Address not provided";
            }
            deliveryAddr.setStreet(addressStr); // Βάζουμε τη διεύθυνση στο street
        }
        // Αναθέτουμε το ΑΝΤΙΚΕΙΜΕΝΟ Address, όχι String
        order.setDeliveryAddress(deliveryAddr);


        // --- 3. Item Saving Logic (Από τον συμφοιτητή σου - Σημαντικό!) ---
        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal itemsTotal = BigDecimal.ZERO;

        if (request.items() != null && !request.items().isEmpty()) {
            for (CreateOrderItemRequest itemRequest : request.items()) {
                MenuItem menuItem = menuItemRepository.findById(itemRequest.menuItemId())
                        .orElseThrow(() -> new EntityNotFoundException("Menu item not found: " + itemRequest.menuItemId()));

                OrderItem orderItem = new OrderItem();

                // Προσοχή: Ελέγξετε αν στο OrderItem.java λέγεται setOrder ή setCustomerOrder
                orderItem.setCustomerOrder(order);
                orderItem.setMenuItem(menuItem);

                // Populate fields (Name, Price)
                orderItem.setName(menuItem.getName());
                orderItem.setPrice(menuItem.getPrice());

                int quantity = (itemRequest.quantity() != null && itemRequest.quantity() > 0) ? itemRequest.quantity() : 1;
                orderItem.setQuantity(quantity);

                // Calculate Subtotal
                BigDecimal lineTotal = menuItem.getPrice().multiply(BigDecimal.valueOf(quantity));
                orderItem.setSubtotal(lineTotal);

                // Add to list and running total
                orderItems.add(orderItem);
                itemsTotal = itemsTotal.add(lineTotal);
            }
        }

        order.setOrderItems(orderItems);

        // --- 4. Total Calculation (Από τον συμφοιτητή σου) ---
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