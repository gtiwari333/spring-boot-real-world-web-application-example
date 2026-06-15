package gt.app.modules.article;

import gt.app.domain.Comment;
import gt.app.exception.RecordNotFoundException;
import gt.app.modules.review.ContentCheckService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {

    final ArticleRepository articleRepository;
    final CommentRepository commentRepository;
    final ContentCheckService contentCheckService;

    @Transactional
    public void save(NewCommentDto c) {
        Comment comment = new Comment(c.content, c.articleId);

        if (c.parentCommentId != null && c.parentCommentId > 0) {
            //checks if the requested parentCommentId actually belong to the articleId in request
            if (!commentRepository.existsByIdAndArticleId(c.parentCommentId, c.articleId)) {
                throw new RecordNotFoundException("The given parent comment id " + c.parentCommentId + " doesn't belong to article" + c.articleId);
            }
            comment.setParentCommentId(c.parentCommentId);
        }

        commentRepository.save(comment);

        contentCheckService.sendForAutoContentReview(comment);
    }


    public List<ArticleReadDto.CommentDto> readComments(Long articleId) {
        return commentRepository.findAllByArticleId(articleId).stream().map(ArticleMapper.INSTANCE::map).toList();
    }

}
