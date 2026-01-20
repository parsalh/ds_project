package gr.hua.dit.project.web.rest;

import gr.hua.dit.project.core.port.SmsNotificationPort;
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
    private final SmsNotificationPort smsNotificationPort;

    public SmsResource(final SmsNotificationPort smsNotificationPort) {
        if (smsNotificationPort == null) throw new NullPointerException();
        this.smsNotificationPort = smsNotificationPort;
    }

    @PostMapping(value = "/", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> sendSms(@RequestBody SendSmsRequest sendSmsRequest) {
        try {
            boolean sent = this.smsNotificationPort.sendSms(sendSmsRequest.e164(), sendSmsRequest.content());
            if (sent) {
                return ResponseEntity.ok("SENT");
            } else {
                return ResponseEntity.status(500).body("FAILED");
            }
        } catch (Exception ex) {
            LOGGER.error("Failed to send SMS {}", sendSmsRequest, ex);
            return ResponseEntity.status(500).body("FAILED");
        }
    }
}
