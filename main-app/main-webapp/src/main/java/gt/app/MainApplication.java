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
            .registerType(ArticleReviewResultDto.class, MemberCategory.values())
            .registerType(NewCommentDto.class, MemberCategory.values())
            // client-side API records
            .registerType(ReportClient.FlagCount.class, MemberCategory.values())
            .registerType(CurrentUserToken.UserToken.class, MemberCategory.values())
            // security types
            .registerType(AppPermissionEvaluatorService.class, MemberCategory.values())
            .registerType(UsernamePasswordAuthenticationToken.class, MemberCategory.values())
            // Spring Data — needed for getTotalElements() etc. in native image
            .registerType(PageImpl.class, MemberCategory.values())
            // MapStruct: Mappers.getMapper() falls back to Class.forName("<Interface>Impl") when no ServiceLoader entry exists
            .registerTypeIfPresent(classLoader, "gt.app.modules.article.ArticleMapperImpl", MemberCategory.values())
            // Thymeleaf expression utility objects (#lists, #numbers, #temporals) — SpEL resolves their methods via reflection
            .registerTypeIfPresent(classLoader, "org.thymeleaf.expression.Lists", MemberCategory.values())
            .registerTypeIfPresent(classLoader, "org.thymeleaf.expression.Numbers", MemberCategory.values())
            .registerTypeIfPresent(classLoader, "org.thymeleaf.extras.java8time.expression.Temporals", MemberCategory.values());

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

        hints.resources()
            .registerPattern("liquibase/master.xml")
            .registerPattern("liquibase/changelog/*.xml")
            .registerPattern("www.liquibase.org/xml/ns/dbchangelog/*.xsd")
            .registerPattern("activemq-version.properties");
    }
}
