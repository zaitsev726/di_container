package diContainer.exampleClasses;

public class CyclicClassC {
    public CyclicClassB cyclicClassB;

    public CyclicClassC(CyclicClassB cyclicClassB) {
        this.cyclicClassB = cyclicClassB;
    }
}
