package gr.hua.dit.project.config;

import gr.hua.dit.project.core.integration.MockSmsService;
import gr.hua.dit.project.core.integration.RouteeSmsService;
import gr.hua.dit.project.core.port.SmsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

/**
 * Class (configuration) for selecting the instance of {@link SmsService} based on application properties.
 */
@Configuration
public class SmsServiceSelector {

    private static final Logger LOGGER = LoggerFactory.getLogger(SmsServiceSelector.class);

    @Bean
    public SmsService smsService(final RouteeProperties routeeProperties,
                                 final RouteeSmsService routeeSmsService,
                                 final MockSmsService mockSmsService) {

        // --- DEBUG ---
        System.out.println("--------------------------------------------------");
        System.out.println("DEBUG: Routee App ID: '" + routeeProperties.getAppId() + "'");
        System.out.println("DEBUG: Routee Secret: '" + routeeProperties.getAppSecret() + "'");
        System.out.println("--------------------------------------------------");
        // --------------------

        if (StringUtils.hasText(routeeProperties.getAppId()) && StringUtils.hasText(routeeProperties.getAppSecret())) {
            return routeeSmsService;
        } else {
            return mockSmsService;
        }

    }
}
