package gt.app.config;

import gt.app.config.security.SecurityUtils;
import gt.app.domain.AppUser;
import gt.app.domain.BaseAuditingEntity;
import org.hibernate.event.spi.PreInsertEvent;
import org.hibernate.event.spi.PreInsertEventListener;
import org.hibernate.event.spi.PreUpdateEvent;
import org.hibernate.event.spi.PreUpdateEventListener;
import org.hibernate.Session;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Hibernate-level fallback for {@code @CreatedBy} / {@code @LastModifiedBy} auditing.
 * <p>
 * In a GraalVM native image, Spring Data's {@code AuditingEntityListener} may not
 * receive its {@code AuditingHandler} (the {@code setAuditingHandler} call happens
 * through {@code AuditingBeanFactoryPostProcessor} which may not survive AOT
 * processing). When the handler is {@code null}, {@code touchForCreate} and
 * {@code touchForUpdate} are no-ops.
 * <p>
 * This listener fires <em>after</em> the JPA {@code @PrePersist} / {@code @PreUpdate}
 * callbacks during the Hibernate flush. It loads the current user via
 * {@link SecurityUtils} and the Hibernate {@code Session} from the event — no
 * Spring proxy or {@code AuditingHandler} involved, so it works in native images.
 * <p>
 * In regular JVM operation the fields are already set by Spring Data's listener;
 * this listener simply overwrites them with the same values (idempotent).
 */
@Component
public class AuditingPreInsertUpdateListener implements PreInsertEventListener, PreUpdateEventListener {

    @Override
    public boolean onPreInsert(PreInsertEvent event) {
        Object entity = event.getEntity();
        if (entity instanceof BaseAuditingEntity base) {
            UUID userId = SecurityUtils.getCurrentUserId();
            if (userId != null) {
                AppUser currentUser = ((Session) event.getSession())
                    .find(AppUser.class, userId);
                if (currentUser != null) {
                    if (base.getCreatedByUser() == null) {
                        base.setCreatedByUser(currentUser);
                    }
                    if (base.getLastModifiedByUser() == null) {
                        base.setLastModifiedByUser(currentUser);
                    }
                }
            }
        }
        return false;
    }

    @Override
    public boolean onPreUpdate(PreUpdateEvent event) {
        Object entity = event.getEntity();
        if (entity instanceof BaseAuditingEntity base) {
            UUID userId = SecurityUtils.getCurrentUserId();
            if (userId != null) {
                AppUser currentUser = ((Session) event.getSession())
                    .find(AppUser.class, userId);
                if (currentUser != null) {
                    base.setLastModifiedByUser(currentUser);
                }
            }
        }
        return false;
    }
}
