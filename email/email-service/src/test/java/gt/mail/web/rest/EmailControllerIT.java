package gt.mail.web.rest;

import gt.api.email.EmailDto;
import gt.mail.frwk.TestContainerConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestContainerConfig.class)
class EmailControllerIT {

    @LocalServerPort
    int port;

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.mail.host", () -> TestContainerConfig.mailhog.getHost());
        registry.add("spring.mail.port", () -> TestContainerConfig.mailhog.getMappedPort(1025));
    }

    @Test
    void sendEmail() {
        var email = new EmailDto(
            "sender@example.com",
            "Sender Name",
            List.of("recipient1@example.com", "recipient2@example.com"),
            List.of("cc@example.com"),
            List.of("bcc@example.com"),
            "Integration Test Subject",
            "<h1>Hello from integration test</h1>",
            true,
            new EmailDto.FileBArray[]{
                new EmailDto.FileBArray("test file content".getBytes(StandardCharsets.UTF_8), "test-attachment.txt")
            }
        );

        var restClient = RestClient.create("http://localhost:" + port);
        var response = restClient.post()
            .uri("/sendEmail")
            .contentType(MediaType.APPLICATION_JSON)
            .body(email)
            .retrieve()
            .toBodilessEntity();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
