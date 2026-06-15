package gt.app.config;

import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.event.spi.EventType;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration(proxyBeanMethods = false)
@EnableJpaAuditing //now @CreatedBy, @LastModifiedBy works
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = "gt.app.modules")
@RequiredArgsConstructor
class JpaConfig implements InitializingBean {

    private final EntityManagerFactory entityManagerFactory;
    private final AuditingPreInsertUpdateListener auditingPreInsertUpdateListener;

    @Override
    public void afterPropertiesSet() {
        SessionFactoryImplementor sfi = entityManagerFactory.unwrap(SessionFactoryImplementor.class);
        sfi.getEventListenerRegistry()
            .appendListeners(EventType.PRE_INSERT, auditingPreInsertUpdateListener);
        sfi.getEventListenerRegistry()
            .appendListeners(EventType.PRE_UPDATE, auditingPreInsertUpdateListener);
    }
}
