package utilities;

import java.util.ArrayList;
import java.util.List;

public class DistanciaArbolesService {

    public static double calcularDistancia(List<AristaPonderada> arbol1, List<AristaPonderada> arbol2) {
        List<Integer> pesos1 = extraerPesos(arbol1);
        List<Integer> pesos2 = extraerPesos(arbol2);

        int sumaUnion = sumar(pesos1) + sumar(pesos2);
        int sumaInterseccion = sumarInterseccionConRepetidos(pesos1, pesos2);

        return (sumaUnion - sumaInterseccion) / 2.0;
    }

    public static List<Integer> obtenerComunes(List<AristaPonderada> arbol1, List<AristaPonderada> arbol2) {
        List<Integer> pesos1 = extraerPesos(arbol1);
        List<Integer> pesos2 = extraerPesos(arbol2);

        List<Integer> comunes = new ArrayList<>();
        List<Integer> copiaPesos2 = new ArrayList<>(pesos2);

        for (Integer peso : pesos1) {
            if (copiaPesos2.contains(peso)) {
                comunes.add(peso);
                copiaPesos2.remove(peso);
            }
        }

        return comunes;
    }

    public static List<Integer> obtenerSoloArbol1(List<AristaPonderada> arbol1, List<AristaPonderada> arbol2) {
        List<Integer> pesos1 = extraerPesos(arbol1);
        List<Integer> pesos2 = extraerPesos(arbol2);

        List<Integer> solo1 = new ArrayList<>();
        List<Integer> copiaPesos2 = new ArrayList<>(pesos2);

        for (Integer peso : pesos1) {
            if (copiaPesos2.contains(peso)) {
                copiaPesos2.remove(peso);
            } else {
                solo1.add(peso);
            }
        }

        return solo1;
    }

    public static List<Integer> obtenerSoloArbol2(List<AristaPonderada> arbol1, List<AristaPonderada> arbol2) {
        List<Integer> pesos1 = extraerPesos(arbol1);
        List<Integer> pesos2 = extraerPesos(arbol2);

        List<Integer> solo2 = new ArrayList<>();
        List<Integer> copiaPesos1 = new ArrayList<>(pesos1);

        for (Integer peso : pesos2) {
            if (copiaPesos1.contains(peso)) {
                copiaPesos1.remove(peso);
            } else {
                solo2.add(peso);
            }
        }

        return solo2;
    }

    public static List<Integer> obtenerNoComunes(List<AristaPonderada> arbol1, List<AristaPonderada> arbol2) {
        List<Integer> noComunes = new ArrayList<>();

        noComunes.addAll(obtenerSoloArbol1(arbol1, arbol2));
        noComunes.addAll(obtenerSoloArbol2(arbol1, arbol2));

        return noComunes;
    }

    private static List<Integer> extraerPesos(List<AristaPonderada> aristas) {
        List<Integer> pesos = new ArrayList<>();

        for (AristaPonderada a : aristas) {
            pesos.add(a.getPeso());
        }

        return pesos;
    }

    private static int sumar(List<Integer> valores) {
        int suma = 0;

        for (Integer valor : valores) {
            suma += valor;
        }

        return suma;
    }

    private static int sumarInterseccionConRepetidos(List<Integer> pesos1, List<Integer> pesos2) {
        int suma = 0;
        List<Integer> copiaPesos2 = new ArrayList<>(pesos2);

        for (Integer peso : pesos1) {
            if (copiaPesos2.contains(peso)) {
                suma += peso;
                copiaPesos2.remove(peso);
            }
        }

        return suma;
    }
}