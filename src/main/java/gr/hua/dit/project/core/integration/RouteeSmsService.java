package gr.hua.dit.project.core.integration;

import gr.hua.dit.project.config.RouteeProperties;
import gr.hua.dit.project.core.port.SmsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.print.attribute.standard.Media;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * AMD Routee {@link SmsService}
 */
@Service
public class RouteeSmsService implements SmsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RouteeSmsService.class);

    private static final String AUTHENTICATION_URL = "https://auth.routee.net/oauth/token";
    private static final String SMS_URL = "https://connect.routee.net/sms";

    // Beans:
    private final RestTemplate restTemplate;
    private final RouteeProperties routeeProperties;

    public RouteeSmsService(final RestTemplate restTemplate, final RouteeProperties routeeProperties) {
        if (restTemplate == null) throw new NullPointerException();
        if (routeeProperties == null) throw new NullPointerException();

        this.restTemplate = restTemplate;
        this.routeeProperties = routeeProperties;
    }

    private String getAccessToken(){
        final String credentials = this.routeeProperties.getAppId() + ":" + this.routeeProperties.getAppSecret();
        final String encoded = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));

        final HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("Authorization", "Basic " + encoded);
        httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        final HttpEntity<String> request = new HttpEntity<>("grant_type=client_credentials", httpHeaders);

        final ResponseEntity<Map> response =
                this.restTemplate.exchange(AUTHENTICATION_URL, HttpMethod.POST, request, Map.class);

        return (String) response.getBody().get("access_token");

    }

//    @Override
//    public void send(String e164, String content) {
//
//        // Authenticate
//        // ----------------------------------------------------
//
//        final String token = this.getAccessToken();
//
//        // Headers
//        // ----------------------------------------------------
//
//        HttpHeaders httpHeaders = new HttpHeaders();
//        httpHeaders.set("Authorization", "Bearer " + token);
//        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
//
//        // Payload
//        // ----------------------------------------------------
//
//        Map<String, Object> body = Map.of(
//                "body", content,
//                "to", e164,
//                "from", this.routeeProperties.getSender());
//
//        // Request
//        // ----------------------------------------------------
//
//        final HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, httpHeaders);
//        final ResponseEntity<String> response = this.restTemplate.postForEntity(SMS_URL, entity, String.class);
//
//        LOGGER.info("Routee response: {}", response);
//
//        if (!response.getStatusCode().is2xxSuccessful()) {
//            throw new RuntimeException(String.format("SMS send to %s failed",e164));
//        }
//
//        //TODO implement
//    }

    @Override
    public void send(String e164, String content) { // <--- Δέχεται Strings, επιστρέφει void
        LOGGER.info("Attempting to send SMS via Routee...");

        try {
            final String token = this.getAccessToken();

            final HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.set("Authorization", "Bearer " + token);
            httpHeaders.setContentType(MediaType.APPLICATION_JSON);

            final Map<String, Object> body = Map.of(
                    "body", content,
                    "to", e164,
                    "from", this.routeeProperties.getSender());

            final HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, httpHeaders);

            // Κλήση στο Routee
            final ResponseEntity<String> response = this.restTemplate.postForEntity(SMS_URL, entity, String.class);

            // ΑΝ ΑΠΟΤΥΧΕΙ, ΠΕΤΑΜΕ EXCEPTION (γιατί δεν μπορούμε να επιστρέψουμε false)
            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("Routee API returned error: " + response.getStatusCode());
            }

            LOGGER.info("SMS sent successfully!");

        } catch (Exception e) {
            LOGGER.error("Failed to send SMS via Routee", e);
            // Ξαναπετάμε το exception για να το καταλάβει ο caller
            throw new RuntimeException(e);
        }
    }

}
