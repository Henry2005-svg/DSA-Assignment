package tarumtresorts.adt;

/** Author: <Your Name>. Ordinary BST: average O(log n), skewed worst O(n). */
public class BinarySearchTree<K extends Comparable<K>, V> {
    public static final class Entry<K, V> {
        public final K key;
        public final V value;

        Entry(K k, V v) {
            key = k;
            value = v;
        }
    }

    private final class Node {
        K key;
        V value;
        Node left, right;

        Node(K k, V v) {
            key = k;
            value = v;
        }
    }

    private Node root;
    private int size;

    public boolean insert(K key, V value) {
        if (root == null) {
            root = new Node(key, value);
            size = 1;
            return true;
        }
        Node n = root;
        while (true) {
            int c = key.compareTo(n.key);
            if (c == 0)
                return false;
            if (c < 0) {
                if (n.left == null) {
                    n.left = new Node(key, value);
                    size++;
                    return true;
                }
                n = n.left;
            } else {
                if (n.right == null) {
                    n.right = new Node(key, value);
                    size++;
                    return true;
                }
                n = n.right;
            }
        }
    }

    public V search(K key) {
        Node n = root;
        while (n != null) {
            int c = key.compareTo(n.key);
            if (c == 0)
                return n.value;
            n = c < 0 ? n.left : n.right;
        }
        return null;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public void clear() {
        root = null;
        size = 0;
    }

    @SuppressWarnings("unchecked")
    public Entry<K, V>[] inOrderEntries() {
        Entry<K, V>[] a = (Entry<K, V>[]) new Entry[size];
        fill(root, a, new int[] { 0 });
        return a;
    }

    private void fill(Node n, Entry<K, V>[] a, int[] i) {
        if (n == null)
            return;
        fill(n.left, a, i);
        a[i[0]++] = new Entry<K, V>(n.key, n.value);
        fill(n.right, a, i);
    }
}
