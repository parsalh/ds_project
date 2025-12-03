package gr.hua.dit.project.config;

import gr.hua.dit.project.core.integration.MockSmsService;
import gr.hua.dit.project.core.integration.RouteeSmsService;
import gr.hua.dit.project.core.port.SmsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.parameters.P;
import org.springframework.util.StringUtils;

/**
 * Factory for creating instances of {@link SmsService} based on application properties.
 */
@Configuration
public class SmsServiceFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(SmsServiceFactory.class);

    @Bean
    public SmsService smsService(final RouteeProperties routeeProperties,
                                 final RouteeSmsService routeeSmsService,
                                 final MockSmsService mockSmsService) {

        if (StringUtils.hasText(routeeProperties.getAppId()) && StringUtils.hasText(routeeProperties.getAppSecret())) {
            return routeeSmsService;
        } else {
            return mockSmsService;
        }

    }
}
