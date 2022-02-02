package exceptions;

public class ResolveBeanException extends ContainerException {
    public ResolveBeanException(String errorMessage) {
        super(errorMessage);
    }

    @Override
    public String toString() {
        return "ResolveBeanException: " + errorMessage;
    }
}
