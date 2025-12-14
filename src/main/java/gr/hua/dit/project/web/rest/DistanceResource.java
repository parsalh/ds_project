package gr.hua.dit.project.web.rest;

import gr.hua.dit.project.core.port.DistanceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/distance")
public class DistanceResource {

    private final DistanceService distanceService;

    public DistanceResource(DistanceService distanceService) {
        this.distanceService = distanceService;
    }

    @GetMapping("/calculate")
    public ResponseEntity<DistanceService.RouteMetrics> calculateRoute(
            @RequestParam double startLat,
            @RequestParam double startLon,
            @RequestParam double endLat,
            @RequestParam double endLon
    ) {
        return distanceService.getDistanceAndDuration(startLat,startLon,endLat,endLon)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

}
