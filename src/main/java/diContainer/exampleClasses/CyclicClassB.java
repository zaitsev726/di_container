package diContainer.exampleClasses;

public class CyclicClassB {
    public CyclicClassA cyclicClassA;

    public CyclicClassB(CyclicClassA cyclicClassA) {
        this.cyclicClassA = cyclicClassA;
    }
}
