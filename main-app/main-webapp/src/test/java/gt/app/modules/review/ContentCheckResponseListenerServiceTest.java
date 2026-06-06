package gt.app.modules.review;

import gt.app.frwk.AbstractIntegrationTest;
import gt.contentchecker.ContentCheckOutcome;
import gt.contentchecker.Request;
import gt.contentchecker.Response;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@SpringBootTest
class ContentCheckResponseListenerServiceTest extends AbstractIntegrationTest {

    @MockitoBean
    CommentReviewResponseService commentReviewResponseService;

    @MockitoBean
    ArticleReviewResponseService articleReviewResponseService;

    @Autowired
    JmsTemplate jmsTemplate;

    @Value("${app-properties.jms.content-checker-call-back-response-queue}")
    String callBackResponseQueue;

    @Test
    void routesCommentResponseToCommentReviewService() {
        Response resp = Response.builder()
            .entityId("1")
            .requestType(Request.RequestType.COMMENT)
            .contentCheckOutcome(ContentCheckOutcome.PASSED)
            .build();

        jmsTemplate.convertAndSend(callBackResponseQueue, resp);

        ArgumentCaptor<Response> captor = ArgumentCaptor.forClass(Response.class);
        verify(commentReviewResponseService, timeout(5000)).handle(captor.capture());
        verifyNoInteractions(articleReviewResponseService);

        Response captured = captor.getValue();
        assertThat(captured.getEntityId()).isEqualTo("1");
        assertThat(captured.getRequestType()).isEqualTo(Request.RequestType.COMMENT);
        assertThat(captured.getContentCheckOutcome()).isEqualTo(ContentCheckOutcome.PASSED);
    }

    @Test
    void routesArticleResponseToArticleReviewService() {
        Response resp = Response.builder()
            .entityId("2")
            .requestType(Request.RequestType.ARTICLE)
            .contentCheckOutcome(ContentCheckOutcome.FAILED)
            .build();

        jmsTemplate.convertAndSend(callBackResponseQueue, resp);

        ArgumentCaptor<Response> captor = ArgumentCaptor.forClass(Response.class);
        verify(articleReviewResponseService, timeout(5000)).handle(captor.capture());
        verifyNoInteractions(commentReviewResponseService);

        Response captured = captor.getValue();
        assertThat(captured.getEntityId()).isEqualTo("2");
        assertThat(captured.getRequestType()).isEqualTo(Request.RequestType.ARTICLE);
        assertThat(captured.getContentCheckOutcome()).isEqualTo(ContentCheckOutcome.FAILED);
    }

    @Test
    void routesManualReviewNeededArticleResponseToArticleReviewService() {
        Response resp = Response.builder()
            .entityId("3")
            .requestType(Request.RequestType.ARTICLE)
            .contentCheckOutcome(ContentCheckOutcome.MANUAL_REVIEW_NEEDED)
            .build();

        jmsTemplate.convertAndSend(callBackResponseQueue, resp);

        ArgumentCaptor<Response> captor = ArgumentCaptor.forClass(Response.class);
        verify(articleReviewResponseService, timeout(5000)).handle(captor.capture());
        verifyNoInteractions(commentReviewResponseService);

        assertThat(captor.getValue().getContentCheckOutcome())
            .isEqualTo(ContentCheckOutcome.MANUAL_REVIEW_NEEDED);
    }
}
