package gt.app;

import gt.app.api.ReportClient;
import gt.app.config.AppProperties;
import gt.app.config.security.CurrentUserToken;
import gt.app.modules.user.AppPermissionEvaluatorService;
import gt.app.modules.article.*;
import gt.app.modules.user.dto.PasswordUpdateDTO;
import gt.app.modules.user.dto.UserDTO;
import gt.app.modules.user.dto.UserProfileUpdateDTO;
import gt.app.modules.user.dto.UserSignUpDTO;
import gt.api.email.EmailDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ImportRuntimeHints;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.PageImpl;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Time;
import java.util.Arrays;
import java.util.Calendar;

@SpringBootApplication
@Slf4j
@EnableConfigurationProperties(AppProperties.class)
@EnableCaching
@EnableScheduling
@ImportRuntimeHints(NativeRuntimeHints.class)
public class MainApplication {

    public static void main(String[] args) throws UnknownHostException {

        SpringApplication app = new SpringApplication(MainApplication.class);
        Environment env = app.run(args).getEnvironment();

        log.info("""
                Access URLs:
                ----------------------------------------------------------
                \tLocal: \t\t\thttp://localhost:{}
                \tExternal: \t\thttp://{}:{}
                \tEnvironment: \t{}\s
                ----------------------------------------------------------""",
            env.getProperty("server.port"),
            InetAddress.getLocalHost().getHostAddress(),
            env.getProperty("server.port"),
            Arrays.toString(env.getActiveProfiles())
        );
    }
}

// Required for GraalVM native image — registers types that need reflective access
// but aren't automatically discovered by Spring AOT (DTOs, records, config properties).
class NativeRuntimeHints implements RuntimeHintsRegistrar {
    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        hints
            .reflection()
            // config properties
            .registerType(AppProperties.class, MemberCategory.values())
            .registerType(AppProperties.FileStorage.class, MemberCategory.values())
            .registerType(AppProperties.Email.class, MemberCategory.values())
            .registerType(AppProperties.JmsProps.class, MemberCategory.values())
            .registerType(AppProperties.Web.class, MemberCategory.values())
            // DTOs and records for JSON serialization/deserialization
            .registerType(EmailDto.class, MemberCategory.values())
            .registerType(EmailDto.FileBArray.class, MemberCategory.values())
            .registerType(PasswordUpdateDTO.class, MemberCategory.values())
            .registerType(UserDTO.class, MemberCategory.values())
            .registerType(UserProfileUpdateDTO.class, MemberCategory.values())
            .registerType(UserSignUpDTO.class, MemberCategory.values())
            .registerType(ArticleCreateDto.class, MemberCategory.values())
            .registerType(ArticleEditDto.class, MemberCategory.values())
            .registerType(ArticleReadDto.class, MemberCategory.values())
            .registerType(ArticleReadDto.FileInfo.class, MemberCategory.values())
            .registerType(ArticleReadDto.CommentDto.class, MemberCategory.values())
            .registerType(ArticlePreviewDto.class, MemberCategory.values())
            .registerType(ArticlePreviewDto.FileInfo.class, MemberCategory.values())
            .registerType(ArticleReviewResultDto.class, MemberCategory.values())
            .registerType(NewCommentDto.class, MemberCategory.values())
            // client-side API records
            .registerType(ReportClient.FlagCount.class, MemberCategory.values())
            // security types — CurrentUserToken itself needed for SpEL property access, not just the inner UserToken
            .registerType(CurrentUserToken.class, MemberCategory.values())
            .registerType(CurrentUserToken.UserToken.class, MemberCategory.values())
            .registerType(AppPermissionEvaluatorService.class, MemberCategory.values())
            .registerType(UsernamePasswordAuthenticationToken.class, MemberCategory.values())
            // user stats accessed by Thymeleaf on account page
            .registerType(gt.app.modules.user.UserStat.class, MemberCategory.values())
            // Spring Data — needed for getTotalElements() etc. in native image
            .registerType(PageImpl.class, MemberCategory.values())
            // MapStruct: Mappers.getMapper() falls back to Class.forName("<Interface>Impl") when no ServiceLoader entry exists
            .registerTypeIfPresent(classLoader, "gt.app.modules.article.ArticleMapperImpl", MemberCategory.values())
            // Thymeleaf expression utility objects (#lists, #numbers, #temporals) — SpEL resolves their methods via reflection
            .registerTypeIfPresent(classLoader, "org.thymeleaf.expression.Lists", MemberCategory.values())
            .registerTypeIfPresent(classLoader, "org.thymeleaf.expression.Numbers", MemberCategory.values())
            .registerTypeIfPresent(classLoader, "org.thymeleaf.extras.java8time.expression.Temporals", MemberCategory.values())
            .registerTypeIfPresent(classLoader, "org.thymeleaf.expression.Temporals", MemberCategory.values())
            // Thymeleaf binary/unary expression nodes — BinaryOperationExpression.doComposeBinaryOperationExpression()
            // calls getDeclaredConstructor(IStandardExpression, IStandardExpression).newInstance() at runtime
            .registerTypeIfPresent(classLoader, "org.thymeleaf.standard.expression.EqualsExpression", MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS)
            .registerTypeIfPresent(classLoader, "org.thymeleaf.standard.expression.NotEqualsExpression", MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS)
            .registerTypeIfPresent(classLoader, "org.thymeleaf.standard.expression.GreaterThanExpression", MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS)
            .registerTypeIfPresent(classLoader, "org.thymeleaf.standard.expression.LessThanExpression", MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS)
            .registerTypeIfPresent(classLoader, "org.thymeleaf.standard.expression.GreaterOrEqualToExpression", MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS)
            .registerTypeIfPresent(classLoader, "org.thymeleaf.standard.expression.LessOrEqualToExpression", MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS)
            .registerTypeIfPresent(classLoader, "org.thymeleaf.standard.expression.AndExpression", MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS)
            .registerTypeIfPresent(classLoader, "org.thymeleaf.standard.expression.OrExpression", MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS)
            .registerTypeIfPresent(classLoader, "org.thymeleaf.standard.expression.AdditionExpression", MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS)
            .registerTypeIfPresent(classLoader, "org.thymeleaf.standard.expression.SubtractionExpression", MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS)
            .registerTypeIfPresent(classLoader, "org.thymeleaf.standard.expression.MultiplicationExpression", MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS)
            .registerTypeIfPresent(classLoader, "org.thymeleaf.standard.expression.DivisionExpression", MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS)
            .registerTypeIfPresent(classLoader, "org.thymeleaf.standard.expression.RemainderExpression", MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS)
            // content-checker JMS model — main-webapp sends Request and receives Response via Jackson JMS converter
            .registerTypeIfPresent(classLoader, "gt.contentchecker.Request", MemberCategory.values())
            .registerTypeIfPresent(classLoader, "gt.contentchecker.Request$RequestType", MemberCategory.values())
            .registerTypeIfPresent(classLoader, "gt.contentchecker.Response", MemberCategory.values())
            .registerTypeIfPresent(classLoader, "gt.contentchecker.ContentCheckOutcome", MemberCategory.values())
            // OAuth2 authentication — SpEL accesses .principal.attributes via reflection in Thymeleaf templates
            .registerTypeIfPresent(classLoader, "org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken", MemberCategory.values())
            .registerTypeIfPresent(classLoader, "org.springframework.security.oauth2.core.user.DefaultOAuth2User", MemberCategory.values())
            .registerTypeIfPresent(classLoader, "org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser", MemberCategory.values())
            // @AuthenticationPrincipal(expression = "idToken") in AuthController accesses OidcIdToken via SpEL
            .registerTypeIfPresent(classLoader, "org.springframework.security.oauth2.core.oidc.OidcIdToken", MemberCategory.values())
            // @CurrentUser uses T(SecurityUtils) in a SpEL expression — type lookup via StandardTypeLocator requires reflection
            .registerType(gt.app.config.security.SecurityUtils.class, MemberCategory.values());

