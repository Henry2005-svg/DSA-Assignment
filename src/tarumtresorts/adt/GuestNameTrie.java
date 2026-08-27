package tarumtresorts.adt;

/**
 * Author: <Your Name>. Prefix location O(p); result collection output-sensitive
 * O(p+k).
 */
public class GuestNameTrie {
    public static final class Match {
        public final String normalizedName, confirmationNumber;

        Match(String n, String c) {
            normalizedName = n;
            confirmationNumber = c;
        }
    }

    private static final class Node {
        Node[] child = new Node[27];
        LinearList<String> confirmations;
    }

    private Node root = new Node();
    private int references;

    private int slot(char c) {
        return c == ' ' ? 26 : c - 'a';
    }

    public boolean insert(
            String name, String confirmation) {
        Node n = root;
        for (int i = 0; i < name.length(); i++) {
            int s = slot(name.charAt(i));
            if (s < 0 || s > 26)
                throw new IllegalArgumentException("Name must contain letters/spaces");
            if (n.child[s] == null)
                n.child[s] = new Node();
            n = n.child[s];
        }
        if (n.confirmations == null)
            n.confirmations = new LinearList<String>();
        n.confirmations.add(confirmation);
        references++;
        return true;
    }

    public Match[] searchPrefix(String prefix) {
        Node n = root;
        for (int i = 0; i < prefix.length(); i++) {
            int s = slot(prefix.charAt(i));
            if (s < 0 || s > 26)
                return new Match[0];
            n = n.child[s];
            if (n == null)
                return new Match[0];
        }
        LinearList<Match> out = new LinearList<Match>();
        collect(n, new StringBuilder(prefix), out);
        Object[] raw = out.toArray();
        Match[] a = new Match[raw.length];
        for (int i = 0; i < a.length; i++)
            a[i] = (Match) raw[i];
        return a;
    }

    private void collect(Node n, StringBuilder word, LinearList<Match> out) {
        if (n.confirmations != null) {
            Object[] refs = n.confirmations.toArray();
            for (int i = 0; i < refs.length; i++)
                out.add(new Match(word.toString(), (String) refs[i]));
        }
        for (int s = 0; s < 27; s++)
            if (n.child[s] != null) {
                word.append(s == 26 ? ' ' : (char) ('a' + s));
                collect(n.child[s], word, out);
                word.setLength(word.length() - 1);
            }
    }

    public int size() {
        return references;
    }

    public void clear() {
        root = new Node();
        references = 0;
    }
}
