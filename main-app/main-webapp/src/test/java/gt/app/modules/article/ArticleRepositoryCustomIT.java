package gt.app.modules.article;

import gt.app.domain.Article;
import gt.app.domain.ArticleStatus;
import gt.app.domain.AppUser;
import gt.app.frwk.AbstractIntegrationTest;
import gt.app.modules.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional
class ArticleRepositoryCustomIT extends AbstractIntegrationTest {

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private UserRepository userRepository;

    // UUIDs match the users seeded by DataCreator
    private static final UUID USER1_ID = UUID.fromString("d1460f56-7f7e-43e1-8396-bddf39dba08f");

    @Test
    void countArticles_shouldReturnCorrectCountForEachStatus() {
        AppUser user = userRepository.getReferenceById(USER1_ID);

        // Given: DataCreator seeds 4 PUBLISHED articles at context startup;
        // those are committed and visible to this test transaction.
        long initialPublishedCount = articleRepository.countArticles(ArticleStatus.PUBLISHED);
        assertThat(initialPublishedCount).isGreaterThanOrEqualTo(4);

        // Create articles with FLAGGED_FOR_MANUAL_REVIEW status
        for (int i = 0; i < 3; i++) {
            Article article = new Article();
            article.setTitle("Flagged Article " + i);
            article.setContent("Content " + i);
            article.setStatus(ArticleStatus.FLAGGED_FOR_MANUAL_REVIEW);
            article.setCreatedByUser(user);
            articleRepository.save(article);
        }

        // Create articles with BLOCKED status
        for (int i = 0; i < 2; i++) {
            Article article = new Article();
            article.setTitle("Blocked Article " + i);
            article.setContent("Content " + i);
            article.setStatus(ArticleStatus.BLOCKED);
            article.setCreatedByUser(user);
            articleRepository.save(article);
        }

        // Then: counts should reflect only the given status
        assertThat(articleRepository.countArticles(ArticleStatus.FLAGGED_FOR_MANUAL_REVIEW))
            .isEqualTo(3);
        assertThat(articleRepository.countArticles(ArticleStatus.BLOCKED))
            .isEqualTo(2);
        assertThat(articleRepository.countArticles(ArticleStatus.UNDER_AUTO_REVIEW))
            .isZero();
        // Published count unchanged — new articles were created with other statuses
        assertThat(articleRepository.countArticles(ArticleStatus.PUBLISHED))
            .isEqualTo(initialPublishedCount);
    }

    @Test
    void countArticles_shouldReturnZeroForStatusWithNoArticles() {
        long count = articleRepository.countArticles(ArticleStatus.UNDER_AUTO_REVIEW);
        assertThat(count).isZero();
    }
}
