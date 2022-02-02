import diContainer.DiContainer;
import diContainer.IDiContainer;
import diContainer.exampleClasses.*;
import exceptions.ContainerException;
import exceptions.ModifierBeanException;
import exceptions.MultipleBeanDefinitionException;
import exceptions.NoSuchBeanException;
import lambdaSupport.LambdaBinary;
import lambdaSupport.LambdaExpression;
import lambdaSupport.LambdaSupplier;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;

public class ContainerMethodsTest {
    private IDiContainer container;
    private ExampleClassB classB;

    @BeforeEach
    void setUp() throws ContainerException {
        container = new DiContainer();
        classB = new ExampleClassB();

        container.register(classB, classB.getClass().getName());
    }

    @Test
    public void registerBeanByObjectTest() throws ContainerException, InvocationTargetException, IllegalAccessException {
        var classC = new ExampleClassC();
        container.register(classC);

        var result = container.resolveByClass(classC.getClass());

        Assertions.assertEquals(classC.getClass().getName(), result.getClass().getName());
        Assertions.assertEquals(classC.getClass(), result.getClass());
        var castedResult = (ExampleClassC) result;
        Assertions.assertEquals(classC.value, castedResult.value);
    }

    @Test
    public void registerBeanByObjectAndNameTest() throws ContainerException, InvocationTargetException, IllegalAccessException {
        var classC = new ExampleClassC();
        var name = "SomeDefaultName";
        container.register(classC, name);

        ExampleClassC result = container.resolveByName(name);

        Assertions.assertEquals(classC.getClass().getName(), result.getClass().getName());
        Assertions.assertEquals(classC.getClass(), result.getClass());
        Assertions.assertEquals(classC.value, result.value);
    }

    @Test
    public void registerBeansWithSameNameShouldThrowMultipleBeanDefinitionException() {
        var classB = new ExampleClassB();
        Assertions.assertThrows(MultipleBeanDefinitionException.class, () -> container.register(classB));
    }

    @Test
    public void resolveByNameNoSuchBeanExceptionTest() throws ContainerException {
        Assertions.assertThrows(NoSuchBeanException.class, () -> container.resolveByClass(ExampleClassC.class));
        var classC = new ExampleClassC();
        container.register(classC);

        Assertions.assertThrows(NoSuchBeanException.class, () -> container.resolveByName("SomeName"));
    }

    @Test
    public void resolveByClassMultipleBeanDefinitionExceptionTest() throws ContainerException {
        var classB = new ExampleClassB();
        container.register(classB, "SomeOtherName");

        Assertions.assertThrows(MultipleBeanDefinitionException.class, () -> container.resolveByClass(ExampleClassB.class));
    }

    @Test
    public void resolveByInterfaceTest() throws ContainerException {
        ExampleClassB result = container.resolveByInterface(SomeInterface.class);

        Assertions.assertEquals(classB.getClass().getName(), result.getClass().getName());
        Assertions.assertEquals(classB.getClass(), result.getClass());
        Assertions.assertEquals(classB.value, result.value);
    }

    @Test
    public void resolveByInterfaceMultipleBeanDefinitionExceptionTest() throws ContainerException {
        var classC = new ExampleClassC();
        container.register(classC);

        Assertions.assertThrows(MultipleBeanDefinitionException.class, () -> container.resolveByInterface(SomeInterface.class));
    }

    @Test
    public void resolveByClassOrInterfaceWithSingleClassTest() throws ContainerException, InvocationTargetException, IllegalAccessException {
        ExampleClassB result = container.resolveByClassOrInterface(ExampleClassB.class);
        Assertions.assertEquals(classB.getClass().getName(), result.getClass().getName());
        Assertions.assertEquals(classB.getClass(), result.getClass());
        Assertions.assertEquals(classB.value, result.value);

        result = container.resolveByClassOrInterface(SomeInterface.class);
        Assertions.assertEquals(classB.getClass().getName(), result.getClass().getName());
        Assertions.assertEquals(classB.getClass(), result.getClass());
        Assertions.assertEquals(classB.value, result.value);
    }

    @Test
    public void resolveByClassOrInterfaceWithMultipleClassesTest() throws ContainerException, InvocationTargetException, IllegalAccessException {
        var classC = new ExampleClassC();
        container.register(classC);

        Assertions.assertNotEquals(classB.value, classC.value);

        ExampleClassB resultB = container.resolveByClassOrInterface(ExampleClassB.class);
        Assertions.assertEquals(classB.getClass().getName(), resultB.getClass().getName());
        Assertions.assertEquals(classB.getClass(), resultB.getClass());
        Assertions.assertEquals(classB.value, resultB.value);

        ExampleClassC resultC = container.resolveByClassOrInterface(ExampleClassC.class);
        Assertions.assertEquals(classC.getClass().getName(), resultC.getClass().getName());
        Assertions.assertEquals(classC.getClass(), resultC.getClass());
        Assertions.assertEquals(classC.value, resultC.value);
    }

