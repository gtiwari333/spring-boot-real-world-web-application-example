package gt.trend;

import static org.assertj.core.api.Assertions.assertThat;

import gt.common.dtos.ArticleSummaryDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jms.core.JmsTemplate;

@SpringBootTest
@Import(TestContainerConfig.class)
class TrendJmsListenerTest {

    @Autowired
    JmsTemplate jmsTemplate;

    @BeforeEach
    void setUp() {
        jmsTemplate.setReceiveTimeout(3000);
    }

    @AfterEach
    void tearDown() {
        jmsTemplate.setReceiveTimeout(0);
    }

    @Test
    void articlePublishedListenerConsumesMessage() {
        var msg = articleSummary(1L, "Test Title", "Test Content", "testuser");

        jmsTemplate.convertAndSend("article-published", msg);

        assertThat(jmsTemplate.receiveAndConvert("article-published")).isNull();
    }

    @Test
    void articleReadListenerConsumesMessage() {
        var msg = articleSummary(2L, "Another Article", "More content", "reader1");

        jmsTemplate.convertAndSend("article-read", msg);

        assertThat(jmsTemplate.receiveAndConvert("article-read")).isNull();
    }

    @Test
    void bothListenersWorkIndependently() {
        var pubMsg = articleSummary(10L, "Published Article", "Pub content", "author");
        var readMsg = articleSummary(20L, "Read Article", "Read content", "reader");

        jmsTemplate.convertAndSend("article-published", pubMsg);
        jmsTemplate.convertAndSend("article-read", readMsg);

        assertThat(jmsTemplate.receiveAndConvert("article-published")).isNull();
        assertThat(jmsTemplate.receiveAndConvert("article-read")).isNull();
    }

    private static ArticleSummaryDto articleSummary(Long id, String title, String content, String username) {
        var dto = new ArticleSummaryDto();
        dto.setId(id);
        dto.setTitle(title);
        dto.setContent(content);
        dto.setUsername(username);
        return dto;
    }
}
