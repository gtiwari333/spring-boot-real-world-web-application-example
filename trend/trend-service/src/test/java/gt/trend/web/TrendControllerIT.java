package gt.trend.web;

import gt.trend.TestContainerConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jms.config.JmsListenerEndpointRegistry;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(TestContainerConfig.class)
class TrendControllerIT {

    @Test
    void contextLoadsWithJmsListeners(@Autowired JmsListenerEndpointRegistry registry) {
        assertThat(registry.getListenerContainers()).isNotEmpty();
    }
}
