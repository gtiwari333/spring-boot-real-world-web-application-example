package gt.contentchecker;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jms.core.JmsTemplate;

@SpringBootTest
@Import(TestContainerConfig.class)
class ContentCheckHandlerTest {

    @Autowired
    JmsTemplate jmsTemplate;

    @Value("${jms.content-checker-request-queue}")
    String requestQueue;

    @BeforeEach
    void setUp() {
        jmsTemplate.setReceiveTimeout(5000);
    }

    @AfterEach
    void tearDown() {
        jmsTemplate.setReceiveTimeout(0);
    }

    @Test
    void passesCleanText() {
        String responseTopic = "test-response-" + UUID.randomUUID();
        Request request = Request.withArticle("hello world", responseTopic, "1", Request.RequestType.COMMENT);

        jmsTemplate.convertAndSend(requestQueue, request);

        Response response = (Response) jmsTemplate.receiveAndConvert(responseTopic);
        assertThat(response).isNotNull();
        assertThat(response.getContentCheckOutcome()).isEqualTo(ContentCheckOutcome.PASSED);
        assertThat(response.getEntityId()).isEqualTo("1");
    }

    @Test
    void failsOnBadWord() {
        String responseTopic = "test-response-" + UUID.randomUUID();
        Request request = Request.withArticle("what the fuck", responseTopic, "2", Request.RequestType.ARTICLE);

        jmsTemplate.convertAndSend(requestQueue, request);

        Response response = (Response) jmsTemplate.receiveAndConvert(responseTopic);
        assertThat(response).isNotNull();
        assertThat(response.getContentCheckOutcome()).isEqualTo(ContentCheckOutcome.FAILED);
        assertThat(response.getEntityId()).isEqualTo("2");
    }

    @Test
    void needsManualReviewOnControversialWord() {
        String responseTopic = "test-response-" + UUID.randomUUID();
        Request request = Request.withArticle("discussing politics today", responseTopic, "3",
            Request.RequestType.COMMENT);

        jmsTemplate.convertAndSend(requestQueue, request);

        Response response = (Response) jmsTemplate.receiveAndConvert(responseTopic);
        assertThat(response).isNotNull();
        assertThat(response.getContentCheckOutcome()).isEqualTo(ContentCheckOutcome.MANUAL_REVIEW_NEEDED);
        assertThat(response.getEntityId()).isEqualTo("3");
    }

    @Test
    void badWordsTakePriorityOverControversial() {
        String responseTopic = "test-response-" + UUID.randomUUID();
        Request request = Request.withArticle("fuck politics and conspiracy", responseTopic, "4",
            Request.RequestType.COMMENT);

        jmsTemplate.convertAndSend(requestQueue, request);

        Response response = (Response) jmsTemplate.receiveAndConvert(responseTopic);
        assertThat(response).isNotNull();
        assertThat(response.getContentCheckOutcome()).isEqualTo(ContentCheckOutcome.FAILED);
        assertThat(response.getEntityId()).isEqualTo("4");
    }

    @Test
    void handlesMultipleRequestsInSequence() {
        String topic1 = "test-response-" + UUID.randomUUID();
        String topic2 = "test-response-" + UUID.randomUUID();
        String topic3 = "test-response-" + UUID.randomUUID();

        jmsTemplate.convertAndSend(requestQueue,
            Request.withArticle("clean text here", topic1, "10", Request.RequestType.ARTICLE));
        jmsTemplate.convertAndSend(requestQueue,
            Request.withArticle("you suck", topic2, "20", Request.RequestType.COMMENT));
        jmsTemplate.convertAndSend(requestQueue,
            Request.withArticle("freedom of speech", topic3, "30", Request.RequestType.ARTICLE));

        Response r1 = (Response) jmsTemplate.receiveAndConvert(topic1);
        Response r2 = (Response) jmsTemplate.receiveAndConvert(topic2);
        Response r3 = (Response) jmsTemplate.receiveAndConvert(topic3);

        assertThat(r1).isNotNull();
        assertThat(r1.getContentCheckOutcome()).isEqualTo(ContentCheckOutcome.PASSED);
        assertThat(r1.getEntityId()).isEqualTo("10");

        assertThat(r2).isNotNull();
        assertThat(r2.getContentCheckOutcome()).isEqualTo(ContentCheckOutcome.FAILED);
        assertThat(r2.getEntityId()).isEqualTo("20");

        assertThat(r3).isNotNull();
        assertThat(r3.getContentCheckOutcome()).isEqualTo(ContentCheckOutcome.MANUAL_REVIEW_NEEDED);
        assertThat(r3.getEntityId()).isEqualTo("30");
    }
}
