package exceptions;

public class MultipleConstructorException extends ContainerException {

    public MultipleConstructorException(String errorMessage) {
        super(errorMessage);
    }

    @Override
    public String toString() {
        return "MultipleConstructorException: " + errorMessage;
    }

}
