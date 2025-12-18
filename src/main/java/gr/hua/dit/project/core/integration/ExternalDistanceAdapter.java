package gr.hua.dit.project.core.integration;

import gr.hua.dit.project.core.port.DistanceService;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Optional;

@Service
@Primary
public class ExternalDistanceAdapter implements DistanceService {

    private final RestClient restClient;

    private record ExternalRouteMetrics(double distanceMeters, double durationSeconds) {}

    public ExternalDistanceAdapter(RestClient.Builder builder){
        this.restClient = builder.baseUrl("http://localhost:8081").build();
    }

    @Override
    public Optional<RouteMetrics> getDistanceAndDuration(double startLat, double startLon,
                                                         double endLat, double endLon) {
        try{
            String uri = UriComponentsBuilder.fromPath("/api/external/route/calculate")
                    .queryParam("startLat", startLat)
                    .queryParam("startLon", startLon)
                    .queryParam("endLat", endLat)
                    .queryParam("endLon", endLon)
                    .toUriString();

            ExternalRouteMetrics response = restClient.get()
                    .uri(uri)
                    .retrieve()
                    .body(ExternalRouteMetrics.class);

            if (response != null){
                return Optional.of(new RouteMetrics(response.distanceMeters(), response.durationSeconds()));
            }
        } catch (Exception e){
            System.err.println("StreetFoodGo: Distance calculation via Microservice failed: " + e.getMessage());
        }
        return Optional.empty();
    }
}


