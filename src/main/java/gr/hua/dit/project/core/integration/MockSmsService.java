package gr.hua.dit.project.core.integration;

import gr.hua.dit.project.core.port.SmsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Mock {@link SmsService}
 */
@Service
public class MockSmsService implements SmsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MockSmsService.class);

    @Override
    public void send(String e164, String content) {
        LOGGER.info("SENDING SMS {} {}", e164, content);
    }
}
