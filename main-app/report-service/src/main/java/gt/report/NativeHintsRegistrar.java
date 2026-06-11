package gt.report;

import gt.app.hibernate.PrefixedNamingStrategy;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;

/**
 * Registers DTO/model classes for reflective access in the GraalVM native image.
 */
public class NativeHintsRegistrar implements RuntimeHintsRegistrar {

    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        // Hibernate native-image support
        hints.reflection().registerType(PrefixedNamingStrategy.class,
            MemberCategory.INVOKE_DECLARED_CONSTRUCTORS);
    }
}
