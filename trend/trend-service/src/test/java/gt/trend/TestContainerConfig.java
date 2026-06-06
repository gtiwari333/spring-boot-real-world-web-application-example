package gt.trend;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.activemq.ArtemisContainer;

@TestConfiguration(proxyBeanMethods = false)
public class TestContainerConfig {

    static final ArtemisContainer artemis = new ArtemisContainer("apache/activemq-artemis:2.44.0");

    static {
        artemis.start();
    }

    @Bean
    @ServiceConnection
    ArtemisContainer artemis() {
        // activemq-artemis has @SeriviceConnection support, so using it here.
        return artemis;
    }

}
