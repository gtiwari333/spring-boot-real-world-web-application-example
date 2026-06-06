package gt.app.modules.review;

import gt.app.domain.Article;
import gt.app.domain.ArticleStatus;
import gt.app.domain.Comment;
import gt.app.domain.CommentStatus;
import gt.app.modules.article.ArticleRepository;
import gt.app.modules.article.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@Primary
public class NoopContentCheckService implements ContentCheckService {

    private final ArticleRepository articleRepository;
    private final CommentRepository commentRepository;

    @Override
    public void sendForAutoContentReview(Article a) {
        a.setStatus(ArticleStatus.PUBLISHED);
        articleRepository.save(a);
    }

    @Override
    public void sendForAutoContentReview(Comment c) {
        c.setStatus(CommentStatus.HIDDEN);
        commentRepository.save(c);
    }
}
