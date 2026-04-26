package utilities;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DistanciaArbolesService {

    public static int calcularDistancia(List<AristaPonderada> arbol1, List<AristaPonderada> arbol2) {
        Set<Integer> pesos1 = extraerPesos(arbol1);
        Set<Integer> pesos2 = extraerPesos(arbol2);

        Set<Integer> union = new HashSet<>(pesos1);
        union.addAll(pesos2);

        Set<Integer> interseccion = new HashSet<>(pesos1);
        interseccion.retainAll(pesos2);

        int sumaUnion = sumar(union);
        int sumaInterseccion = sumar(interseccion);

        return (sumaUnion - sumaInterseccion) / 2;
    }

    public static Set<Integer> obtenerComunes(List<AristaPonderada> arbol1, List<AristaPonderada> arbol2) {
        Set<Integer> pesos1 = extraerPesos(arbol1);
        Set<Integer> pesos2 = extraerPesos(arbol2);

        pesos1.retainAll(pesos2);
        return pesos1;
    }

    public static Set<Integer> obtenerSoloArbol1(List<AristaPonderada> arbol1, List<AristaPonderada> arbol2) {
        Set<Integer> pesos1 = extraerPesos(arbol1);
        Set<Integer> pesos2 = extraerPesos(arbol2);

        pesos1.removeAll(pesos2);
        return pesos1;
    }

    public static Set<Integer> obtenerSoloArbol2(List<AristaPonderada> arbol1, List<AristaPonderada> arbol2) {
        Set<Integer> pesos1 = extraerPesos(arbol1);
        Set<Integer> pesos2 = extraerPesos(arbol2);

        pesos2.removeAll(pesos1);
        return pesos2;
    }

    public static Set<Integer> obtenerNoComunes(List<AristaPonderada> arbol1, List<AristaPonderada> arbol2) {
        Set<Integer> solo1 = obtenerSoloArbol1(arbol1, arbol2);
        Set<Integer> solo2 = obtenerSoloArbol2(arbol1, arbol2);

        Set<Integer> noComunes = new HashSet<>(solo1);
        noComunes.addAll(solo2);

        return noComunes;
    }

    private static Set<Integer> extraerPesos(List<AristaPonderada> aristas) {
        Set<Integer> pesos = new HashSet<>();

        for (AristaPonderada a : aristas) {
            pesos.add(a.getPeso());
        }

        return pesos;
    }

    private static int sumar(Set<Integer> valores) {
        int suma = 0;

        for (Integer valor : valores) {
            suma += valor;
        }

        return suma;
    }
}