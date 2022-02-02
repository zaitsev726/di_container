package workWithAnnotations.support;

import annotations.Autowired;
import workWithAnnotations.AnnotationUtil;

import java.lang.reflect.Constructor;
import java.util.*;

public class AutowiredConstructorHelper {
    public Constructor<?> getConstructor(Class<?> clazz) {
        Optional<Constructor<?>> constructorToUseOptional = getConstructorToUse(clazz);
        return constructorToUseOptional.orElse(null);
    }

    private Optional<Constructor<?>> getConstructorToUse(Class<?> clazz) {
        List<Constructor<?>> constructors = Arrays.asList(clazz.getConstructors());
        if (constructors.size() == 1) {
            return Optional.of(constructors.get(0));
        } else if (constructors.size() > 1) {
            List<Constructor<?>> autowiredAnnotatedConstructors = constructors.stream()
                    .filter(constructor -> AnnotationUtil.hasAnnotation(constructor, Autowired.class)).toList();
            if (autowiredAnnotatedConstructors.size() == 1) {
                return Optional.of(autowiredAnnotatedConstructors.get(0));
            } else {
                throw new IllegalArgumentException("No unambiguous public constructor found for " + clazz
                        + ", either create one constructor only or annotatate only one constructor with @Autowired");
            }
        }
        return Optional.empty();
    }
}
