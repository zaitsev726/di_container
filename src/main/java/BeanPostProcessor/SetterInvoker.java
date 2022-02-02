package BeanPostProcessor;

import diContainer.DiContainer;
import exceptions.ContainerException;
import workWithAnnotations.support.AutowiredSetterHelper;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;

public class SetterInvoker {

    public <T> void invokeSetters(DiContainer container, T bean) throws InvocationTargetException, IllegalAccessException {
        AutowiredSetterHelper setterWireSupport = new AutowiredSetterHelper();

        List<Method> setterDependencies = setterWireSupport.getSetterDependencies(bean.getClass());
        for(var setterDependency: setterDependencies) {
            Parameter[] parameters = setterDependency.getParameters();
            Object[] arguments = Arrays.stream(parameters).map(parameter -> {
                try {
                    return container.resolveByClass(parameter.getType());
                } catch (ContainerException | IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }
                return null;
            }).toList().toArray();
            setterDependency.setAccessible(true);
            setterDependency.invoke(bean, arguments);
        }
    }

}
