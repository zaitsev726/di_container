package BeanPostProcessor;

import diContainer.DiContainer;
import workWithAnnotations.support.AutowiredFieldHelper;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

public class FieldResolveInvoker {
    public <T> void invokeFields(DiContainer diContainer, T bean) throws InvocationTargetException, IllegalAccessException {
        AutowiredFieldHelper fieldWireSupport = new AutowiredFieldHelper();
        List<Field> fieldsDependencies = fieldWireSupport.getFieldDependencies(bean.getClass());
        for(var field: fieldsDependencies){
            try {
                Object objectToSet = diContainer.resolveByClass(field.getType());
                if (objectToSet != null) {
                    field.setAccessible(true);
                    field.set(bean, objectToSet);
                }
            } catch (Exception exception) {
                throw new IllegalStateException("Unable to call setter " + field, exception);
            }
        }
    }

}
