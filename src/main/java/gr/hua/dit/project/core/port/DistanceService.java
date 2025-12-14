package gr.hua.dit.project.core.port;

import java.util.Optional;

public interface DistanceService {

    Optional<RouteMetrics> getDistanceAndDuration(double startLat,
                                                  double startLon,
                                                  double endLat,
                                                  double endLon);

    record RouteMetrics(double distanceMeters, double durationSeconds) {}

}
