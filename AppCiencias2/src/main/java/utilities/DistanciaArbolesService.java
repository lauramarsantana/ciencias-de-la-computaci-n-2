package utilities;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DistanciaArbolesService {

    public static double calcularDistancia(List<AristaPonderada> arbol1,
                                           List<AristaPonderada> arbol2) {

        Set<String> aristas1 = extraerAristas(arbol1);
        Set<String> aristas2 = extraerAristas(arbol2);

        double sumaNoComunes = 0;

        for (AristaPonderada arista : arbol1) {
            if (!aristas2.contains(representarArista(arista))) {
                sumaNoComunes += arista.getPeso();
            }
        }

        for (AristaPonderada arista : arbol2) {
            if (!aristas1.contains(representarArista(arista))) {
                sumaNoComunes += arista.getPeso();
            }
        }

        return sumaNoComunes / 2.0;
    }

    public static int calcularRango(List<String> vertices) {
        return vertices.size() - 1;
    }

    public static int calcularNulidad(List<String> vertices,
                                      List<AristaPonderada> aristas) {
        return aristas.size() - vertices.size() + 1;
    }

    public static Set<String> obtenerComunes(List<AristaPonderada> arbol1,
                                             List<AristaPonderada> arbol2) {

        Set<String> aristas1 = extraerAristas(arbol1);
        Set<String> aristas2 = extraerAristas(arbol2);

        aristas1.retainAll(aristas2);
        return aristas1;
    }

    public static Set<String> obtenerSoloArbol1(List<AristaPonderada> arbol1,
                                                List<AristaPonderada> arbol2) {

        Set<String> aristas1 = extraerAristas(arbol1);
        Set<String> aristas2 = extraerAristas(arbol2);

        aristas1.removeAll(aristas2);
        return aristas1;
    }

    public static Set<String> obtenerSoloArbol2(List<AristaPonderada> arbol1,
                                                List<AristaPonderada> arbol2) {

        Set<String> aristas1 = extraerAristas(arbol1);
        Set<String> aristas2 = extraerAristas(arbol2);

        aristas2.removeAll(aristas1);
        return aristas2;
    }

    public static Set<String> obtenerNoComunes(List<AristaPonderada> arbol1,
                                               List<AristaPonderada> arbol2) {

        Set<String> noComunes = obtenerSoloArbol1(arbol1, arbol2);
        noComunes.addAll(obtenerSoloArbol2(arbol1, arbol2));

        return noComunes;
    }

    private static Set<String> extraerAristas(List<AristaPonderada> aristas) {
        Set<String> resultado = new HashSet<>();

        for (AristaPonderada arista : aristas) {
            resultado.add(representarArista(arista));
        }

        return resultado;
    }

    private static String representarArista(AristaPonderada arista) {
        String origen = arista.getOrigen();
        String destino = arista.getDestino();

        // Para que a-b-2 sea igual que b-a-2
        if (origen.compareTo(destino) > 0) {
            String temp = origen;
            origen = destino;
            destino = temp;
        }

        return origen + "-" + destino + "-" + arista.getPeso();
    }
}