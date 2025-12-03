package gr.hua.dit.project.web.rest;

import gr.hua.dit.project.core.port.SmsService;
import gr.hua.dit.project.core.port.impl.dto.SendSmsRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/v1/sms", produces =  MediaType.APPLICATION_JSON_VALUE)
public class SmsResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(SmsResource.class);
    private final SmsService smsService;

    public SmsResource(final SmsService smsService) {
        if (smsService == null) throw new NullPointerException();
        this.smsService = smsService;
    }

    @PostMapping(value = "/", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> sendSms(@RequestBody SendSmsRequest sendSmsRequest) {
        try {
            this.smsService.send(sendSmsRequest.e164(), sendSmsRequest.content());
            return ResponseEntity.ok("SENT");
        } catch (Exception ex) {
            LOGGER.error("Failed to send SMS {}", sendSmsRequest, ex);
            return ResponseEntity.status(500).body("FAILED");

        }
    }
}
