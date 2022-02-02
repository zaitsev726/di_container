package diContainer.exampleClasses;

import annotations.Autowired;

public class ExampleClassA {
    @Autowired
    public ExampleClassB exampleClassB;

    @Autowired
    public ExampleClassC exampleClassC;

    public ExampleClassA(ExampleClassB exampleClassB, ExampleClassC exampleClassC) {
        this.exampleClassB = exampleClassB;
        this.exampleClassC = exampleClassC;
    }

    public class SomeTestClass extends AbstractClass {

    }
}
