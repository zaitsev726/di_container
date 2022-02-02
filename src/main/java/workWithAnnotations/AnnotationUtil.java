package workWithAnnotations;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

public class AnnotationUtil {

    public static Set<DiAnnotation> getAnnotationsOfType(AnnotatedElement annotatedElement, Class<? extends Annotation> annotationClass) {
        Set<DiAnnotation> result = new HashSet<>();
        var annotations =  annotatedElement.getAnnotations();
        for(Annotation annotation: annotations) {
            Class<? extends Annotation> type = annotation.annotationType();

            Map<String, Object> attributes = new HashMap<>();
            for (Method method : type.getMethods()) {
                if (method.getParameterTypes().length == 0) {
                    String attribute = method.getName();
                    Object value = getAnnotationValue(annotation, method);
                    attributes.put(attribute, value);
                }
            }
            DiAnnotation diAnnotation = new DiAnnotation(annotation, attributes);

            if (!doesContainItself(result, annotation)) {
                result.add(diAnnotation);
            }
        }
         result = result.stream()
                .filter(annotation -> annotation.getType().annotationType().equals(annotationClass))
                .collect(Collectors.toSet());
        return result;
    }

    private static boolean doesContainItself(Set<DiAnnotation> result, Annotation annotation) {
        return result.stream()
                .anyMatch(a -> a.getType().equals(annotation));
    }

    private static Object getAnnotationValue(Annotation annotation, Method method) {
        try {
            return method.invoke(annotation);
        } catch (Exception e) {
            throw new RuntimeException("Unable to get annotation value", e);
        }
    }

    public static boolean hasAnnotation(AnnotatedElement parameter, Class<? extends Annotation> annotation) {
        return getAnnotationsOfType(parameter, annotation).size() > 0;
    }

    public static List<Method> getMethodsAnnotatedWith(Class<?> beanClass, Class<? extends Annotation> annotation) {
        var methods = new ArrayList<Method>();
        while (beanClass != Object.class) {
            var allMethods = new ArrayList<Method>(Arrays.asList(beanClass.getDeclaredMethods()));
            for (final Method method : allMethods) {
                if (method.isAnnotationPresent(annotation)) {
                    methods.add(method);
                }
            }
            beanClass = beanClass.getSuperclass();
        }
        return methods;
    }

}
