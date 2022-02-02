package workWithAnnotations.support;

import annotations.Autowired;
import workWithAnnotations.AnnotationUtil;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
public class AutowiredFieldHelper {
    public List<Field> getFieldDependencies(Class<?> clazz) {
        return new ArrayList<>(collectAutowiredFields(clazz));
    }
    private List<Field> collectAutowiredFields(Class<?> clazz) {
        return Arrays.stream(clazz.getDeclaredFields())
                .filter(this::isAnnotated).toList();
    }

    private boolean isAnnotated(Field field) {
        return AnnotationUtil.getAnnotationsOfType(field, Autowired.class).size() > 0;
    }
}
