package utilities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import utilities.AristaCamino;
import utilities.VerticeCamino;

public class DistanciaVerticesService {

    private static final int INF = 999999;

    public ResultadoDistanciaVertices calcular(
            List<VerticeCamino> vertices,
            List<AristaCamino> aristas) {

        int n = vertices.size();
        int[][] distancias = new int[n][n];

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                distancias[i][j] = (i == j) ? 0 : INF;
            }
        }

        for (AristaCamino arista : aristas) {
            int i = indiceVertice(vertices, arista.getOrigen());
            int j = indiceVertice(vertices, arista.getDestino());

            if (i == -1 || j == -1) {
                continue;
            }

            distancias[i][j] = arista.getPeso();
            distancias[j][i] = arista.getPeso();
        }

        for (int k = 0; k < n; k++) {
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    if (distancias[i][k] != INF
                            && distancias[k][j] != INF
                            && distancias[i][k] + distancias[k][j] < distancias[i][j]) {

                        distancias[i][j] = distancias[i][k] + distancias[k][j];
                    }
                }
            }
        }

        Map<String, Integer> excentricidades = new HashMap<>();

        int radio = INF;
        int diametro = 0;

        for (int i = 0; i < n; i++) {
            int mayor = 0;

            for (int j = 0; j < n; j++) {
                if (distancias[i][j] != INF && distancias[i][j] > mayor) {
                    mayor = distancias[i][j];
                }
            }

            String nombre = vertices.get(i).getNombre();
            excentricidades.put(nombre, mayor);

            if (mayor < radio) {
                radio = mayor;
            }

            if (mayor > diametro) {
                diametro = mayor;
            }
        }

        List<String> centro = new ArrayList<>();

        for (VerticeCamino v : vertices) {
            if (excentricidades.get(v.getNombre()) == radio) {
                centro.add(v.getNombre());
            }
        }

        return new ResultadoDistanciaVertices(
                distancias,
                excentricidades,
                radio,
                diametro,
                centro
        );
    }

    private int indiceVertice(List<VerticeCamino> vertices, String nombre) {
        for (int i = 0; i < vertices.size(); i++) {
            if (vertices.get(i).getNombre().equals(nombre)) {
                return i;
            }
        }
        return -1;
    }

    public String formatearResultado(
            List<VerticeCamino> vertices,
            ResultadoDistanciaVertices resultado) {

        StringBuilder sb = new StringBuilder();

        sb.append("DISTANCIA ENTRE VÉRTICES\n\n");
        sb.append("Matriz de distancias mínimas:\n\n");

        sb.append("      ");
        for (VerticeCamino v : vertices) {
            sb.append(String.format("%6s", v.getNombre()));
        }
        sb.append("\n");

        int[][] matriz = resultado.getMatrizDistancias();

        for (int i = 0; i < matriz.length; i++) {
            sb.append(String.format("%6s", vertices.get(i).getNombre()));

            for (int j = 0; j < matriz[i].length; j++) {
                if (matriz[i][j] >= INF) {
                    sb.append(String.format("%6s", "∞"));
                } else {
                    sb.append(String.format("%6d", matriz[i][j]));
                }
            }

            sb.append("\n");
        }

        sb.append("\nExcentricidad de cada vértice:\n\n");

        for (VerticeCamino v : vertices) {
            sb.append(v.getNombre())
              .append(" = ")
              .append(resultado.getExcentricidades().get(v.getNombre()))
              .append("\n");
        }

        sb.append("\nRadio: ")
          .append(resultado.getRadio())
          .append("\n");

        sb.append("Diámetro: ")
          .append(resultado.getDiametro())
          .append("\n");

        sb.append("Centro: ")
          .append(resultado.getCentro())
          .append("\n");

        return sb.toString();
    }
}
