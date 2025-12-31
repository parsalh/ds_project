package gr.hua.dit.project.core.service.impl;

import gr.hua.dit.project.core.model.*;
import gr.hua.dit.project.core.repository.CustomerOrderRepository;
import gr.hua.dit.project.core.repository.PersonRepository;
import gr.hua.dit.project.core.repository.RestaurantRepository;
import gr.hua.dit.project.core.service.CustomerOrderService;
import gr.hua.dit.project.core.service.mapper.CustomerOrderMapper;
import gr.hua.dit.project.core.service.model.CreateOrderRequest;
import gr.hua.dit.project.core.service.model.CustomerOrderView;
import gr.hua.dit.project.core.security.CurrentUser;
import gr.hua.dit.project.core.security.CurrentUserProvider;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class CustomerOrderServiceImpl implements CustomerOrderService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomerOrderServiceImpl.class);

    private final CustomerOrderMapper customerOrderMapper;
    private final CustomerOrderRepository customerOrderRepository;
    private final PersonRepository personRepository;
    private final RestaurantRepository restaurantRepository;
    private final CurrentUserProvider currentUserProvider;

    public CustomerOrderServiceImpl(final CustomerOrderMapper customerOrderMapper,
                                    final CustomerOrderRepository customerOrderRepository,
                                    final PersonRepository personRepository,
                                    final RestaurantRepository restaurantRepository,
                                    final CurrentUserProvider currentUserProvider) {
        this.customerOrderMapper = customerOrderMapper;
        this.customerOrderRepository = customerOrderRepository;
        this.personRepository = personRepository;
        this.restaurantRepository = restaurantRepository;
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

        Address deliveryAddr = new Address();
        deliveryAddr.setStreet(request.deliveryAddress());
        order.setDeliveryAddress(deliveryAddr);
        order.setServiceType(ServiceType.DELIVERY);

        order.setTotalPrice(restaurant.getDeliveryFee());

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

        // Security: Ensure the current user owns the restaurant
        if (!order.getRestaurant().getOwner().getId().equals(currentUser.id())) {
            throw new SecurityException("You are not the owner of this order");
        }

        order.setOrderStatus(status);
        customerOrderRepository.save(order);
    }
}