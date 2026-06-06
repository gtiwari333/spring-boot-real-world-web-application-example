package gt.mail.web.rest;

import gt.mail.frwk.TestContainerConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestContainerConfig.class)
@DirtiesContext
class EmailResourceIT {

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.mail.host", () -> TestContainerConfig.mailhog.getHost());
        registry.add("spring.mail.port", () -> TestContainerConfig.mailhog.getMappedPort(1025));
    }

    @Test
    void canSendEmail(@Autowired MockMvc mvc) throws Exception {

        mvc.perform(
                post("/sendEmail")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(sampleEmail().getBytes()))
            .andExpect(status().isOk());
    }

    String sampleEmail() {
        return """
            {
                "fromEmail":"test@email.com",
                "subject":"Test",
                "content":"Body",
                "isHtml":false,
                "files":[],
                "to":["recep@emai.com"]
                ,"cc":[],
                "bcc":[]
            }
            """;
    }
}
