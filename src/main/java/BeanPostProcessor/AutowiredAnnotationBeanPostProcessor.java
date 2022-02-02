package BeanPostProcessor;

import diContainer.DiContainer;
import java.lang.reflect.InvocationTargetException;


public class AutowiredAnnotationBeanPostProcessor {
    private DiContainer diContainer;

    public <T> void processInjection(T bean) throws InvocationTargetException, IllegalAccessException {
        FieldResolveInvoker fieldResolveInvoker = new FieldResolveInvoker();
        SetterInvoker setterInvoker = new SetterInvoker();
        fieldResolveInvoker.invokeFields(diContainer, bean);
        setterInvoker.invokeSetters(diContainer, bean);
    }

    public void setDiContainer(DiContainer diContainer){
        this.diContainer = diContainer;
    }
}
