package gt.app;

import gt.app.config.AppProperties;
import gt.app.config.Constants;
import gt.app.domain.*;
import gt.app.modules.article.ArticleRepository;
import gt.app.modules.article.CommentRepository;
import gt.app.modules.user.AuthorityService;
import gt.app.modules.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.UUID;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataCreator {
    final AuthorityService authorityService;

    final UserService userService;
    final ArticleRepository articleRepository;
    final CommentRepository commentRepository;
    final AppProperties appProperties;

    public static final String USER1 = "user1";
    public static final String USER2 = "user2";
    public static final String SYSTEM_USER = "system"; //admin

    @EventListener
    public void ctxRefreshed(ContextRefreshedEvent evt) {
        initData();
    }

    public void initData() {

        log.info("Context Refreshed !!, Initializing environment (db, folders etc)... ");

        File uploadFolder = new File(appProperties.getFileStorage().getUploadFolder());
        if (!uploadFolder.exists()) {
            if (uploadFolder.mkdirs() && Stream.of(ReceivedFile.FileGroup.values()).allMatch(f -> new File(uploadFolder.getAbsolutePath() + File.separator + f.path).mkdir())) {
                log.info("Upload folder created successfully");
            } else {
                log.info("Failure to create upload folder");
            }
        }

        if (userService.existsByUsername("system")) {
            log.info("DB already initialized !!!");
            return;
        }

        Authority adminAuthority = new Authority();
        adminAuthority.setName(Constants.ROLE_ADMIN);
        authorityService.save(adminAuthority);

        Authority userAuthority = new Authority();
        userAuthority.setName(Constants.ROLE_USER);
        authorityService.save(userAuthority);

        String systemUserId = "a621ac4c-6172-4103-9050-b27c053b11eb";

        if (userService.exists(UUID.fromString(systemUserId))) {
            log.info("DB already initialized !!!");
            return;
        }

        //ID and login are linked with the keycloak export json
        AppUser adminUser = new AppUser(systemUserId, "system", "System", "Tiwari", "system@email");
        userService.save(adminUser);

        AppUser user1 = new AppUser("d1460f56-7f7e-43e1-8396-bddf39dba08f", "user1", "Ganesh", "Tiwari", "user1@email");
        userService.save(user1);


        AppUser user2 = new AppUser("fa6820a5-cf39-4cbf-9e50-89cc832bebee", "user2", "Jyoti", "Kattel", "user2@email");
        userService.save(user2);

        createArticle(adminUser, "Admin's First Article", "Content1 Admin");
        createArticle(adminUser, "Admin's Second Article", "Content2 Admin");
        createArticle(user1, "User1 Article", "Content User 1");
        createArticle(user2, "User2 Article", "Content User 2");

        // create articles for admin review testing
        createArticleWithStatus(user1, "Flagged Article To Accept", "Flagged Content To Accept", ArticleStatus.FLAGGED_FOR_MANUAL_REVIEW);
        createArticleWithStatus(user2, "Flagged Article To Reject", "Flagged Content To Reject", ArticleStatus.FLAGGED_FOR_MANUAL_REVIEW);
    }

    void createArticle(AppUser user, String title, String content) {
        createArticleWithStatus(user, title, content, ArticleStatus.PUBLISHED);
    }

    void createArticleWithStatus(AppUser user, String title, String content, ArticleStatus status) {
        var n = new Article();
        n.setCreatedByUser(user);
        n.setTitle(title);
        n.setContent(content);
        n.setStatus(status);

        articleRepository.save(n);

        Comment c = new Comment();
        c.setStatus(CommentStatus.SHOWING);
        c.setContent("Test comment for " + title);
        c.setArticleId(n.getId());
        c.setCreatedByUser(user); //self
        commentRepository.save(c);

    }


}