        // Artemis generated _impl classes loaded via Class.forName at runtime
        hints.reflection()
            .registerTypeIfPresent(classLoader, "org.apache.activemq.artemis.core.client.ActiveMQClientMessageBundle_impl", MemberCategory.values())
            .registerTypeIfPresent(classLoader, "org.apache.activemq.artemis.core.client.ActiveMQClientLogger_impl", MemberCategory.values())
            .registerTypeIfPresent(classLoader, "org.apache.activemq.artemis.logs.ActiveMQUtilBundle_impl", MemberCategory.values())
            .registerTypeIfPresent(classLoader, "org.apache.activemq.artemis.logs.ActiveMQUtilLogger_impl", MemberCategory.values())
            .registerTypeIfPresent(classLoader, "org.apache.activemq.artemis.logs.AuditLogger_impl", MemberCategory.values())
            .registerTypeIfPresent(classLoader, "org.apache.activemq.artemis.jms.client.ActiveMQJMSClientBundle_impl", MemberCategory.values())
            .registerTypeIfPresent(classLoader, "org.apache.activemq.artemis.jms.client.ActiveMQJMSClientLogger_impl", MemberCategory.values())
            // load-balancing policies instantiated via ClassloadingUtil.newInstanceFromClassLoader (Class.forName)
            .registerTypeIfPresent(classLoader, "org.apache.activemq.artemis.api.core.client.loadbalance.RoundRobinConnectionLoadBalancingPolicy", MemberCategory.values())
            .registerTypeIfPresent(classLoader, "org.apache.activemq.artemis.api.core.client.loadbalance.RandomConnectionLoadBalancingPolicy", MemberCategory.values())
            .registerTypeIfPresent(classLoader, "org.apache.activemq.artemis.api.core.client.loadbalance.FirstElementConnectionLoadBalancingPolicy", MemberCategory.values())
            .registerTypeIfPresent(classLoader, "org.apache.activemq.artemis.api.core.client.loadbalance.RandomStickyConnectionLoadBalancingPolicy", MemberCategory.values())
            // connector/acceptor factories instantiated via ClassloadingUtil (Class.forName)
            .registerTypeIfPresent(classLoader, "org.apache.activemq.artemis.core.remoting.impl.netty.NettyConnectorFactory", MemberCategory.values())
            .registerTypeIfPresent(classLoader, "org.apache.activemq.artemis.core.remoting.impl.netty.NettyAcceptorFactory", MemberCategory.values())
            .registerTypeIfPresent(classLoader, "org.apache.activemq.artemis.core.remoting.impl.invm.InVMConnectorFactory", MemberCategory.values())
            .registerTypeIfPresent(classLoader, "org.apache.activemq.artemis.core.remoting.impl.invm.InVMAcceptorFactory", MemberCategory.values())
            // commons-beanutils ConvertUtilsBean.registerArrays calls Array.newInstance on these types;
            // object array types must be registered explicitly in native image
            .registerType(BigDecimal[].class)
            .registerType(BigInteger[].class)
            .registerType(Boolean[].class)
            .registerType(Byte[].class)
            .registerType(Character[].class)
            .registerType(Double[].class)
            .registerType(Float[].class)
            .registerType(Integer[].class)
            .registerType(Long[].class)
            .registerType(Short[].class)
            .registerType(String[].class)
            .registerType(Class[].class)
            .registerType(java.util.Date[].class)
            .registerType(Calendar[].class)
            .registerType(File[].class)
            .registerType(java.sql.Date[].class)
            .registerType(Time[].class)
            .registerType(java.sql.Timestamp[].class)
            .registerType(java.net.URL[].class);

