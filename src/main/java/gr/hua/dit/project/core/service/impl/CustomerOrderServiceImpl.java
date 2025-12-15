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
        // 1. Get current authenticated user
        final CurrentUser currentUser = currentUserProvider.requireCurrentUser(); // Fixed: added ()

        // 2. Security Check: Only customers can create orders
        if (currentUser.type() != PersonType.CUSTOMER) {
            throw new SecurityException("Only customers can create orders");
        }

        // 3. Fetch the Customer (Person entity)
        final Person customer = personRepository
                .findById(currentUser.id())
                .orElseThrow(() -> new EntityNotFoundException("Customer not found"));

        // 4. Fetch the Restaurant (Use RestaurantRepository, not OwnerRepository)
        // Assuming request.restaurantId() exists. If you had 'ownerId' before, change it to restaurantId.
        final Restaurant restaurant = restaurantRepository
                .findById(request.restaurantId())
                .orElseThrow(() -> new EntityNotFoundException("Restaurant not found"));

        // 5. Create the Order
        final CustomerOrder order = new CustomerOrder(); // Fixed: Order -> CustomerOrder
        order.setCustomer(customer);
        order.setRestaurant(restaurant); // Fixed: setRestaurant instead of setOwner
        order.setOrderStatus(OrderStatus.PENDING);
        // Initialize other fields if necessary (e.g., totalPrice, items, address)
        // order.setDeliveryAddress(customer.getAddress());

        // 6. Save and Return
        final CustomerOrder savedOrder = customerOrderRepository.save(order);
        return customerOrderMapper.toView(savedOrder);
    }

    @Override
    public Optional<CustomerOrderView> getCustomerOrder(final Long id) {
        if (id == null) throw new NullPointerException();
        if (id <= 0) throw new IllegalArgumentException();

        final CurrentUser currentUser = this.currentUserProvider.requireCurrentUser(); // Fixed typo

        // 1. Fetch Order
        final CustomerOrder customerOrder;
        try {
            // Using findById is safer than getReferenceById if you plan to access properties immediately
            customerOrder = this.customerOrderRepository.findById(id).orElseThrow(EntityNotFoundException::new);
        } catch (EntityNotFoundException ignored) {
            return Optional.empty();
        }

        // 2. Security: Check if the user owns this order
        final long customerOrderPersonId;

        if (currentUser.type() == PersonType.OWNER) {
            // If Owner: Check if they own the restaurant associated with this order
            customerOrderPersonId = customerOrder.getRestaurant().getOwner().getId(); // Fixed path to Owner
        } else {
            // If Customer: Check if they placed the order
            customerOrderPersonId = customerOrder.getCustomer().getId();
        }

        if (currentUser.id() != customerOrderPersonId) {
            // Return empty or throw SecurityException if they try to view someone else's order
            return Optional.empty();
        }

        // 3. Convert to View
        return Optional.of(customerOrderMapper.toView(customerOrder));
    }
}
