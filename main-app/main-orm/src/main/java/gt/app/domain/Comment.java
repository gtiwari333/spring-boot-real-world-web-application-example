package gt.app.domain;

import jakarta.persistence.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Data
public class Comment extends BaseAuditingEntity implements Serializable {
    @Serial
    private static final long serialVersionUID = -420530763778423311L;

    private String content;

    @Enumerated(EnumType.STRING)
    private CommentStatus status = CommentStatus.AWAITING_APPROVAL;

    @JoinColumn(nullable = false)
    private Long articleId;

    @JoinColumn(nullable = true)
    private Long parentCommentId;

    public Comment() {
    }

    public Comment(String content, Long articleId) {
        this.content = content;
        this.articleId = articleId;
    }

    public Comment(String content, Long parentCommentId, Long articleId) {
        this.content = content;
        this.articleId = articleId;
        this.parentCommentId = parentCommentId;
    }

    public void addChildComment(Comment childComment) {
        childComment.setParentCommentId(getId());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Comment comment = (Comment) o;
        return Objects.equals(id, comment.id) &&
            Objects.equals(articleId, comment.articleId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, articleId);
    }
}
