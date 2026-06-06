package gt.app.modules.article;

import gt.app.domain.Comment;
import gt.app.modules.common.AbstractRepositoryImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

@Slf4j
class CommentRepositoryCustomImpl extends AbstractRepositoryImpl<Comment, CommentRepository> implements CommentRepositoryCustom {

    CommentRepositoryCustomImpl( ) {
        super(Comment.class);
    }

    @Autowired
    @Lazy
    public void setRepository(CommentRepository repository) {
        this.repository = repository;
    }

}
