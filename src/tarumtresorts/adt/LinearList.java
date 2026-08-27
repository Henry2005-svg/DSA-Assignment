package tarumtresorts.adt;

/** Author: <Your Name>. Custom linked linear ADT; no Java Collections used. */
public class LinearList<T> {
    private static final class Node<E> {
        E value;
        Node<E> next;

        Node(E v) {
            value = v;
        }
    }

    private Node<T> head, tail;
    private int size;

    public void add(T value) {
        Node<T> n = new Node<T>(value);
        if (head == null)
            head = n;
        else
            tail.next = n;
        tail = n;
        size++;
    }

    public T get(int index) {
        return node(index).value;
    }

    public T remove(int index) {
        if (index < 0 || index >= size)
            throw new IndexOutOfBoundsException();
        Node<T> old;
        if (index == 0) {
            old = head;
            head = head.next;
        } else {
            Node<T> p = node(index - 1);
            old = p.next;
            p.next = old.next;
            if (old == tail)
                tail = p;
        }
        if (--size == 0)
            tail = null;
        return old.value;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public void clear() {
        head = tail = null;
        size = 0;
    }

    public Object[] toArray() {
        Object[] a = new Object[size];
        Node<T> n = head;
        for (int i = 0; i < size; i++) {
            a[i] = n.value;
            n = n.next;
        }
        return a;
    }

    private Node<T> node(int index) {
        if (index < 0 || index >= size)
            throw new IndexOutOfBoundsException();
        Node<T> n = head;
        for (int i = 0; i < index; i++)
            n = n.next;
        return n;
    }
}
