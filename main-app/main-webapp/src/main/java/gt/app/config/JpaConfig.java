package gt.app.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration(proxyBeanMethods = false)
@EnableJpaAuditing //now @CreatedBy, @LastModifiedBy works
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = "gt.app.modules")
class JpaConfig {

}
