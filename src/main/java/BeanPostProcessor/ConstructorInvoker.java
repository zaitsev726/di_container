package BeanPostProcessor;

import diContainer.DiContainer;
import exceptions.ContainerException;
import workWithAnnotations.support.AutowiredConstructorHelper;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class ConstructorInvoker {
    private Object invokeConstructor(DiContainer diContainer, Class<?> beanClass)
            throws InvocationTargetException, InstantiationException, IllegalAccessException, ContainerException {

        AutowiredConstructorHelper autowiredConstructorHelper = new AutowiredConstructorHelper();
        Constructor<?> beanConstructor = autowiredConstructorHelper.getConstructor(beanClass);
        if (beanConstructor.getParameterCount() == 0) {
            return beanConstructor.newInstance((Object[]) null);
        }

        var i = 0;
        var resolvedParams = new Object[beanConstructor.getParameterCount()];
        for (var param : beanConstructor.getParameters()) {
            resolvedParams[i] = diContainer.resolveByClass(param.getType());
            i++;
        }
        return beanConstructor.newInstance(resolvedParams);
    }
}
