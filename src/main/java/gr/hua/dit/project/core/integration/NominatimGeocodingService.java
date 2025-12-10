package gr.hua.dit.project.core.integration;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import gr.hua.dit.project.config.NominatimProperties;
import gr.hua.dit.project.core.port.GeocodingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Optional;

/**
 * Implementation of {@link GeocodingService} using Nominatim API.
 */
@Service
public class NominatimGeocodingService implements GeocodingService {

    private static final Logger LOGGER = LoggerFactory.getLogger(NominatimGeocodingService.class);
    private final RestTemplate restTemplate;
    private final NominatimProperties nominatimProperties;

    public NominatimGeocodingService(RestTemplate restTemplate,
                                     NominatimProperties nominatimProperties) {
        this.restTemplate = restTemplate;
        this.nominatimProperties = nominatimProperties;
    }

    /**
     * Internal DTO to map the JSON response from Nominatim.
     * Annotation to prevent UnrecognizedPropertyException.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    private record NominatimResult(String lat, String lon) {}

    /**
     * Retrieves the coordinates for a given address.
     */
    @Override
    public Optional<double[]> getCoordinates(String address) {
        try {
            // construct the API url
            URI url = UriComponentsBuilder.fromUriString(nominatimProperties.getUrl())
                    .queryParam("q",address)
                    .queryParam("format","json")
                    .queryParam("limit",1)
                    .build()
                    .toUri();

            // set header
            HttpHeaders headers = new HttpHeaders();
            headers.add("User-Agent",nominatimProperties.getUserAgent());
            HttpEntity<String> entity = new HttpEntity<>(headers);

            // GET request
            ResponseEntity<NominatimResult[]> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    NominatimResult[].class
            );

            // process response
            if (response.getBody() != null && response.getBody().length > 0) {
                LOGGER.debug("Received response from Nominatim: {}", response);
                NominatimResult firstResult = response.getBody()[0];
                double lat = Double.parseDouble(firstResult.lat());
                double lon = Double.parseDouble(firstResult.lon());
                return Optional.of(new double[]{lat,lon});
            } else {
                LOGGER.warn("Nominatim returned no results for address: {}", address);
            }
        } catch (Exception e) {
            LOGGER.error("Failed to geocode address: {}",address,e);
        }
        return Optional.empty();
    }

}
