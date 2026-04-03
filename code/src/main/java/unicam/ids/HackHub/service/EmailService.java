package unicam.ids.HackHub.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    public void sendEmail(String to, String subject, String body) {
        logger.info("=============================================");
        logger.info("SIMULAZIONE INVIO EMAIL");
        logger.info("Destinatario: {}", to);
        logger.info("Oggetto: {}", subject);
        logger.info("Corpo del messaggio:\n{}", body);
        logger.info("=============================================");
    }
}
