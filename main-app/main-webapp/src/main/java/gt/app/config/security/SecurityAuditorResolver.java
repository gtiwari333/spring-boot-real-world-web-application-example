package gt.app.config.security;

import gt.app.domain.AppUser;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SecurityAuditorResolver implements AuditorAware<AppUser> {

    private final EntityManager entityManager;

    @Override
    public Optional<AppUser> getCurrentAuditor() {

        UUID userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            return Optional.empty();
        }

        // find() instead of getReference() — getReference() creates a Hibernate proxy
        // which fails in native image with bytecode.provider=none
        return Optional.ofNullable(entityManager.find(AppUser.class, userId));
    }
}
