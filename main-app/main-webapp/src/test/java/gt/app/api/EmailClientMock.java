package gt.app.api;

import gt.api.email.EmailDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class EmailClientMock extends EmailClient {

    @Override
    public ResponseEntity<Void> sendEmailWithAttachments(@Valid @NotNull EmailDto email) {
        return ResponseEntity.noContent().build();
    }
}
