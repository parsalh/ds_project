package gr.hua.dit.project.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestApiClientConfig {

    @Value("${app.external-service.url}")
    private String externalServiceUrl;

    @Value("${app.external-service.api-key}")
    private String apiKey;

    @Bean
    public RestClient restClient(RestClient.Builder builder) {
        return builder
                .baseUrl(externalServiceUrl)
                .defaultHeader("SFGO-API-KEY", apiKey)
                .build();
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

}
