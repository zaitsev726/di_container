package exceptions;

public class MultipleBeanDefinitionException extends ContainerException {

    public MultipleBeanDefinitionException(String errorMessage) {
        super(errorMessage);
    }

    @Override
    public String toString() {
        return "MultipleBeanDefinitionException: " + errorMessage;
    }
}