    @Test
    public void resolveByClassOrInterfaceMultipleBeanDefinitionExceptionTest() throws ContainerException {
        var classC = new ExampleClassC();
        container.register(classC);

        Assertions.assertNotEquals(classB.value, classC.value);
        Assertions.assertThrows(MultipleBeanDefinitionException.class, () -> container.resolveByClassOrInterface(SomeInterface.class));
        Assertions.assertThrows(MultipleBeanDefinitionException.class, () -> container.resolveByClassOrInterface(Object.class));
    }

    @Test
    public void resolveByClassOrInterfaceNoSuchBeanExceptionTest() throws ContainerException {
        var classC = new ExampleClassC();
        container.register(classC);

        Assertions.assertNotEquals(classB.value, classC.value);
        Assertions.assertThrows(NoSuchBeanException.class, () -> container.resolveByClassOrInterface(Runnable.class));
        Assertions.assertThrows(NoSuchBeanException.class, () -> container.resolveByClassOrInterface(String.class));
    }

    @Test
    public void unregisterByNameTest() {
        var className = classB.getClass().getName();
        container.unregister(className);

        Assertions.assertThrows(NoSuchBeanException.class, () -> container.resolveByName(className));
        Assertions.assertThrows(NoSuchBeanException.class, () -> container.resolveByClass(classB.getClass()));
    }

    @Test
    public void unregisterByClassRemoveSingleBeanTest() {
        var className = classB.getClass().getName();
        container.unregister(classB.getClass());
        Assertions.assertThrows(NoSuchBeanException.class, () -> container.resolveByName(className));
        Assertions.assertThrows(NoSuchBeanException.class, () -> container.resolveByClass(classB.getClass()));
    }

    @Test
    public void unregisterByClassRemoveMultipleBeanTest() throws ContainerException {
        var classC = new ExampleClassC();
        container.register(classC);

        Assertions.assertNotEquals(classB.value, classC.value);

        var classNameB = classB.getClass().getName();
        var classNameC = classC.getClass().getName();

        container.unregister(Object.class);
        Assertions.assertThrows(NoSuchBeanException.class, () -> container.resolveByName(classNameB));
        Assertions.assertThrows(NoSuchBeanException.class, () -> container.resolveByName(classNameC));
        Assertions.assertThrows(NoSuchBeanException.class, () -> container.resolveByClass(classB.getClass()));
        Assertions.assertThrows(NoSuchBeanException.class, () -> container.resolveByClass(classC.getClass()));
        Assertions.assertThrows(NoSuchBeanException.class, () -> container.resolveByClass(Object.class));
    }

    @Test
    public void resolveAbstractClassWithOneRegisteredRealizationTest() {
        Assertions.assertDoesNotThrow(() -> {
            var realizationA = new AbstractRealizationA();
            container.register(realizationA);
            var expectedA = (AbstractRealizationA) container.resolveByClassOrInterface(AbstractClass.class);
            Assertions.assertEquals(realizationA.value, expectedA.value);
        });
    }

    @Test
    public void resolveAbstractClassWithTwoRegisteredRealizationTest() {
        Assertions.assertThrows(MultipleBeanDefinitionException.class, () -> {
            var realizationA = new AbstractRealizationA();
            var realizationB = new AbstractRealizationB();
            container.register(realizationA);
            container.register(realizationB);

            container.resolveByClassOrInterface(AbstractClass.class);
        });
    }

    @Test
    public void resolveAbstractClassWithNoneRegisteredRealizationTest() {
        Assertions.assertThrows(NoSuchBeanException.class, () -> container.resolveByClassOrInterface(AbstractClass.class));

        Assertions.assertThrows(ModifierBeanException.class, () -> container.resolveSingle(AbstractClass.class));

        Assertions.assertThrows(ModifierBeanException.class, () -> container.resolveFirst(AbstractClass.class));

        Assertions.assertDoesNotThrow(() -> {
            var result = container.resolveAll(AbstractClass.class);
            Assertions.assertEquals(result.get(0).getClass(), AbstractRealizationA.class);
            Assertions.assertEquals(result.get(1).getClass(), AbstractRealizationB.class);
        });
    }

    @Test
    public void lambdaSupportTest() {
        Assertions.assertDoesNotThrow(() -> {
            container.register(() -> {
                System.out.println("Lambda Expression Executed");
            });

            LambdaExpression expression = container.resolveByInterface(LambdaExpression.class);
            expression.run();
        });

        Assertions.assertDoesNotThrow(() -> {
            container.register(() -> {
                System.out.println("Lambda Supplier Executed");
                return 1000;
            });

            LambdaSupplier<Integer> expression = container.resolveByInterface(LambdaSupplier.class);
            Assertions.assertEquals(1000, expression.run());
        });

        Assertions.assertDoesNotThrow(() -> {
            container.register((Integer first, Integer second) -> {
                System.out.println("Lambda Binary Executed");
                return first - second;
            });

            LambdaBinary<Integer> expression = container.resolveByInterface(LambdaBinary.class);
            Assertions.assertEquals(500, expression.run(1000, 500));
        });
    }

}
