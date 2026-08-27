package tarumtresorts.adt;

/**
 * Author: <Your Name>. Circular array queue: O(1) front/dequeue, amortized O(1)
 * enqueue.
 */
public class CircularArrayQueue<T> implements QueueInterface<T> {
    private T[] items;
    private int front, count;

    @SuppressWarnings("unchecked")
    public CircularArrayQueue(int capacity) {
        items = (T[]) new Object[Math.max(1, capacity)];
    }

    public boolean enqueue(T item) {
        if (item == null)
            throw new IllegalArgumentException("Null item");
        if (isFull())
            grow();
        items[(front + count) % items.length] = item;
        count++;
        return true;
    }

    public T dequeue() {
        if (isEmpty())
            return null;
        T value = items[front];
        items[front] = null;
        front = (front + 1) % items.length;
        count--;
        return value;
    }

    public T getFront() {
        return isEmpty() ? null : items[front];
    }

    public boolean isEmpty() {
        return count == 0;
    }

    public boolean isFull() {
        return count == items.length;
    }

    public int size() {
        return count;
    }

    @SuppressWarnings("unchecked")
    public void clear() {
        items = (T[]) new Object[items.length];
        front = 0;
        count = 0;
    }

    public Object[] toArray() {
        Object[] out = new Object[count];
        for (int i = 0; i < count; i++)
            out[i] = items[(front + i) % items.length];
        return out;
    }

    @SuppressWarnings("unchecked")
    private void grow() {
        T[] next = (T[]) new Object[items.length * 2];
        for (int i = 0; i < count; i++)
            next[i] = items[(front + i) % items.length];
        items = next;
        front = 0;
    }
}
