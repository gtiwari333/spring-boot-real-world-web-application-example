package gt.app.api;

import gt.api.email.EmailDto;
import gt.api.email.EmailService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@Slf4j
public class EmailClient implements EmailService {

    private final RestClient restClient;

    /**
     * For use by subclasses in tests.
     */
    protected EmailClient() {
        this.restClient = null;
    }

    public EmailClient(@Value("${feign-clients.email-service.url}") String baseUrl,
                       RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder.baseUrl(baseUrl).build();
    }

    @Override
    public ResponseEntity<Void> sendEmailWithAttachments(@Valid @NotNull EmailDto email) {
        return restClient.post()
            .uri("/sendEmail")
            .body(email)
            .retrieve()
            .toBodilessEntity();

    }
}

