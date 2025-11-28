package gr.hua.dit.project.core.repository;

import gr.hua.dit.project.core.model.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for managing {@link OrderItem} entity.
 */
@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem,Integer> {

    List<OrderItem> findAllByOrderId(final Long orderId);

    List<OrderItem> findAllByMenuItemId(final Long menuItemId);

    List<OrderItem> findAllByNameIgnoreCase(final String name);

    boolean existsByMenuItemId(final Long menuItemId);

}
