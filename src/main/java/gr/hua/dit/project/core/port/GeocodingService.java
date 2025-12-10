package gr.hua.dit.project.core.port;

import java.util.Optional;

/**
 * Service for geocoding addresses.
 */
public interface GeocodingService {

    Optional<double[]> getCoordinates(String address);

}
