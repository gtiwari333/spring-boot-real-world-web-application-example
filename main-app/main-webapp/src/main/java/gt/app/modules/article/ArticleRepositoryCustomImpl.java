package gt.app.modules.article;

import gt.app.domain.Article;
import gt.app.modules.common.AbstractRepositoryImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

@Slf4j
class ArticleRepositoryCustomImpl extends AbstractRepositoryImpl<Article, ArticleRepository> implements ArticleRepositoryCustom {

    ArticleRepositoryCustomImpl() {
        super(Article.class);
    }

    @Autowired
    @Lazy
    public void setRepository(ArticleRepository repository) {
        this.repository = repository;
    }

}
