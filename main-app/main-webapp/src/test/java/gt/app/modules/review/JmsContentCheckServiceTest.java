package gt.app.modules.review;

import gt.app.config.AppProperties;
import gt.app.domain.Article;
import gt.app.domain.Comment;
import gt.app.frwk.AbstractIntegrationTest;
import gt.contentchecker.Request;
import jakarta.jms.Message;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jms.core.JmsTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

@SpringBootTest
@Slf4j
class JmsContentCheckServiceTest extends AbstractIntegrationTest {

    @Autowired
    JmsContentCheckService jmsContentCheckService;

    @Autowired
    JmsTemplate jmsTemplate;

    @Autowired
    AppProperties appProperties;

    @Value("${app-properties.jms.content-checker-request-queue}")
    String requestQueue;

    @BeforeEach
    @AfterEach
    void drainQueue() {
        // drain any leftover messages from the shared Artemis container
        jmsTemplate.setReceiveTimeout(5_000);
        Message leftOver;
        while ((leftOver = jmsTemplate.receive(requestQueue)) != null) {
            log.info("Discarding leftover JMS message: {}", leftOver);
        }
    }

    @Test
    void sendsArticleForAutoContentReview() {
        assertThat(requestQueue).isNotNull();

        Article article = new Article();
        article.setId(1L);
        article.setContent("test article content");

        jmsContentCheckService.sendForAutoContentReview(article);

        Request sent = (Request) jmsTemplate.receiveAndConvert(requestQueue);
        assertThat(sent).isNotNull();
        assertThat(sent.getText()).isEqualTo("test article content");
        assertThat(sent.getEntityId()).isEqualTo("1");
        assertThat(sent.getRequestType()).isEqualTo(Request.RequestType.ARTICLE);
        assertThat(sent.getPostBackTopic())
            .isEqualTo(appProperties.getJms().getContentCheckerCallBackResponseQueue());
    }

    @Test
    void sendsCommentForAutoContentReview() {
        Comment comment = new Comment();
        comment.setId(2L);
        comment.setContent("test comment content");
        comment.setArticleId(42L);

        jmsContentCheckService.sendForAutoContentReview(comment);

        Request sent = (Request) jmsTemplate.receiveAndConvert(requestQueue);
        assertThat(sent).isNotNull();
        assertThat(sent.getText()).isEqualTo("test comment content");
        assertThat(sent.getEntityId()).isEqualTo("2");
        assertThat(sent.getRequestType()).isEqualTo(Request.RequestType.COMMENT);
        assertThat(sent.getPostBackTopic())
            .isEqualTo(appProperties.getJms().getContentCheckerCallBackResponseQueue());
    }

    @Test
    void handlesArticleWithEmptyContent() {
        Article article = new Article();
        article.setId(3L);
        article.setContent("");

        assertThatCode(() -> jmsContentCheckService.sendForAutoContentReview(article))
            .doesNotThrowAnyException();
    }
}