        // Spring SingleConnectionFactory wraps the JMS connection in a JDK proxy; interfaces added conditionally
        hints.proxies()
            .registerJdkProxy(jakarta.jms.Connection.class, jakarta.jms.QueueConnection.class, jakarta.jms.TopicConnection.class)
            .registerJdkProxy(jakarta.jms.Connection.class, jakarta.jms.QueueConnection.class)
            .registerJdkProxy(jakarta.jms.Connection.class, jakarta.jms.TopicConnection.class)
            .registerJdkProxy(jakarta.jms.Connection.class)
            // Spring CachingConnectionFactory wraps JMS sessions in a JDK proxy; interfaces added conditionally
            .registerJdkProxy(org.springframework.jms.connection.SessionProxy.class, jakarta.jms.QueueSession.class, jakarta.jms.TopicSession.class)
            .registerJdkProxy(org.springframework.jms.connection.SessionProxy.class, jakarta.jms.QueueSession.class)
            .registerJdkProxy(org.springframework.jms.connection.SessionProxy.class, jakarta.jms.TopicSession.class)
            .registerJdkProxy(org.springframework.jms.connection.SessionProxy.class);

        // Hibernate resolves PhysicalNamingStrategy by class name via Class.forName()
        hints.reflection()
            .registerTypeIfPresent(classLoader, "gt.app.hibernate.PrefixedNamingStrategy", MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS, MemberCategory.INVOKE_PUBLIC_METHODS);

        // Liquibase calls getter methods via reflection in ChangeParameterMetaData.getCurrentValue()
        // during checksum generation — register all change types used in the changelog
        hints.reflection()
            .registerTypeIfPresent(classLoader, "liquibase.change.core.AddForeignKeyConstraintChange", MemberCategory.INVOKE_PUBLIC_METHODS)
            .registerTypeIfPresent(classLoader, "liquibase.change.core.CreateTableChange", MemberCategory.INVOKE_PUBLIC_METHODS)
            .registerTypeIfPresent(classLoader, "liquibase.change.core.CreateIndexChange", MemberCategory.INVOKE_PUBLIC_METHODS)
            .registerTypeIfPresent(classLoader, "liquibase.change.ColumnConfig", MemberCategory.INVOKE_PUBLIC_METHODS)
            .registerTypeIfPresent(classLoader, "liquibase.change.ConstraintsConfig", MemberCategory.INVOKE_PUBLIC_METHODS);

        hints.resources()
            .registerPattern("liquibase/master.xml")
            .registerPattern("liquibase/changelog/*.xml")
            .registerPattern("www.liquibase.org/xml/ns/dbchangelog/*.xsd")
            .registerPattern("activemq-version.properties");
    }
}
