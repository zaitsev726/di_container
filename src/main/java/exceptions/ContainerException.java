package exceptions;

public class ContainerException extends Exception {

    public String errorMessage;

    public ContainerException(String errorMessage){
        super(errorMessage);
        this.errorMessage = errorMessage;
    }

    @Override
    public String toString(){
        return errorMessage;
    }
}
