package gr.hua.dit.project.core.port;

import java.util.Optional;

public interface DistanceService {

    /**
     * Calculates distances between two locations.
     * @return Distance and duration item.
     */
    Optional<RouteMetrics> getDistanceAndDuration(double startLat,
                                                  double startLon,
                                                  double endLat,
                                                  double endLon);

    // DTO
    record RouteMetrics(double distanceMeters, double durationSeconds) {}

}