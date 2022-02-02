package diContainer.exampleClasses.annotations;

import annotations.Autowired;
import diContainer.exampleClasses.ExampleClassB;
import diContainer.exampleClasses.ExampleClassC;


public class ClassWithSetterInjection {
    private ExampleClassC exampleClassC;

    @Autowired
    private ExampleClassB exampleClassB;


    @Autowired
    public void setExampleClassC(ExampleClassC exampleClassC) {
        this.exampleClassC = exampleClassC;
    }

    public ExampleClassC getExampleClassC() {
        return exampleClassC;
    }

    public ExampleClassB getExampleClassB() {
        return exampleClassB;
    }
}

