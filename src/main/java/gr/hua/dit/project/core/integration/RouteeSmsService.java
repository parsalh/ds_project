package gr.hua.dit.project.core.integration;

import gr.hua.dit.project.config.RouteeProperties;
import gr.hua.dit.project.core.port.SmsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestTemplate;

/**
 * AMD Routee {@link SmsService}
 */
public class RouteeSmsService implements SmsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RouteeSmsService.class);

    private static final String AUTHENTICATION_URL = "https;//auth.routee.net/oauth/token";
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



    @Override
    public void send(String e164, String content) {
        //TODO implement
    }
}
