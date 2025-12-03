package gr.hua.dit.project.core.port.impl;

import gr.hua.dit.project.config.RestApiClientConfig;
import gr.hua.dit.project.core.port.SmsNotificationPort;
import gr.hua.dit.project.core.port.SmsService;
import gr.hua.dit.project.core.port.impl.dto.SendSmsRequest;
import gr.hua.dit.project.core.port.impl.dto.SendSmsResult;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;


/**
 * Default implementation of {@link SmsNotificationPort}, It uses the NOC external service.
 */
@Service
public class SmsNotificationPortImpl implements SmsNotificationPort {

//    private final RestTemplate restTemplate;
    private final SmsService smsService;

    public SmsNotificationPortImpl(final SmsService smsService) {
        this.smsService = smsService;
    }

//    @Override
//    public boolean sendSms(final String e164, final String content) {
//        if (e164 == null) throw new NullPointerException();
//        if (e164.isBlank()) throw new IllegalArgumentException();
//        if (content == null) throw new NullPointerException();
//        if (content.isBlank()) throw new IllegalArgumentException();
//
//        // HTTP Headers
//        // ---------------------------------------------------------
//
//        final HttpHeaders httpHeaders = new HttpHeaders();
//        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
//
//        // ---------------------------------------------------------
//
//        final SendSmsRequest body = new SendSmsRequest(e164, content);
//
//        // Alternative ???
//        // Map<String, Object> body = Map.of("e164",e164,"content",content);
//
//        // ---------------------------------------------------------
//
//        final String baseUrl = RestApiClientConfig.BASE_URL;
//        final String url = baseUrl + "/api/v1/sms";
//        final HttpEntity<SendSmsRequest> entity = new HttpEntity<>(body, httpHeaders); // request
//        final ResponseEntity<SendSmsResult> response = this.restTemplate.postForEntity(url, entity, SendSmsResult.class);
//
//        if (response.getStatusCode().is2xxSuccessful()) {
//            final SendSmsResult sendSmsResult = response.getBody();
//            if (sendSmsResult == null) throw new NullPointerException();
//            return sendSmsResult.sent();
//        }
//
//        throw new RuntimeException("External service responded with " + response.getStatusCode());
//
//    }

    @Override
    public boolean sendSms(final String e164, final String content) {
        try {
            this.smsService.send(e164, content);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
