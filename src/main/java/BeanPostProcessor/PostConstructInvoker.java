package BeanPostProcessor;

import annotations.PostConstruct;
import workWithAnnotations.AnnotationUtil;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

public class PostConstructInvoker {
    public void invokePostConstructMethods(Object bean) {
        List<Method> postConstructMethods = AnnotationUtil.getMethodsAnnotatedWith(bean.getClass(), PostConstruct.class);
        postConstructMethods.forEach(method -> callPostConstructMethod(bean, method));
    }
    private void callPostConstructMethod(Object bean, Method method) {
        try {
            method.setAccessible(true);
            method.invoke(bean);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
}
