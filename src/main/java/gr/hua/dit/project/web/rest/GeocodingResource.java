package gr.hua.dit.project.web.rest;

import gr.hua.dit.project.core.port.GeocodingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/geocoding")
public class GeocodingResource {

    private final GeocodingService geocodingService;

    public GeocodingResource(GeocodingService geocodingService) {
        this.geocodingService = geocodingService;
    }

    record GeoResult(String address,double latitude,double longitude){}

    @GetMapping("/search")
    public ResponseEntity<?> getCoordinates(@RequestParam String address){
        Optional<double[]> coordinates =  geocodingService.getCoordinates(address);

        if (coordinates.isPresent()){
            double[] coords = coordinates.get();
            return ResponseEntity.ok(new GeoResult(address,coords[0],coords[1]));
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
