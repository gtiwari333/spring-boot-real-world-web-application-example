package gt.mail.modules.email;

import gt.api.email.EmailDto;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static gt.mail.utils.EmailUtil.toInetArray;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender javaMailSender;

    public void sendEmail(EmailDto email) {

        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            var message = new MimeMessageHelper(mimeMessage, true, StandardCharsets.UTF_8.name());

            message.setTo(toInetArray(email.to()));
            message.setCc(toInetArray(email.cc()));
            message.setBcc(toInetArray(email.bcc()));
            message.setFrom(email.fromEmail(), email.fromName());

            message.setSubject(email.subject());
            message.setText(email.content(), email.isHtml());

            if (email.files() != null) {
                for (var file : email.files()) {
                    message.addAttachment(file.filename(), new ByteArrayResource(file.data()));
                }
            }

            javaMailSender.send(mimeMessage);

            log.debug("Email Sent subject: {}", email.subject());
        } catch (IOException | MessagingException e) {
            throw new RuntimeException("Failed to send email", e);
        }
    }

}
