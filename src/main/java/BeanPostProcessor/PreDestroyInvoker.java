package BeanPostProcessor;

import annotations.PreDestroy;
import workWithAnnotations.AnnotationUtil;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

public class PreDestroyInvoker {
    public void invokePostConstructMethods(Object bean) {
        List<Method> preDestroyMethods = AnnotationUtil.getMethodsAnnotatedWith(bean.getClass(), PreDestroy.class);
        preDestroyMethods.forEach(method -> callPostConstructMethod(bean, method));
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
