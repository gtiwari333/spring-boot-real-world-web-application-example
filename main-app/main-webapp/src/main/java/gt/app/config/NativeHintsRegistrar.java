package gt.app.config;

import gt.app.config.security.CurrentUserToken;
import gt.app.config.security.SecurityAuditorResolver;
import gt.app.config.security.SecurityUtils;
import gt.app.domain.Article;
import gt.app.domain.AppUser;
import gt.app.hibernate.PrefixedNamingStrategy;
import gt.app.modules.article.ArticleCreateDto;
import gt.app.modules.article.ArticleEditDto;
import gt.app.modules.article.ArticlePreviewDto;
import gt.app.modules.article.ArticleReadDto;
import gt.app.modules.article.ArticleReviewResultDto;
import gt.app.modules.article.NewCommentDto;
import gt.app.modules.user.UserStat;
import gt.app.modules.user.dto.PasswordUpdateDTO;
import gt.app.modules.user.dto.UserProfileUpdateDTO;
import gt.app.modules.user.dto.UserSignUpDTO;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.aot.hint.TypeReference;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.thymeleaf.expression.Lists;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;

/**
 * Registers DTO/model classes for reflective access in the GraalVM native image.
 * These classes are accessed via SpEL property resolution in Thymeleaf templates
 * (e.g., ${article.title}, ${article.userId}, *{firstName}).
 */
public class NativeHintsRegistrar implements RuntimeHintsRegistrar {

    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        // Hibernate native-image support
        hints.reflection().registerType(PrefixedNamingStrategy.class,
            MemberCategory.INVOKE_DECLARED_CONSTRUCTORS);
        hints.reflection().registerType(
            TypeReference.of("org.hibernate.bytecode.internal.bytebuddy.BytecodeProviderImpl"),
            MemberCategory.INVOKE_DECLARED_CONSTRUCTORS);

        // JPA auditing — Hibernate invokes entity listeners via reflection in native image
        hints.reflection().registerType(AuditingEntityListener.class,
            MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
            MemberCategory.INVOKE_PUBLIC_METHODS);
        hints.reflection().registerType(SecurityAuditorResolver.class,
            MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
            MemberCategory.INVOKE_PUBLIC_METHODS);
        hints.reflection().registerType(AppUser.class,
            MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
            MemberCategory.INVOKE_PUBLIC_METHODS);

        // Article DTOs used in landing, article list, read, edit, and admin templates
        hints.reflection().registerType(ArticlePreviewDto.class,
            MemberCategory.INVOKE_PUBLIC_METHODS,
            MemberCategory.INVOKE_DECLARED_CONSTRUCTORS);
        hints.reflection().registerType(ArticlePreviewDto.FileInfo.class,
            MemberCategory.INVOKE_PUBLIC_METHODS,
            MemberCategory.INVOKE_DECLARED_CONSTRUCTORS);

        hints.reflection().registerType(ArticleReadDto.class,
            MemberCategory.INVOKE_PUBLIC_METHODS,
            MemberCategory.INVOKE_DECLARED_CONSTRUCTORS);
        hints.reflection().registerType(ArticleReadDto.FileInfo.class,
            MemberCategory.INVOKE_PUBLIC_METHODS,
            MemberCategory.INVOKE_DECLARED_CONSTRUCTORS);
        hints.reflection().registerType(ArticleReadDto.CommentDto.class,
            MemberCategory.INVOKE_PUBLIC_METHODS,
            MemberCategory.INVOKE_DECLARED_CONSTRUCTORS);

        hints.reflection().registerType(ArticleEditDto.class,
            MemberCategory.INVOKE_PUBLIC_METHODS,
            MemberCategory.INVOKE_DECLARED_CONSTRUCTORS);

        // Form-backing DTOs for article create and review
        hints.reflection().registerType(ArticleCreateDto.class,
            MemberCategory.INVOKE_PUBLIC_METHODS,
            MemberCategory.INVOKE_DECLARED_CONSTRUCTORS);
        hints.reflection().registerType(ArticleReviewResultDto.class,
            MemberCategory.INVOKE_PUBLIC_METHODS,
            MemberCategory.INVOKE_DECLARED_CONSTRUCTORS);

        hints.reflection().registerType(NewCommentDto.class,
            MemberCategory.INVOKE_PUBLIC_METHODS,
            MemberCategory.INVOKE_DECLARED_CONSTRUCTORS);

        // User DTOs used in signup, profile, and password templates
        hints.reflection().registerType(UserProfileUpdateDTO.class,
            MemberCategory.INVOKE_PUBLIC_METHODS,
            MemberCategory.INVOKE_DECLARED_CONSTRUCTORS);
        hints.reflection().registerType(UserSignUpDTO.class,
            MemberCategory.INVOKE_PUBLIC_METHODS,
            MemberCategory.INVOKE_DECLARED_CONSTRUCTORS);
        hints.reflection().registerType(PasswordUpdateDTO.class,
            MemberCategory.INVOKE_PUBLIC_METHODS,
            MemberCategory.INVOKE_DECLARED_CONSTRUCTORS);

        // User stat DTO used in user summary fragment
        hints.reflection().registerType(UserStat.class,
            MemberCategory.INVOKE_PUBLIC_METHODS,
            MemberCategory.INVOKE_DECLARED_CONSTRUCTORS);

        // Domain entity used as form-backing object in landing page
        hints.reflection().registerType(Article.class,
            MemberCategory.INVOKE_PUBLIC_METHODS,
            MemberCategory.INVOKE_DECLARED_CONSTRUCTORS);

        // Spring Security types navigated via SpEL in Thymeleaf templates
        // e.g., #authentication.principal.attributes['preferred_username']
        hints.reflection().registerType(OAuth2AuthenticationToken.class,
            MemberCategory.INVOKE_PUBLIC_METHODS,
            MemberCategory.INVOKE_DECLARED_CONSTRUCTORS);
        hints.reflection().registerType(DefaultOidcUser.class,
            MemberCategory.INVOKE_PUBLIC_METHODS,
            MemberCategory.INVOKE_DECLARED_CONSTRUCTORS);
        hints.reflection().registerType(CurrentUserToken.class,
            MemberCategory.INVOKE_PUBLIC_METHODS,
            MemberCategory.INVOKE_DECLARED_CONSTRUCTORS);

        // SecurityUtils invoked via SpEL T(...) type reference in @AuthenticationPrincipal expression
        // T(gt.app.config.security.SecurityUtils).mapAuthenticationPrincipalToCurrentUser(#this)
        hints.reflection().registerType(SecurityUtils.class,
            MemberCategory.INVOKE_PUBLIC_METHODS,
            MemberCategory.INVOKE_DECLARED_CONSTRUCTORS);

        // Spring Data PageImpl — methods accessed via SpEL in pagination templates
        // e.g., ${pagedObject.getNumber()}, ${articles.getTotalElements()}
        hints.reflection().registerType(PageImpl.class,
            MemberCategory.INVOKE_PUBLIC_METHODS,
            MemberCategory.INVOKE_DECLARED_CONSTRUCTORS);

        // Thymeleaf expression utility — #lists.isEmpty() called via SpEL in templates
        hints.reflection().registerType(Lists.class,
            MemberCategory.INVOKE_PUBLIC_METHODS,
            MemberCategory.INVOKE_DECLARED_CONSTRUCTORS);
    }
}
