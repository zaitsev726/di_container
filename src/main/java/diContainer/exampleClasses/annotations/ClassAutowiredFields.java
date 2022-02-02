package diContainer.exampleClasses.annotations;

import annotations.Autowired;
import diContainer.exampleClasses.ExampleClassA;
import diContainer.exampleClasses.ExampleClassB;


public class ClassAutowiredFields {
    @Autowired
    private ExampleClassA exampleClassA;

    @Autowired
    private ExampleClassB exampleClassB;

    public ExampleClassA getExampleClassA() {
        return exampleClassA;
    }

    public ExampleClassB getExampleClassB() {
        return exampleClassB;
    }
}
