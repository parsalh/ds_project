package gr.hua.dit.project.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestApiClientConfig {

    //TODO Get me from application properties!!!
    public static final String BASE_URL = "http://localhost:8080"; //no trailing slash

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

}
