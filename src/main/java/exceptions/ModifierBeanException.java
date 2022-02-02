package exceptions;

public class ModifierBeanException extends ContainerException {

    public ModifierBeanException(String errorMessage) {
        super(errorMessage);
    }

    @Override
    public String toString() {
        return "ModifierBeanException: " + errorMessage;
    }
}
