package gr.hua.dit.project.core.port;

import java.util.Optional;

/**
 * Service for geocoding addresses.
 */
public interface GeocodingService {

    Optional<double[]> getCoordinates(String address);

    Optional<String> getAddress(double lat, double lon);

}
