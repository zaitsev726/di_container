package exceptions;

public class NoSuchBeanException extends ContainerException {

    public NoSuchBeanException(String errorMessage) {
        super(errorMessage);
    }

    @Override
    public String toString() {
        return "NoSuchBeanException: " + errorMessage;
    }
}
