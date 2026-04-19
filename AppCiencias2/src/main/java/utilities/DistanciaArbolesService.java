package utilities;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DistanciaArbolesService {

    public static int calcularDistancia(List<AristaPonderada> arbol1, List<AristaPonderada> arbol2) {
        Set<Integer> ids1 = extraerIds(arbol1);
        Set<Integer> ids2 = extraerIds(arbol2);

        Set<Integer> union = new HashSet<>(ids1);
        union.addAll(ids2);

        Set<Integer> interseccion = new HashSet<>(ids1);
        interseccion.retainAll(ids2);

        return union.size() - interseccion.size();
    }

    public static Set<Integer> obtenerComunes(List<AristaPonderada> arbol1, List<AristaPonderada> arbol2) {
        Set<Integer> ids1 = extraerIds(arbol1);
        Set<Integer> ids2 = extraerIds(arbol2);

        ids1.retainAll(ids2);
        return ids1;
    }

    public static Set<Integer> obtenerSoloArbol1(List<AristaPonderada> arbol1, List<AristaPonderada> arbol2) {
        Set<Integer> ids1 = extraerIds(arbol1);
        Set<Integer> ids2 = extraerIds(arbol2);

        ids1.removeAll(ids2);
        return ids1;
    }

    public static Set<Integer> obtenerSoloArbol2(List<AristaPonderada> arbol1, List<AristaPonderada> arbol2) {
        Set<Integer> ids1 = extraerIds(arbol1);
        Set<Integer> ids2 = extraerIds(arbol2);

        ids2.removeAll(ids1);
        return ids2;
    }

    private static Set<Integer> extraerIds(List<AristaPonderada> aristas) {
        Set<Integer> ids = new HashSet<>();
        for (AristaPonderada a : aristas) {
            ids.add(a.getPeso());
        }
        return ids;
    }
}