package utilities;

import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Set;

public class MatrizCircuitosService {

    public List<List<String>> obtenerCircuitos(
            List<String> vertices,
            List<String[]> aristas,
            boolean dirigido) {

        List<List<String>> circuitos = new ArrayList<>();

        for (String inicio : vertices) {
            List<String> camino = new ArrayList<>();
            buscarCircuitos(inicio, inicio, vertices, aristas, dirigido, camino, circuitos);
        }

        return eliminarCircuitosRepetidos(circuitos);
    }
    
    public List<List<String>> obtenerCircuitosFundamentales(
        List<String> vertices,
        List<String[]> aristas) {

    GrafoPonderado grafo = new GrafoPonderado();

    for (String v : vertices) {
        grafo.agregarVertice(v);
    }

    for (String[] a : aristas) {
        grafo.agregarArista(a[0], a[1], 1);
    }

    List<AristaPonderada> arbol =
            ArbolGeneradorService.kruskal(grafo, false);

    Set<String> aristasArbol = new HashSet<>();

    for (AristaPonderada a : arbol) {
        aristasArbol.add(claveArista(a.getOrigen(), a.getDestino()));
    }

    List<List<String>> fundamentales = new ArrayList<>();

    for (String[] a : aristas) {

        String clave = claveArista(a[0], a[1]);

        if (!aristasArbol.contains(clave)) {

            List<String> camino =
                    buscarCaminoEnArbol(
                            a[0],
                            a[1],
                            arbol,
                            new ArrayList<>(),
                            new HashSet<>());

            if (!camino.isEmpty()) {

                camino.add(a[0]);

                fundamentales.add(camino);
            }
        }
    }

    return fundamentales;
}
    private List<String> buscarCaminoEnArbol(
        String actual,
        String destino,
        List<AristaPonderada> arbol,
        List<String> camino,
        Set<String> visitados) {

    camino.add(actual);
    visitados.add(actual);

    if (actual.equals(destino)) {
        return new ArrayList<>(camino);
    }

    for (AristaPonderada a : arbol) {

        String vecino = null;

        if (a.getOrigen().equals(actual)) {
            vecino = a.getDestino();
        } else if (a.getDestino().equals(actual)) {
            vecino = a.getOrigen();
        }

        if (vecino != null && !visitados.contains(vecino)) {

            List<String> resultado =
                    buscarCaminoEnArbol(
                            vecino,
                            destino,
                            arbol,
                            camino,
                            visitados);

            if (!resultado.isEmpty()) {
                return resultado;
            }
        }
    }

    camino.remove(camino.size() - 1);

    return new ArrayList<>();
}
    private String claveArista(String a, String b) {

    if (a.compareTo(b) < 0) {
        return a + "-" + b;
    }

    return b + "-" + a;
}

    private void buscarCircuitos(
            String actual,
            String inicio,
            List<String> vertices,
            List<String[]> aristas,
            boolean dirigido,
            List<String> camino,
            List<List<String>> circuitos) {

        camino.add(actual);

        for (String[] arista : aristas) {
            String origen = arista[0];
            String destino = arista[1];

            if (!origen.equals(actual)) {
                if (!dirigido && destino.equals(actual)) {
                    destino = origen;
                } else {
                    continue;
                }
            }

            if (destino.equals(inicio) && camino.size() > 2) {
                List<String> circuito = new ArrayList<>(camino);
                circuito.add(inicio);
                circuitos.add(circuito);
            } else if (!camino.contains(destino)) {
                buscarCircuitos(destino, inicio, vertices, aristas, dirigido, camino, circuitos);
            }
        }

        camino.remove(camino.size() - 1);
    }

    public int[][] generarMatrizCircuitos(
            List<String[]> aristas,
            List<List<String>> circuitos,
            boolean dirigido) {

        int[][] matriz = new int[circuitos.size()][aristas.size()];

        for (int i = 0; i < circuitos.size(); i++) {
            List<String> circuito = circuitos.get(i);

            for (int j = 0; j < aristas.size(); j++) {
                String origenArista = aristas.get(j)[0];
                String destinoArista = aristas.get(j)[1];

                for (int k = 0; k < circuito.size() - 1; k++) {
                    String origenCircuito = circuito.get(k);
                    String destinoCircuito = circuito.get(k + 1);

                    boolean mismaDireccion =
                            origenArista.equals(origenCircuito)
                                    && destinoArista.equals(destinoCircuito);

                    boolean direccionContraria =
                            origenArista.equals(destinoCircuito)
                                    && destinoArista.equals(origenCircuito);

                    if (mismaDireccion) {
                        matriz[i][j] = 1;
                    } else if (!dirigido && direccionContraria) {
                        matriz[i][j] = 1;
                    } else if (dirigido && direccionContraria) {
                        matriz[i][j] = -1;
                    }
                }
            }
        }

        return matriz;
    }

    public int[][] generarMatrizCircuitosFundamentales(
        List<String[]> aristas,
        List<List<String>> circuitosFundamentales,
        boolean dirigido) {

    return generarMatrizCircuitos(
            aristas,
            circuitosFundamentales,
            dirigido
    );
}

    private List<List<String>> eliminarCircuitosRepetidos(List<List<String>> circuitos) {
        List<List<String>> resultado = new ArrayList<>();
        List<String> claves = new ArrayList<>();

        for (List<String> circuito : circuitos) {
            String clave = generarClaveCircuito(circuito);

            if (!claves.contains(clave)) {
                claves.add(clave);
                resultado.add(circuito);
            }
        }

        return resultado;
    }

    private String generarClaveCircuito(List<String> circuito) {
        List<String> copia = new ArrayList<>(circuito);

        if (copia.size() > 1 && copia.get(0).equals(copia.get(copia.size() - 1))) {
            copia.remove(copia.size() - 1);
        }

        copia.sort(String::compareTo);

        return String.join("-", copia);
    }
}