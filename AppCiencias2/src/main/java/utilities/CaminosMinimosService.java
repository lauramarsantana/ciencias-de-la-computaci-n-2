package utilities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;

public class CaminosMinimosService {

    private static final int INF = 999999;

    public ResultadoCaminoMinimo ejecutarBellmanOrdinal(List<VerticeCamino> vertices,
                                                        List<AristaCamino> aristas,
                                                        String origen) {

        List<String> pasos = new ArrayList<>();
        Map<String, Integer> distancias = new LinkedHashMap<>();
        Map<String, String> predecesores = new LinkedHashMap<>();

        for (VerticeCamino v : vertices) {
            distancias.put(v.getNombre(), INF);
            predecesores.put(v.getNombre(), "-");
        }

        distancias.put(origen, 0);

        pasos.add("Bellman");
        pasos.add("λ" + limpiarNombre(origen) + " = 0");

        for (VerticeCamino v : vertices) {
            String destinoActual = v.getNombre();

            if (destinoActual.equals(origen)) {
                continue;
            }

            List<AristaCamino> entrantes = obtenerAristasEntrantes(aristas, destinoActual);

            if (entrantes.isEmpty()) {
                pasos.add("λ" + limpiarNombre(destinoActual) + " = ∞ porque no tiene aristas entrantes.");
                continue;
            }

            StringBuilder formula1 = new StringBuilder();
            StringBuilder formula2 = new StringBuilder();
            StringBuilder formula3 = new StringBuilder();

            int menor = INF;
            String mejorPredecesor = "-";

            formula1.append("λ").append(limpiarNombre(destinoActual)).append(" = min{");

            for (int i = 0; i < entrantes.size(); i++) {
                AristaCamino a = entrantes.get(i);

                int distanciaOrigen = distancias.getOrDefault(a.getOrigen(), INF);
                int suma = distanciaOrigen == INF ? INF : distanciaOrigen + a.getPeso();

                if (i > 0) {
                    formula1.append(", ");
                    formula2.append(", ");
                    formula3.append(", ");
                }

                formula1.append("(λ")
                        .append(limpiarNombre(a.getOrigen()))
                        .append(" + (")
                        .append(limpiarNombre(a.getOrigen()))
                        .append(" a ")
                        .append(limpiarNombre(a.getDestino()))
                        .append("))");

                formula2.append("(")
                        .append(formatoDistancia(distanciaOrigen))
                        .append(" + ")
                        .append(a.getPeso())
                        .append(")");

                formula3.append(formatoDistancia(suma));

                if (suma < menor) {
                    menor = suma;
                    mejorPredecesor = a.getOrigen();
                }
            }

            formula1.append("}");

            distancias.put(destinoActual, menor);
            predecesores.put(destinoActual, mejorPredecesor);

            pasos.add(formula1.toString());
            pasos.add("λ" + limpiarNombre(destinoActual) + " = min{" + formula2 + "}");
            pasos.add("λ" + limpiarNombre(destinoActual) + " = min{" + formula3 + "} = " + formatoDistancia(menor));
        }

        return new ResultadoCaminoMinimo(pasos, distancias, predecesores, null, false);
    }

