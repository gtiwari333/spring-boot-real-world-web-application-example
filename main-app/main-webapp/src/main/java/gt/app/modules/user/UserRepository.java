package gt.app.modules.user;

import gt.app.domain.AppUser;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<AppUser, UUID> {

    @Override
    @Cacheable("userExistsById")
    boolean existsById(UUID id);

    @EntityGraph(attributePaths = {"authorities"})
    @Transactional(readOnly = true)
    @Cacheable("userWithAuthByUsername")
    Optional<AppUser> findOneWithAuthoritiesByUsername(String username);

    @Transactional(readOnly = true)
    @Cacheable("userExistsByUsername")
    boolean existsByUsername(String username);

    @Cacheable("userExistsByEmail")
    boolean existsByEmail(String email);
}
