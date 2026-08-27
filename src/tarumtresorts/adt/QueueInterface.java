package tarumtresorts.adt;

/**
 * Author: <Your Name>. Team-assessed ADT contract; acknowledge any adapted
 * course source here.
 */
public interface QueueInterface<T> {
    boolean enqueue(T item);

    T dequeue();

    T getFront();

    boolean isEmpty();

    boolean isFull();

    int size();

    void clear();

    Object[] toArray();
}
