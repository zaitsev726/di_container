import diContainer.DiContainer;
import diContainer.IDiContainer;
import diContainer.exampleClasses.ExampleClassC;
import exceptions.ContainerException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;

public class ContainerHierarchyTest {

    private IDiContainer container;
    private IDiContainer mainContainer;

    @BeforeEach
    void setUp() {
        mainContainer = new DiContainer();

        container = new DiContainer(true, mainContainer);
    }

    @Test
    public void registerBeanByObjectWithParentTest() throws ContainerException, InvocationTargetException, IllegalAccessException {
        var classC = new ExampleClassC();
        mainContainer.register(classC);

        var result = container.resolveByClass(classC.getClass());

        Assertions.assertEquals(classC.getClass().getName(), result.getClass().getName());
        Assertions.assertEquals(classC.getClass(), result.getClass());
        var castedResult = (ExampleClassC) result;
        Assertions.assertEquals(classC.value, castedResult.value);
    }

    @Test
    public void registerBeanByObjectAndNameWithParentTest() throws ContainerException, InvocationTargetException, IllegalAccessException {
        var classC = new ExampleClassC();
        var name = "SomeDefaultName";
        mainContainer.register(classC, name);

        var result = container.resolveByName(name);

        Assertions.assertEquals(classC.getClass().getName(), result.getClass().getName());
        Assertions.assertEquals(classC.getClass(), result.getClass());
        var castedResult = (ExampleClassC) result;
        Assertions.assertEquals(classC.value, castedResult.value);
    }
}