    public ResultadoCaminoMinimo ejecutarDijkstra(List<VerticeCamino> vertices,
                                                  List<AristaCamino> aristas,
                                                  String origen) {

        List<String> pasos = new ArrayList<>();
        Map<String, Integer> distancias = new LinkedHashMap<>();
        Map<String, String> predecesores = new LinkedHashMap<>();
        Map<String, Boolean> permanentes = new HashMap<>();

        for (VerticeCamino v : vertices) {
            distancias.put(v.getNombre(), INF);
            predecesores.put(v.getNombre(), "-");
            permanentes.put(v.getNombre(), false);
        }

        distancias.put(origen, 0);
        permanentes.put(origen, true);

        pasos.add("Dijkstra");
        pasos.add("Nodo " + limpiarNombre(origen) + ": [0, -]* permanente desde el inicio");
        pasos.add("Todos los demás: [∞, -] temporales");

        String actual = origen;
        int paso = 1;

        while (!todosPermanentes(permanentes)) {
            pasos.add("");
            pasos.add("Paso " + paso + " desde nodo " + limpiarNombre(actual) + " permanente");

            boolean revisoAristas = false;

            for (AristaCamino a : aristas) {
                if (!a.getOrigen().equals(actual)) {
                    continue;
                }

                revisoAristas = true;

                String destino = a.getDestino();
                int distanciaActual = distancias.get(actual);
                int nuevaDistancia = distanciaActual + a.getPeso();

                if (permanentes.get(destino)) {
                    pasos.add("(" + limpiarNombre(actual) + " a " + limpiarNombre(destino) + "): ["
                            + distanciaActual + " + " + a.getPeso() + ", " + limpiarNombre(actual)
                            + "] = [" + nuevaDistancia + ", " + limpiarNombre(actual)
                            + "] → nodo " + limpiarNombre(destino)
                            + " ya es permanente [" + formatoDistancia(distancias.get(destino))
                            + "," + limpiarNombre(predecesores.get(destino)) + "]* → no cambia");
                    continue;
                }

                if (nuevaDistancia < distancias.get(destino)) {
                    String anterior = formatoEtiqueta(distancias.get(destino), predecesores.get(destino));

                    distancias.put(destino, nuevaDistancia);
                    predecesores.put(destino, actual);

                    if (anterior.contains("∞")) {
                        pasos.add("(" + limpiarNombre(actual) + " a " + limpiarNombre(destino) + "): ["
                                + distanciaActual + " + " + a.getPeso() + ", " + limpiarNombre(actual)
                                + "] = [" + nuevaDistancia + ", " + limpiarNombre(actual) + "] temporal");
                    } else {
                        pasos.add("(" + limpiarNombre(actual) + " a " + limpiarNombre(destino) + "): ["
                                + distanciaActual + " + " + a.getPeso() + ", " + limpiarNombre(actual)
                                + "] = [" + nuevaDistancia + ", " + limpiarNombre(actual)
                                + "] → mejora " + anterior + " → se actualiza");
                    }
                } else {
                    pasos.add("(" + limpiarNombre(actual) + " a " + limpiarNombre(destino) + "): ["
                            + distanciaActual + " + " + a.getPeso() + ", " + limpiarNombre(actual)
                            + "] = [" + nuevaDistancia + ", " + limpiarNombre(actual)
                            + "] → no mejora " + formatoEtiqueta(distancias.get(destino), predecesores.get(destino)));
                }
            }

            if (!revisoAristas) {
                pasos.add("El nodo " + limpiarNombre(actual) + " no tiene aristas salientes.");
            }

            String activos = etiquetasTemporalesActivas(vertices, distancias, predecesores, permanentes);
            if (!activos.isEmpty()) {
                pasos.add("Etiquetas temporales activas: " + activos);
            }

            String siguiente = obtenerMenorTemporal(vertices, distancias, permanentes);

            if (siguiente == null) {
                pasos.add("No quedan nodos temporales alcanzables.");
                break;
            }

            permanentes.put(siguiente, true);

            pasos.add("Menor temporal: "
                    + formatoEtiqueta(distancias.get(siguiente), predecesores.get(siguiente))
                    + " en nodo " + limpiarNombre(siguiente)
                    + " → pasa a permanente");

            pasos.add("Nodo " + limpiarNombre(siguiente) + ": "
                    + formatoEtiqueta(distancias.get(siguiente), predecesores.get(siguiente)) + "*");

            actual = siguiente;
            paso++;
        }

        pasos.add("");
        pasos.add("Ya todos los nodos alcanzables quedan permanentes. Hasta ahí termina.");

        return new ResultadoCaminoMinimo(pasos, distancias, predecesores, null, false);
    }

    public ResultadoCaminoMinimo ejecutarFloyd(List<VerticeCamino> vertices,
                                               List<AristaCamino> aristas) {

        List<String> pasos = new ArrayList<>();
        Map<String, Integer> distanciasFinales = new LinkedHashMap<>();
        Map<String, String> predecesores = new LinkedHashMap<>();

        int n = vertices.size();
        int[][] matriz = new int[n][n];

        for (int i = 0; i < n; i++) {
            String nombre = vertices.get(i).getNombre();
            predecesores.put(nombre, "-");

            for (int j = 0; j < n; j++) {
                matriz[i][j] = (i == j) ? 0 : INF;
            }
        }

        for (AristaCamino a : aristas) {
            int i = indiceVertice(vertices, a.getOrigen());
            int j = indiceVertice(vertices, a.getDestino());

            if (i != -1 && j != -1) {
                matriz[i][j] = a.getPeso();
            }
        }

        pasos.add("Floyd");
        pasos.add("Matriz inicial:");
        pasos.add(matrizComoTexto(vertices, matriz));

        for (int k = 0; k < n; k++) {
            pasos.add("");
            pasos.add("Ciclo con k = " + limpiarNombre(vertices.get(k).getNombre()));

            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {

                    if (matriz[i][k] == INF || matriz[k][j] == INF) {
                        pasos.add("D" + limpiarNombre(vertices.get(i).getNombre())
                                + limpiarNombre(vertices.get(j).getNombre())
                                + " = min("
                                + formatoDistancia(matriz[i][j])
                                + ", "
                                + formatoDistancia(matriz[i][k])
                                + " + "
                                + formatoDistancia(matriz[k][j])
                                + ") = "
                                + formatoDistancia(matriz[i][j]));
                        continue;
                    }

                    int nueva = matriz[i][k] + matriz[k][j];
                    int anterior = matriz[i][j];

                    if (nueva < anterior) {
                        matriz[i][j] = nueva;
                        pasos.add("D" + limpiarNombre(vertices.get(i).getNombre())
                                + limpiarNombre(vertices.get(j).getNombre())
                                + " = min("
                                + formatoDistancia(anterior)
                                + ", "
                                + matriz[i][k]
                                + " + "
                                + matriz[k][j]
                                + ") = "
                                + nueva
                                + " → se actualiza");
                    }
                }
            }

            pasos.add("Matriz después de k = " + limpiarNombre(vertices.get(k).getNombre()) + ":");
            pasos.add(matrizComoTexto(vertices, matriz));
        }

