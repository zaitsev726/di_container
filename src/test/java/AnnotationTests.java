import diContainer.DiContainer;
import diContainer.IDiContainer;
import diContainer.exampleClasses.ClassWithAnnotatedMethod;
import diContainer.exampleClasses.ExampleClassB;
import diContainer.exampleClasses.ExampleClassC;

import diContainer.exampleClasses.annotations.ClassAutowiredFields;
import diContainer.exampleClasses.annotations.ClassWithConstructAndDestroy;
import diContainer.exampleClasses.annotations.ClassWithSetterInjection;
import exceptions.ContainerException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import annotations.Autowired;
import workWithAnnotations.AnnotationUtil;

public class AnnotationTests {

    private IDiContainer container;

    @BeforeEach
    public void setUp() {
        container = new DiContainer(true);
    }

    @Test
    public void getMethodsAnnotatedWith() throws NoSuchMethodException {
        List<Method> result = AnnotationUtil.getMethodsAnnotatedWith(ClassWithAnnotatedMethod.class, Autowired.class);
        Assertions.assertEquals(result.size(), 2);
        Assertions.assertTrue(result.contains(ClassWithAnnotatedMethod.class.getMethod("annotatedMethod1")));
        Assertions.assertTrue(result.contains(ClassWithAnnotatedMethod.class.getMethod("annotatedMethod2")));
    }

    @Test
    public void autowiredFieldsTest() throws ContainerException, IllegalAccessException, InvocationTargetException {
        ExampleClassB exampleClassB = new ExampleClassB();
        ExampleClassC exampleClassC = new ExampleClassC();
        ClassAutowiredFields clazz = new ClassAutowiredFields();
        container.register(exampleClassB);
        container.register(exampleClassC);
        container.register(clazz);
        ClassAutowiredFields object = container.resolveByClass(clazz.getClass());
        Assertions.assertNotNull(object.getExampleClassA());
        Assertions.assertNotNull(object.getExampleClassB());
    }

    @Test
    public void autowiredConstructorTest() throws ContainerException, IllegalAccessException, InvocationTargetException {
        ExampleClassB exampleClassB = new ExampleClassB();
        ExampleClassC exampleClassC = new ExampleClassC();
        ClassAutowiredFields clazz = new ClassAutowiredFields();
        container.register(exampleClassB);
        container.register(exampleClassC);
        container.register(clazz);
        ClassAutowiredFields object = container.resolveByClass(clazz.getClass());
        Assertions.assertNotNull(object.getExampleClassA());
        Assertions.assertNotNull(object.getExampleClassB());
    }

    @Test
    public void autowiredSetterTest() throws ContainerException, IllegalAccessException, InvocationTargetException {
        ExampleClassB exampleClassB = new ExampleClassB();
        ExampleClassC exampleClassC = new ExampleClassC();
        ClassWithSetterInjection clazz = new ClassWithSetterInjection();
        container.register(exampleClassB);
        container.register(exampleClassC);
        container.register(clazz);
        ClassWithSetterInjection object = container.resolveByClass(clazz.getClass());
        Assertions.assertNotNull(object.getExampleClassC());
        Assertions.assertNotNull(object.getExampleClassB());
    }

    @Test
    public void preDestroyAndPostConstructTest() throws ContainerException, IllegalAccessException, InvocationTargetException {
        ClassWithConstructAndDestroy clazz = new ClassWithConstructAndDestroy();
        container.register(clazz);
        ClassWithConstructAndDestroy object = container.resolveByClass(clazz.getClass());
        Assertions.assertTrue(ClassWithConstructAndDestroy.IS_INITIALIZED);
        container.unregister(object.getClass());
        Assertions.assertFalse(ClassWithConstructAndDestroy.IS_INITIALIZED);
    }
}
