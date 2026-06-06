package gt.app.modules.review;

import gt.app.config.AppProperties;
import gt.app.domain.Article;
import gt.app.domain.Comment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import static gt.contentchecker.Request.RequestType.ARTICLE;
import static gt.contentchecker.Request.RequestType.COMMENT;
import static gt.contentchecker.Request.withArticle;

@RequiredArgsConstructor
@Service
@Slf4j
public class JmsContentCheckService implements ContentCheckService {

    private final JmsTemplate jmsTemplate;
    private final AppProperties appProperties;

    @Override
    @Async //TODO: tracing is not working with async yet !
    public void sendForAutoContentReview(Article a) {
        log.info("Sending article {} for review", a.getId());
        var req = withArticle(a.getContent(), appProperties.getJms().getContentCheckerCallBackResponseQueue(), Long.toString(a.getId()), ARTICLE);
        jmsTemplate.convertAndSend(appProperties.getJms().getContentCheckerRequestQueue(), req);
    }

    @Override
    @Async
    public void sendForAutoContentReview(Comment c) {
        log.info("Sending comment {} for review", c.getId());
        var req = withArticle(c.getContent(), appProperties.getJms().getContentCheckerCallBackResponseQueue(), Long.toString(c.getId()), COMMENT);
        jmsTemplate.convertAndSend(appProperties.getJms().getContentCheckerRequestQueue(), req);
    }

}
