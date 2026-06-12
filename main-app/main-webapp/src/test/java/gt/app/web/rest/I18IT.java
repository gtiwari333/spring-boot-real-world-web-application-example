package gt.app.web.rest;

import gt.app.frwk.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class I18IT extends AbstractIntegrationTest {

    @Test
    void testi18n(@Autowired MockMvc mvc) throws Exception {
        mvc.perform(get("/"))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Language", "en-US"));

        mvc.perform(get("/?lang=np"))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Language", "np"));

        mvc.perform(get("/?lang=en"))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Language", "en"));
    }
}