        pasos.add("");
        pasos.add("Matriz final:");
        pasos.add(matrizComoTexto(vertices, matriz));

        for (int i = 0; i < n; i++) {
            distanciasFinales.put(vertices.get(i).getNombre(), matriz[0][i]);
        }

        return new ResultadoCaminoMinimo(pasos, distanciasFinales, predecesores, matriz, false);
    }

    private List<AristaCamino> obtenerAristasEntrantes(List<AristaCamino> aristas, String destino) {
        List<AristaCamino> entrantes = new ArrayList<>();

        for (AristaCamino a : aristas) {
            if (a.getDestino().equals(destino)) {
                entrantes.add(a);
            }
        }

        return entrantes;
    }

    private boolean todosPermanentes(Map<String, Boolean> permanentes) {
        for (Boolean permanente : permanentes.values()) {
            if (!permanente) {
                return false;
            }
        }
        return true;
    }

    private String obtenerMenorTemporal(List<VerticeCamino> vertices,
                                        Map<String, Integer> distancias,
                                        Map<String, Boolean> permanentes) {

        String menorVertice = null;
        int menorDistancia = INF;

        for (VerticeCamino v : vertices) {
            String nombre = v.getNombre();

            if (!permanentes.get(nombre) && distancias.get(nombre) < menorDistancia) {
                menorDistancia = distancias.get(nombre);
                menorVertice = nombre;
            }
        }

        return menorVertice;
    }

    private String etiquetasTemporalesActivas(List<VerticeCamino> vertices,
                                              Map<String, Integer> distancias,
                                              Map<String, String> predecesores,
                                              Map<String, Boolean> permanentes) {

        StringBuilder sb = new StringBuilder();

        for (VerticeCamino v : vertices) {
            String nombre = v.getNombre();

            if (!permanentes.get(nombre) && distancias.get(nombre) != INF) {
                if (sb.length() > 0) {
                    sb.append(" / ");
                }

                sb.append(formatoEtiqueta(distancias.get(nombre), predecesores.get(nombre)))
                  .append(" en nodo ")
                  .append(limpiarNombre(nombre));
            }
        }

        return sb.toString();
    }

    private String formatoEtiqueta(int distancia, String predecesor) {
        return "[" + formatoDistancia(distancia) + ", " + limpiarNombre(predecesor) + "]";
    }

    private String formatoDistancia(int valor) {
        return valor >= INF ? "∞" : String.valueOf(valor);
    }

    private int indiceVertice(List<VerticeCamino> vertices, String nombre) {
        for (int i = 0; i < vertices.size(); i++) {
            if (vertices.get(i).getNombre().equals(nombre)) {
                return i;
            }
        }
        return -1;
    }

    private String matrizComoTexto(List<VerticeCamino> vertices, int[][] matriz) {
        StringBuilder sb = new StringBuilder();

        sb.append("      ");
        for (VerticeCamino v : vertices) {
            sb.append(String.format("%5s", limpiarNombre(v.getNombre())));
        }
        sb.append("\n");

        for (int i = 0; i < vertices.size(); i++) {
            sb.append(String.format("%5s", limpiarNombre(vertices.get(i).getNombre())));

            for (int j = 0; j < vertices.size(); j++) {
                sb.append(String.format("%5s", formatoDistancia(matriz[i][j])));
            }

            sb.append("\n");
        }

        return sb.toString();
    }

    private String limpiarNombre(String nombre) {
        if (nombre == null || nombre.equals("-")) {
            return "-";
        }

        if (nombre.startsWith("V")) {
            return nombre.substring(1);
        }

        return nombre;
    }
}