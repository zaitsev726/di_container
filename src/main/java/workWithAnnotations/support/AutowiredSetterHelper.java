package workWithAnnotations.support;

import annotations.Autowired;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static workWithAnnotations.AnnotationUtil.hasAnnotation;

public class AutowiredSetterHelper {
    public List<Method> getSetterDependencies(Class<?> clazz) {
        return new ArrayList<>(collectInjectMethods(clazz));
    }

    private List<Method> collectInjectMethods(Class<?> clazz) {
        return Arrays.stream(clazz.getDeclaredMethods()).filter(this::isAutowiredSetter).toList();
    }

    private boolean isAutowiredSetter(Method method) {
        return hasAnnotation(method, Autowired.class) && hasSetterName(method);
    }

    private boolean hasSetterName(Method method) {
        String methodName = method.getName();
        return methodName.startsWith("set") || methodName.startsWith("is");
    }
}
