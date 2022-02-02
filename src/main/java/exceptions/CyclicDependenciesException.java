package exceptions;

public class CyclicDependenciesException extends ContainerException {
    public CyclicDependenciesException(String errorMessage) {
        super(errorMessage);
    }

    @Override
    public String toString() {
        return "CyclicDependenciesException: " + errorMessage;
    }
}
