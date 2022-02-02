package diContainer.exampleClasses.annotations;

import annotations.PostConstruct;
import annotations.PreDestroy;

public class ClassWithConstructAndDestroy {
    public static boolean IS_INITIALIZED = false;

    @PostConstruct
    public void init() {
        IS_INITIALIZED = true;
    }

    @PreDestroy
    public void destroy() {
        IS_INITIALIZED = false;
    }
}
