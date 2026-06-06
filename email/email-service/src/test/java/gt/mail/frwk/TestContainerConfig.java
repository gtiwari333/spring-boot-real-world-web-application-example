package gt.mail.frwk;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;

@TestConfiguration(proxyBeanMethods = false)
public class TestContainerConfig {

    public static final GenericContainer<?> mailhog = new GenericContainer<>("richarvey/mailhog")
        .withExposedPorts(1025);

    static {
        mailhog.start();
    }

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.mail.host", () -> mailhog.getHost());
        registry.add("spring.mail.port", () -> mailhog.getMappedPort(1025));
    }
}
