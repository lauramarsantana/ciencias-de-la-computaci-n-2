package utilities;

import java.util.HashMap;
import java.util.Map;

public class UnionFind {

    private Map<String, String> padre;

    public UnionFind() {
        padre = new HashMap<>();
    }

    public void makeSet(String x) {
        padre.put(x, x);
    }

    public String find(String x) {
        if (!padre.containsKey(x)) {
            throw new IllegalArgumentException("El vértice no existe en UnionFind: " + x);
        }

        if (!padre.get(x).equals(x)) {
            padre.put(x, find(padre.get(x)));
        }

        return padre.get(x);
    }

    public boolean union(String a, String b) {
        String raizA = find(a);
        String raizB = find(b);

        if (raizA.equals(raizB)) {
            return false;
        }

        padre.put(raizA, raizB);
        return true;
    }
}