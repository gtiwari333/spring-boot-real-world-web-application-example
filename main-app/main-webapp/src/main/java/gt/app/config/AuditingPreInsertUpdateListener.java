package gt.app.config;

import gt.app.config.security.SecurityUtils;
import gt.app.domain.AppUser;
import gt.app.domain.BaseAuditingEntity;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.hibernate.event.spi.PreInsertEvent;
import org.hibernate.event.spi.PreInsertEventListener;
import org.hibernate.event.spi.PreUpdateEvent;
import org.hibernate.event.spi.PreUpdateEventListener;
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
 * callbacks during the Hibernate flush. It uses the injected {@link EntityManager}
 * (a Spring thread-safe proxy) instead of casting {@code event.getSession()} to
 * {@code org.hibernate.Session}, which may fail in a native image when the session
 * implementation class differs.
 * <p>
 * In regular JVM operation the fields are already set by Spring Data's listener;
 * this listener simply overwrites them with the same values (idempotent).
 */
@Component
@RequiredArgsConstructor
public class AuditingPreInsertUpdateListener implements PreInsertEventListener, PreUpdateEventListener {

    private final EntityManager entityManager;

    @Override
    public boolean onPreInsert(PreInsertEvent event) {
        Object entity = event.getEntity();
        if (entity instanceof BaseAuditingEntity base) {
            UUID userId = SecurityUtils.getCurrentUserId();
            if (userId != null) {
                AppUser currentUser = entityManager.find(AppUser.class, userId);
                if (currentUser != null) {
                    // Update in-memory fields
                    base.setCreatedByUser(currentUser);
                    base.setLastModifiedByUser(currentUser);

                    // With bytecode.provider=none (GraalVM native image), Hibernate uses
                    // a pre-captured state[] array for the INSERT SQL.  Changes made via
                    // setters alone are NOT reflected — we must also update the state array.
                    String[] propertyNames = event.getPersister().getPropertyNames();
                    Object[] state = event.getState();
                    for (int i = 0; i < propertyNames.length; i++) {
                        if ("createdByUser".equals(propertyNames[i])) {
                            state[i] = currentUser;
                        } else if ("lastModifiedByUser".equals(propertyNames[i])) {
                            state[i] = currentUser;
                        }
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
                AppUser currentUser = entityManager.find(AppUser.class, userId);
                if (currentUser != null) {
                    base.setLastModifiedByUser(currentUser);

                    // Update the state array so the value is included in the UPDATE SQL
                    // even without bytecode enhancement in the native image.
                    String[] propertyNames = event.getPersister().getPropertyNames();
                    Object[] state = event.getState();
                    for (int i = 0; i < propertyNames.length; i++) {
                        if ("lastModifiedByUser".equals(propertyNames[i])) {
                            state[i] = currentUser;
                        }
                    }
                }
            }
        }
        return false;
    }
}
