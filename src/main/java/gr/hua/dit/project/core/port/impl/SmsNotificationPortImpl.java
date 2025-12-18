package gr.hua.dit.project.core.port.impl;

import gr.hua.dit.project.config.RestApiClientConfig;
import gr.hua.dit.project.core.port.SmsNotificationPort;
import gr.hua.dit.project.core.port.SmsService;
import gr.hua.dit.project.core.port.impl.dto.SendSmsRequest;
import gr.hua.dit.project.core.port.impl.dto.SendSmsResult;

import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;
import gr.hua.dit.project.core.port.SmsService;

import java.sql.SQLOutput;


/**
 * Default implementation of {@link SmsNotificationPort}, It uses the SMS external service.
 */
@Service
@Primary
public class SmsNotificationPortImpl implements SmsNotificationPort {

    private final RestClient restClient;

    private record ExternalSmsRequest(String to, String message){}

    public SmsNotificationPortImpl(RestClient.Builder builder) {
        this.restClient = builder.baseUrl("http://localhost:8081").build();
    }

    @Override
    public boolean sendSms(String e164, String content) {
        try {
            restClient.post()
                    .uri("api/external/sms/send")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new ExternalSmsRequest(e164,content))
                    .retrieve()
                    .toBodilessEntity();
            System.out.println("Main Project: SMS forwarded to Microservice for: " + e164);
            return true;
        } catch (Exception e) {
            System.err.println("Main Project: Failed to send SMS via Microservice: " + e.getMessage());
            return false;
        }
    }

}
