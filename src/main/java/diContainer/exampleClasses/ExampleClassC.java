package diContainer.exampleClasses;

import java.util.Random;

public class ExampleClassC implements SomeInterface {
    public int value = new Random().nextInt(100000);

    @Override
    public String toString() {
        return "TestPrototypeBox{" +
                "value=" + value +
                '}';
    }
}