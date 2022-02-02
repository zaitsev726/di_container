package diContainer;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Node {
    private final Class<?> head;
    private final List<Class<?>> transitions;

    public Node(Class<?> head) {
        this.head = head;
        transitions = new ArrayList<>();
    }

    public Node(Class<?> head, List<Class<?>> transitions) {
        this.head = head;
        this.transitions = transitions;
    }

    public void addNewTransition(Class<?> transition) {
        transitions.add(transition);
    }

    public List<Class<?>> getTransitions() {
        return transitions;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Node node = (Node) o;
        return head.equals(node.head) && Objects.equals(transitions, node.transitions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(head, transitions);
    }
}
