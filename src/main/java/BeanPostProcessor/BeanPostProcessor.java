package BeanPostProcessor;

import annotations.PostConstruct;
import annotations.PreDestroy;
import workWithAnnotations.AnnotationUtil;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

public class BeanPostProcessor {
    Object postProcessBeforeInitialization(Object bean) throws InvocationTargetException, IllegalAccessException {
        List<Method> postConstructMethods = AnnotationUtil.getMethodsAnnotatedWith(bean.getClass(), PostConstruct.class);
        for(var method: postConstructMethods) {
            method.setAccessible(true);
            method.invoke(bean);
        }
        return bean;
    }

    Object postProcessAfterInitialization(Object bean) throws InvocationTargetException, IllegalAccessException {
        List<Method> preDestroyMethods = AnnotationUtil.getMethodsAnnotatedWith(bean.getClass(), PreDestroy.class);
        for(var method: preDestroyMethods) {
            method.setAccessible(true);
            method.invoke(bean);
        }
        return bean;
    }
}
