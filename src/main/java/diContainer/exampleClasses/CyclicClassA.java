package diContainer.exampleClasses;

public class CyclicClassA {
    public CyclicClassB cyclicClassB;

    public CyclicClassA(CyclicClassB cyclicClassB) {
        this.cyclicClassB = cyclicClassB;
    }
}
