package gt.app.api;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

/**
 * Tests that {@link ReportClient} correctly deserializes JSON into the
 * {@link ReportClient.FlagCount} record. This is critical for GraalVM
 * native-image reachability metadata — Jackson must call
 * {@code Class.getRecordComponents()} on {@code FlagCount} during
 * deserialization, and the native-image-agent must trace that call.
 */
class ReportClientTest {

    private static final String BASE_URL = "http://localhost:0";

    @Test
    void deserializesJsonResponseToFlagCount() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo(BASE_URL + "/to-review"))
            .andExpect(method(HttpMethod.POST))
            .andRespond(withSuccess("{\"value\": 42}", MediaType.APPLICATION_JSON));

        ReportClient client = new ReportClient(BASE_URL, builder,
            (request, body, execution) -> execution.execute(request, body));

        ReportClient.FlagCount result = client.getFlaggedForReviewCount();

        assertThat(result).isNotNull();
        assertThat(result.value()).isEqualTo(42);
        server.verify();
    }

    @Test
    void returnsFallbackValueOnHttpError() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo(BASE_URL + "/to-review"))
            .andExpect(method(HttpMethod.POST))
            .andRespond(withServerError());

        ReportClient client = new ReportClient(BASE_URL, builder,
            (request, body, execution) -> execution.execute(request, body));

        ReportClient.FlagCount result = client.getFlaggedForReviewCount();

        assertThat(result.value()).isEqualTo(-100); // fallback from catch block
        server.verify();
    }
}
