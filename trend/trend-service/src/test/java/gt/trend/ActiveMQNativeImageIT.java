package gt.trend;

import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(TestContainerConfig.class)
public class ActiveMQNativeImageIT {

    @Autowired
    private ConnectionFactory connectionFactory;

    @Test
    public void testActiveMQClientLoggerInitialization() {
        try {
            // Attempting to create a connection will trigger the Artemis client
            // initialization and the JBoss Logging BundleFactory, forcing the
            // Class.forName("...ActiveMQClientLogger_impl") call.
            connectionFactory.createConnection().close();
        } catch (JMSException e) {
            // We expect this to fail if no broker is running, but the
            // important part is that the logger initialization was attempted
            // and recorded by the GraalVM agent.
            System.out.println("Expected connection failure, but logger initialized: " + e.getMessage());
        }
    }
}
