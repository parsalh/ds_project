package gr.hua.dit.project.core.port;

/**
 * Service for sending SMS.
 */
public interface SmsService {

    void send(String e164, String content);

}
