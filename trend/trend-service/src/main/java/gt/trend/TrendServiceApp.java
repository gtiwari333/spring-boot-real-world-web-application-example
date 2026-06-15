package gt.trend;

import gt.common.dtos.ArticleSummaryDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ImportRuntimeHints;
import org.springframework.core.env.Environment;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.support.converter.JacksonJsonMessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessageType;
import trend.TrendDto;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

@SpringBootApplication
@Slf4j
@ImportRuntimeHints(NativeRuntimeHints.class)
public class TrendServiceApp {

    public static void main(String[] args) throws UnknownHostException {

        SpringApplication app = new SpringApplication(TrendServiceApp.class);
        Environment env = app.run(args).getEnvironment();

        log.info("""
                        Access URLs:
                        ----------------------------------------------------------
                        \tLocal: \t\t\thttp://localhost:{}
                        \tExternal: \t\thttp://{}:{}
                        \tEnvironment: \t{}\s
                        ----------------------------------------------------------""",
            env.getProperty("server.port"),
            InetAddress.getLocalHost().getHostAddress(),
            env.getProperty("server.port"),
            Arrays.toString(env.getActiveProfiles())
        );
    }

    @JmsListener(destination = "article-published")
    void onArticlePublished(ArticleSummaryDto msg) {
        log.info("Received msg for trend calculation {}", msg);
    }


    @JmsListener(destination = "article-read")
    void onArticleRead(ArticleSummaryDto msg) {
        log.info("Received msg for trend calculation {}", msg);
    }


    @Bean
    MessageConverter jacksonJmsMessageConverter() {
        var converter = new JacksonJsonMessageConverter();
        converter.setTargetType(MessageType.TEXT);
        converter.setTypeIdPropertyName("_type");
        return converter;
    }

}

class NativeRuntimeHints implements RuntimeHintsRegistrar {
    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        hints.reflection()
            .registerType(TrendDto.class, MemberCategory.values())
            .registerType(ArticleSummaryDto.class, MemberCategory.values())
            // Artemis JBoss Logging generated _impl classes loaded via Class.forName at runtime
            .registerTypeIfPresent(classLoader, "org.apache.activemq.artemis.core.client.ActiveMQClientMessageBundle_impl", MemberCategory.values())
            .registerTypeIfPresent(classLoader, "org.apache.activemq.artemis.core.client.ActiveMQClientLogger_impl", MemberCategory.values())
            .registerTypeIfPresent(classLoader, "org.apache.activemq.artemis.logs.ActiveMQUtilBundle_impl", MemberCategory.values())
            .registerTypeIfPresent(classLoader, "org.apache.activemq.artemis.logs.ActiveMQUtilLogger_impl", MemberCategory.values())
            .registerTypeIfPresent(classLoader, "org.apache.activemq.artemis.logs.AuditLogger_impl", MemberCategory.values())
            .registerTypeIfPresent(classLoader, "org.apache.activemq.artemis.jms.client.ActiveMQJMSClientBundle_impl", MemberCategory.values())
            .registerTypeIfPresent(classLoader, "org.apache.activemq.artemis.jms.client.ActiveMQJMSClientLogger_impl", MemberCategory.values())
            // load-balancing policies instantiated via ClassloadingUtil.newInstanceFromClassLoader (Class.forName)
            .registerTypeIfPresent(classLoader, "org.apache.activemq.artemis.api.core.client.loadbalance.RoundRobinConnectionLoadBalancingPolicy", MemberCategory.values())
            .registerTypeIfPresent(classLoader, "org.apache.activemq.artemis.api.core.client.loadbalance.RandomConnectionLoadBalancingPolicy", MemberCategory.values())
            .registerTypeIfPresent(classLoader, "org.apache.activemq.artemis.api.core.client.loadbalance.FirstElementConnectionLoadBalancingPolicy", MemberCategory.values())
            .registerTypeIfPresent(classLoader, "org.apache.activemq.artemis.api.core.client.loadbalance.RandomStickyConnectionLoadBalancingPolicy", MemberCategory.values())
            // connector/acceptor factories instantiated via ClassloadingUtil (Class.forName)
            .registerTypeIfPresent(classLoader, "org.apache.activemq.artemis.core.remoting.impl.netty.NettyConnectorFactory", MemberCategory.values())
            .registerTypeIfPresent(classLoader, "org.apache.activemq.artemis.core.remoting.impl.netty.NettyAcceptorFactory", MemberCategory.values())
            .registerTypeIfPresent(classLoader, "org.apache.activemq.artemis.core.remoting.impl.invm.InVMConnectorFactory", MemberCategory.values())
            .registerTypeIfPresent(classLoader, "org.apache.activemq.artemis.core.remoting.impl.invm.InVMAcceptorFactory", MemberCategory.values())
            // commons-beanutils ConvertUtilsBean.registerArrays calls Array.newInstance on these types;
            // object array types must be registered explicitly in native image
            .registerType(java.math.BigDecimal[].class)
            .registerType(java.math.BigInteger[].class)
            .registerType(Boolean[].class)
            .registerType(Byte[].class)
            .registerType(Character[].class)
            .registerType(Double[].class)
            .registerType(Float[].class)
            .registerType(Integer[].class)
            .registerType(Long[].class)
            .registerType(Short[].class)
            .registerType(String[].class)
            .registerType(Class[].class)
            .registerType(java.util.Date[].class)
            .registerType(java.util.Calendar[].class)
            .registerType(java.io.File[].class)
            .registerType(java.sql.Date[].class)
            .registerType(java.sql.Time[].class)
            .registerType(java.sql.Timestamp[].class)
            .registerType(java.net.URL[].class);

        hints.resources()
            .registerPattern("activemq-version.properties");
    }
}
