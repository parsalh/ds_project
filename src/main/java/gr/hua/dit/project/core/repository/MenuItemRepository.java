package gr.hua.dit.project.core.repository;

import gr.hua.dit.project.core.model.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.List;

/**
 * Repository for managing {@link MenuItem} entity.
 */
@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem,Long> {

    @Override
    Optional<MenuItem> findById(Long id);

    Optional<MenuItem> findByNameIgnoreCase(String name);

    Optional<MenuItem> findByNameIgnoreCaseAndRestaurantId(String name, Long restaurantId);

    List<MenuItem> findAllByRestaurantId(final Long restaurantId);

    List<MenuItem> findAllByRestaurantIdAndAvailable(final Long restaurantId, final Boolean available);

    List<MenuItem> findAllByRestaurantIdAndPriceLessThanEqual(final Long restaurantId, final BigDecimal maxPrice);

    List<MenuItem> findByRestaurantIdAndNameContainingIgnoreCase(final Long restaurantId, final String partialName);

    boolean existsByNameIgnoreCaseAndRestaurantIdAndIdNot(final String name, final Long restaurantId, final Long itemId);

}
