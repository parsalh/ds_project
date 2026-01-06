package gr.hua.dit.project.core.repository;

import gr.hua.dit.project.core.model.Cuisine;
import gr.hua.dit.project.core.model.Restaurant;
import gr.hua.dit.project.core.model.ServiceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Repository for managing {@link Restaurant} entity.
 */
@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant,Long> {

    Optional<Restaurant> findByNameIgnoreCase(final String name);

    boolean existsByNameIgnoreCase(final String name);

    List<Restaurant> findAllByOwnerId(final Long id);

    List<Restaurant> findByNameContainingIgnoreCase(final String partialName);

    List<Restaurant> findAllByServiceType(final ServiceType serviceType);

    List<Restaurant> findAllByDeliveryFeeLessThanEqual(final BigDecimal maxDeliveryFee);

    List<Restaurant> findAllByCuisinesContaining(Cuisine cuisine);

    boolean existsByOwnerId(final Long ownerId);

    boolean existsByServiceType(final ServiceType serviceType);

    List<Restaurant> findAllByAddressInfoLatitudeBetweenAndAddressInfoLongitudeBetween(
            Double minLat, Double maxLat, Double minLon, Double maxLon);

}
