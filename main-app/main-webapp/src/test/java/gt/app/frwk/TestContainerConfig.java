package gt.app.frwk;

import gt.app.api.ReportClient;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.web.config.PageableHandlerMethodArgumentResolverCustomizer;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.testcontainers.activemq.ArtemisContainer;
import org.testcontainers.mysql.MySQLContainer;

import static org.springframework.test.web.client.ExpectedCount.manyTimes;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

@TestConfiguration(proxyBeanMethods = false)
public class TestContainerConfig {

    static final ArtemisContainer artemis = new ArtemisContainer("apache/activemq-artemis:2.44.0")
        .withReuse(true);

    static final MySQLContainer mysql = new MySQLContainer("mysql:9.7")
        .withReuse(true)
        .withDatabaseName("test_mainwebapp")
        .withCommand(
            "mysqld",
            "--lower_case_table_names=1",
            "--character_set_server=utf8mb4",
            "--explicit_defaults_for_timestamp"
        );

    static {
        artemis.start();
        mysql.start();
    }


    @Bean
    @ServiceConnection
    ArtemisContainer artemis() {
        // activemq-artemis has @SeriviceConnection support, so using it here.
        return artemis;
    }

    @Bean
    @ServiceConnection
    static MySQLContainer mysql() {     //mysql is lightweight
        return mysql;
    }

    /**
     * Override the production maxPageSize=5 so e2e tests that assert
     * on article listings don't need to handle pagination.
     */
    @Bean
    @Primary
    PageableHandlerMethodArgumentResolverCustomizer testPaginationCustomizer() {
        return resolver -> resolver.setMaxPageSize(50);
    }

    /**
     * Replace the RestClient-based ReportClient so tests don't need the
     * report-service to be running. Uses MockRestServiceServer to return
     * real JSON that Jackson deserializes into FlagCount — this exercises
     * the full record-deserialization path, critical for GraalVM
     * native-image reachability metadata generation.
     */
    @Bean
    @Primary
    ReportClient reportClient(ClientHttpRequestInterceptor interceptor) {
        RestClient.Builder builder = RestClient.builder();

        MockRestServiceServer mockServer = MockRestServiceServer.bindTo(builder).build();
        mockServer.expect(manyTimes(), requestTo("http://localhost:0/to-review"))
            .andExpect(method(HttpMethod.POST))
            .andRespond(withSuccess("{\"value\": 2}", MediaType.APPLICATION_JSON));

        return new ReportClient("http://localhost:0", builder, interceptor);
    }

}
