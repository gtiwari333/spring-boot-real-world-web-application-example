package gt.report;

import gt.report.frwk.TestContainerConfig;
import gtapp.jooq.tables.GAppUser;
import gtapp.jooq.tables.GArticle;
import org.jooq.DSLContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.nio.ByteBuffer;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(TestContainerConfig.class)
class StatReportIntegrationTest {

    @Autowired
    StatReport statReport;

    @Autowired
    DSLContext db;

    @AfterEach
    void cleanUp() {
        db.deleteFrom(GArticle.G_ARTICLE).execute();
        db.deleteFrom(GAppUser.G_APP_USER).execute();
    }

    @Test
    void returnsZeroWhenNoFlaggedArticles() {
        assertThat(statReport.run().value()).isZero();
    }

    @Test
    void countsFlaggedArticlesCorrectly() {
        byte[] userId = uuidToBytes(UUID.randomUUID());

        db.insertInto(GAppUser.G_APP_USER)
            .set(GAppUser.G_APP_USER.ID, userId)
            .set(GAppUser.G_APP_USER.EMAIL, "test@test.com")
            .set(GAppUser.G_APP_USER.FIRST_NAME, "Test")
            .set(GAppUser.G_APP_USER.LAST_NAME, "User")
            .set(GAppUser.G_APP_USER.USERNAME, "testuser")
            .execute();

        db.insertInto(GArticle.G_ARTICLE)
            .set(GArticle.G_ARTICLE.TITLE, "Flagged Article")
            .set(GArticle.G_ARTICLE.CONTENT, "Bad content")
            .set(GArticle.G_ARTICLE.STATUS, "FLAGGED_FOR_MANUAL_REVIEW")
            .set(GArticle.G_ARTICLE.CREATED_BY_USER_ID, userId)
            .set(GArticle.G_ARTICLE.CREATED_DATE, LocalDateTime.now())
            .set(GArticle.G_ARTICLE.LAST_MODIFIED_DATE, LocalDateTime.now())
            .execute();

        db.insertInto(GArticle.G_ARTICLE)
            .set(GArticle.G_ARTICLE.TITLE, "Clean Article")
            .set(GArticle.G_ARTICLE.CONTENT, "Good content")
            .set(GArticle.G_ARTICLE.STATUS, "PUBLISHED")
            .set(GArticle.G_ARTICLE.CREATED_BY_USER_ID, userId)
            .set(GArticle.G_ARTICLE.CREATED_DATE, LocalDateTime.now())
            .set(GArticle.G_ARTICLE.LAST_MODIFIED_DATE, LocalDateTime.now())
            .execute();

        assertThat(statReport.run().value()).isEqualTo(1);
    }

    @Test
    void countsMultipleFlaggedArticles() {
        byte[] userId = uuidToBytes(UUID.randomUUID());

        db.insertInto(GAppUser.G_APP_USER)
            .set(GAppUser.G_APP_USER.ID, userId)
            .set(GAppUser.G_APP_USER.EMAIL, "test@test.com")
            .set(GAppUser.G_APP_USER.FIRST_NAME, "Test")
            .set(GAppUser.G_APP_USER.LAST_NAME, "User")
            .set(GAppUser.G_APP_USER.USERNAME, "testuser")
            .execute();

        for (int i = 0; i < 3; i++) {
            db.insertInto(GArticle.G_ARTICLE)
                .set(GArticle.G_ARTICLE.TITLE, "Flagged " + i)
                .set(GArticle.G_ARTICLE.CONTENT, "Content " + i)
                .set(GArticle.G_ARTICLE.STATUS, "FLAGGED_FOR_MANUAL_REVIEW")
                .set(GArticle.G_ARTICLE.CREATED_BY_USER_ID, userId)
                .set(GArticle.G_ARTICLE.CREATED_DATE, LocalDateTime.now())
                .set(GArticle.G_ARTICLE.LAST_MODIFIED_DATE, LocalDateTime.now())
                .execute();
        }

        assertThat(statReport.run().value()).isEqualTo(3);
    }

    private static byte[] uuidToBytes(UUID uuid) {
        ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());
        return bb.array();
    }
}
