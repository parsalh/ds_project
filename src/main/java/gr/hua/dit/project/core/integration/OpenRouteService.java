package gr.hua.dit.project.core.integration;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import gr.hua.dit.project.config.OpenRouteServiceProperties;
import gr.hua.dit.project.core.port.DistanceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Optional;

@Service
public class OpenRouteService implements DistanceService {

    private static final Logger LOGGER = LoggerFactory.getLogger(OpenRouteService.class);

    private final RestTemplate restTemplate;
    private final OpenRouteServiceProperties properties;

    public OpenRouteService(RestTemplate restTemplate, OpenRouteServiceProperties properties) {
        this.restTemplate = restTemplate;
        this.properties = properties;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record OrsResponse(List<OrsFeature> features) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record OrsFeature(OrsProperties properties) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record OrsProperties(OrsSummary summary) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record OrsSummary(double distance, double duration) {}

    @Override
    public Optional<RouteMetrics> getDistanceAndDuration(double startLat,
                                                         double startLon,
                                                         double endLat,
                                                         double endLon) {

        try {
            String start = startLon + "," + startLat;
            String end = endLon + "," + endLat;

            URI url = UriComponentsBuilder.fromUriString(properties.getBaseUrl())
                    .queryParam("api_key", properties.getApiKey())
                    .queryParam("start", start)
                    .queryParam("end", end)
                    .build()
                    .toUri();

            ResponseEntity<OrsResponse> response = restTemplate.getForEntity(url, OrsResponse.class);

            if (response.getBody()!=null && !response.getBody().features().isEmpty()) {
                OrsSummary summary = response.getBody().features.get(0).properties().summary();

                LOGGER.info("Route calculated: {} meters, {} seconds", summary.distance(), summary.duration());

                return Optional.of(new RouteMetrics(summary.distance(), summary.duration()));
            }

        } catch (Exception e){
            LOGGER.error(e.getMessage());
        }
        return Optional.empty();
    }

}
