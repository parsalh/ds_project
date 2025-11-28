package gr.hua.dit.project.core.repository;

import gr.hua.dit.project.core.model.DayOfWeek;
import gr.hua.dit.project.core.model.OpenHour;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for managing {@link OpenHour} entity.
 */
@Repository
public interface OpenHourRepository extends JpaRepository<OpenHour, Long> {

    List<OpenHour> findAllByRestaurantId(final Long restaurantId);

    Optional<OpenHour> findByRestaurantIdAndDayOfWeek(final Long restaurantId, final DayOfWeek dayOfWeek);

    boolean existsByRestaurantIdAndDayOfWeek(final Long restaurantId, final DayOfWeek dayOfWeek);

    boolean existsByRestaurantId(final Long restaurantId);

}
