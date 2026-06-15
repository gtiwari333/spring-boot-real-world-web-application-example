package gt.contentchecker;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportRuntimeHints;
import org.springframework.core.env.Environment;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@SpringBootApplication
@Slf4j
@ImportRuntimeHints(NativeRuntimeHints.class)
public class ContentCheckerService {

    public static void main(String[] args) throws UnknownHostException {

        SpringApplication app = new SpringApplication(ContentCheckerService.class);
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
}

@Component
@Slf4j
@RequiredArgsConstructor
class ContentCheckHandler {

    final JmsTemplate jmsTemplate;
    final ContentChecker checker;

    @JmsListener(destination = "${jms.content-checker-request-queue}")
    void onMessage(Request msg) {
        log.info("Received msg for content check {}", msg);

        var result = checker.isOkay(msg.text);

        jmsTemplate.convertAndSend(msg.postBackTopic, Response.withResult(msg, result));
    }

}

@Component
@Slf4j
class ContentChecker {

    private final Pattern badPattern;
    private final Pattern controversialPattern;

    ContentChecker() {
        this.badPattern = buildPattern(List.of("fuck", "suck", "ass"));
        this.controversialPattern = buildPattern(List.of("party", "politics", "libtard", "freedom", "conspiracy", "snowflake"));
    }

    ContentCheckOutcome isOkay(String text) {
        Matcher m = badPattern.matcher(text);
        if (m.find()) {
            return ContentCheckOutcome.FAILED;
        }
        m = controversialPattern.matcher(text);
        if (m.find()) {
            return ContentCheckOutcome.MANUAL_REVIEW_NEEDED;
        }
        return ContentCheckOutcome.PASSED;
    }

    private static Pattern buildPattern(List<String> words) {
        String regex = words.stream()
            .map(w -> w.length() <= 3 ? Pattern.quote(w) + "\\b" : Pattern.quote(w))
            .collect(Collectors.joining("|", "\\b(?:", ")"));
        return Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
    }

}

class NativeRuntimeHints implements RuntimeHintsRegistrar {
    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        hints.reflection()
            .registerType(Request.class, MemberCategory.values())
            .registerType(Request.RequestType.class, MemberCategory.values())
            .registerType(Response.class, MemberCategory.values())
            .registerType(ContentCheckOutcome.class, MemberCategory.values())
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



