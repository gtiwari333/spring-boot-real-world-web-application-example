package gt.contentchecker;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
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



