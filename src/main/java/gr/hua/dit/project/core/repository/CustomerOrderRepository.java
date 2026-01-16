package gr.hua.dit.project.core.repository;


import gr.hua.dit.project.core.model.CustomerOrder;
import gr.hua.dit.project.core.model.OrderStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 *  Repository for managing {@link CustomerOrder} entity.
 */
@Repository
public interface CustomerOrderRepository extends JpaRepository<CustomerOrder,Long> {

    @Override
    Optional<CustomerOrder> findById(Long id);

    List<CustomerOrder> findAllByCustomerIdOrderByCreatedAtDesc(final Long customerId);

    List<CustomerOrder> findAllByRestaurantIdOrderByCreatedAtDesc(final Long restaurantId);

    List<CustomerOrder> findAllByCustomerIdAndOrderStatusOrderByCreatedAtDesc(
            final Long customerId,
            final OrderStatus orderStatus);

    boolean existsByCustomerId(final Long customerId);

    boolean existsByCustomerIdAndOrderStatus(final Long customerId, final OrderStatus orderStatus);

    boolean existsByRestaurantIdAndOrderStatus(final Long restaurantId, final OrderStatus orderStatus);
}
