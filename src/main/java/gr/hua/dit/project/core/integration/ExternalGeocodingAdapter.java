package gr.hua.dit.project.core.integration;

import gr.hua.dit.project.core.port.GeocodingService;
import org.apache.tomcat.util.descriptor.web.SecurityRoleRef;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Optional;

@Service
public class ExternalGeocodingAdapter implements GeocodingService {

    private final RestClient restClient;

    private record ExternalCoordinates(double lat, double lon) {}
    private record ExternalAddressResult(String address) {}

    public ExternalGeocodingAdapter(RestClient.Builder builder) {
        this.restClient = builder.baseUrl("http://localhost:8081").build();
    }

    @Override
    public Optional<double[]> getCoordinates(String address) {
        try {
            String uri = UriComponentsBuilder.fromPath("/api/external/geo/search")
                    .queryParam("address", address)
                    .toUriString();

            ExternalCoordinates response = restClient.get()
                    .uri(uri)
                    .retrieve()
                    .body(ExternalCoordinates.class);

            if (response != null) {
                return Optional.of(new double[]{response.lat(), response.lon()});
            }
        } catch (Exception e) {
            System.err.println("StreetFoodGo: Geocoding via Microservice failed:"+e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public Optional<String> getAddress(double lat, double lon) {
        try{
            String uri = UriComponentsBuilder.fromPath("/api/external/geo/reverse")
                    .queryParam("lat",lat)
                    .queryParam("lon",lon)
                    .toUriString();

            ExternalAddressResult response = restClient.get()
                    .uri(uri)
                    .retrieve()
                    .body(ExternalAddressResult.class);

            if (response!=null && response.address()!=null) {
                return Optional.of(response.address());
            }
        } catch (Exception e) {
            System.err.println("StreetFoodGo: Reverse Geocoding via Microservice failed:"+e.getMessage());
        }
        return Optional.empty();
    }

}
