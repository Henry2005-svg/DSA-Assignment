package tarumtresorts.adt;

/** Author: <Your Name>. Separate chaining: average O(1), worst O(n). */
public class HashTable<K, V> {
    public static final class Entry<K, V> {
        public final K key;
        public V value;
        Entry<K, V> next;

        Entry(K k, V v, Entry<K, V> n) {
            key = k;
            value = v;
            next = n;
        }
    }

    private Entry<K, V>[] buckets;
    private int size;

    @SuppressWarnings("unchecked")
    public HashTable() {
        buckets = (Entry<K, V>[]) new Entry[31];
    }

    private int index(K key) {
        if (key == null)
            throw new IllegalArgumentException("Null key");
        return (key.hashCode() & 0x7fffffff) % buckets.length;
    }

    public V put(K key, V value) {
        int i = index(key);
        for (Entry<K, V> e = buckets[i]; e != null; e = e.next)
            if (e.key.equals(key)) {
                V old = e.value;
                e.value = value;
                return old;
            }
        buckets[i] = new Entry<K, V>(key, value, buckets[i]);
        size++;
        return null;
    }

    public V get(K key) {
        for (Entry<K, V> e = buckets[index(key)]; e != null; e = e.next)
            if (e.key.equals(key))
                return e.value;
        return null;
    }

    public boolean containsKey(K key) {
        return get(key) != null;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public V remove(K key) {
        int i = index(key);
        Entry<K, V> p = null, e = buckets[i];
        while (e != null) {
            if (e.key.equals(key)) {
                if (p == null)
                    buckets[i] = e.next;
                else
                    p.next = e.next;
                size--;
                return e.value;
            }
            p = e;
            e = e.next;
        }
        return null;
    }

    public void clear() {
        for (int i = 0; i < buckets.length; i++)
            buckets[i] = null;
        size = 0;
    }

    @SuppressWarnings("unchecked")
    public Entry<K, V>[] entries() {
        Entry<K, V>[] a = (Entry<K, V>[]) new Entry[size];
        int n = 0;
        for (int i = 0; i < buckets.length; i++)
            for (Entry<K, V> e = buckets[i]; e != null; e = e.next)
                a[n++] = e;
        return a;
    }
}
