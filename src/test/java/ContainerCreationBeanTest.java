import diContainer.DiContainer;
import diContainer.IDiContainer;
import diContainer.exampleClasses.*;
import exceptions.ContainerException;
import exceptions.CyclicDependenciesException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import scope.TypeScope;

import java.lang.reflect.InvocationTargetException;

public class ContainerCreationBeanTest {

    private IDiContainer container;

    @BeforeEach
    public void setUp() {
        container = new DiContainer(true);
    }

    @Test
    void containerShouldCreateNotExistedObjectWithoutParametersInConstructorTest() throws ContainerException, InvocationTargetException, IllegalAccessException {
        ExampleClassB classB = container.resolveByClass(ExampleClassB.class);
        ExampleClassC classC = container.resolveByClass(ExampleClassC.class);
        Assertions.assertNotEquals(classB.value, classC.value);
    }

    @Test
    void containerShouldCreateNotExistedObjectWithParametersInConstructorTest() {
        Assertions.assertDoesNotThrow(() -> container.resolveByClass(ExampleClassA.class));
    }

    @Test
    void containerShouldCreateNotExistedObjectByFirstSuitableConstructorTest() {
        Assertions.assertDoesNotThrow(() -> {
            ExampleClassD classD = container.resolveFirst(ExampleClassD.class);
            Assertions.assertNull(classD.classA);
            Assertions.assertNull(classD.classB);
        });
    }

    @Test
    void containerShouldCreateNewCopyOfTransientClassEveryTimeTest() {
        Assertions.assertDoesNotThrow(() -> container.registerLazyBean(ExampleClassB.class, TypeScope.TRANSIENT));

        Assertions.assertDoesNotThrow(() -> {
            ExampleClassB firstCall = container.resolveByClass(ExampleClassB.class);
            ExampleClassB secondCall = container.resolveByClass(ExampleClassB.class);
            Assertions.assertNotEquals(firstCall.value, secondCall.value);

            ExampleClassB thirdCall = container.resolveByClass(ExampleClassB.class);
            Assertions.assertNotEquals(firstCall.value, thirdCall.value);
            Assertions.assertNotEquals(secondCall.value, thirdCall.value);
        });
    }

    @Test
    void containerCreateSimpleCyclicBeanTest() {
        Assertions.assertThrows(CyclicDependenciesException.class, () -> container.resolveSingle(CyclicClassA.class));
    }

    @Test
    void containerCreateTwoStagesCyclicBeanTest() {
        Assertions.assertThrows(CyclicDependenciesException.class, () -> container.resolveSingle(CyclicClassC.class));
    }

    @Test
    void containerTakeExistedBeanTest() {
        Assertions.assertDoesNotThrow(() -> {
            var classB = new ExampleClassB();
            container.register(classB);

            ExampleClassA classA = container.resolveSingle(ExampleClassA.class);
            Assertions.assertEquals(classB.value, classA.exampleClassB.value);
        });
    }
}
