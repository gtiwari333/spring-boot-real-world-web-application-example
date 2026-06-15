package gt.app.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestClient;

@Component
@Slf4j
public class ReportClient {

    private final RestClient restClient;

    public ReportClient(@Value("${feign-clients.report-service.url}") String baseUrl,
                        RestClient.Builder restClientBuilder,
                        ClientHttpRequestInterceptor bearerAuthInterceptor) {
        this.restClient = restClientBuilder
            .baseUrl(baseUrl)
            .requestInterceptor(bearerAuthInterceptor)
            .build();
    }

    public FlagCount getFlaggedForReviewCount() {
        try {
            return restClient.post()
                .uri("/to-review")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(FlagCount.class);
        } catch (Exception e) {
            log.debug("Failed to get flagged review count via RestClient, using fallback", e);
            return new FlagCount(-100);
        }
    }

    public record FlagCount(int value) {
    }
}



