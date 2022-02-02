package diContainer;

import exceptions.ContainerException;
import lambdaSupport.LambdaBinary;
import lambdaSupport.LambdaExpression;
import lambdaSupport.LambdaSupplier;
import scope.TypeScope;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

public interface IDiContainer {
    void register(Object object) throws ContainerException;
    void register(LambdaExpression expression) throws ContainerException;
    <T> void register(LambdaSupplier<T> expression) throws ContainerException;
    <T> void register(LambdaBinary<T> expression) throws ContainerException;
    void register(Object object, String name) throws ContainerException;
    void register(Object object, String name, TypeScope scope) throws ContainerException;
    void registerLazyBean(Class<?> beanClass, TypeScope scope) throws ContainerException;
    <T> T resolveByClassOrInterface(Class<?> bean) throws ContainerException, InvocationTargetException, IllegalAccessException;
    <T> T resolveByClass(Class<?> beanClass) throws ContainerException, InvocationTargetException, IllegalAccessException;
    <T> T resolveByInterface(Class<?> beanInterface) throws ContainerException;
    <T> T resolveByName(String name) throws ContainerException, InvocationTargetException, IllegalAccessException;

    <T> T resolveSingle(Class<?> beanClass) throws ContainerException, InvocationTargetException, IllegalAccessException;
    <T> T resolveFirst(Class<?> beanClass) throws ContainerException, InvocationTargetException, IllegalAccessException;
    <T> List<? extends T> resolveAll(Class<?> beanClass) throws ContainerException;

    void unregister(String name);
    void unregister(Class<?> bean);

    void dispose();
}
