package utilities;

import java.util.List;

public class MatrizGrafosService {

    public int[][] generarMatrizAdyacencia(
            List<String> vertices,
            List<String[]> aristas,
            boolean dirigido
    ) {

        int n = vertices.size();

        int[][] matriz = new int[n][n];

        for (String[] arista : aristas) {

            String origen = arista[0];
            String destino = arista[1];

            int i = vertices.indexOf(origen);
            int j = vertices.indexOf(destino);

            if (i == -1 || j == -1) {
                continue;
            }

            matriz[i][j] = 1;

            if (!dirigido) {
                matriz[j][i] = 1;
            }
        }

        return matriz;
    }

    public int[][] generarMatrizIncidencia(
            List<String> vertices,
            List<String[]> aristas,
            boolean dirigido
    ) {

        int filas = vertices.size();
        int columnas = aristas.size();

        int[][] matriz = new int[filas][columnas];

        for (int j = 0; j < aristas.size(); j++) {

            String origen = aristas.get(j)[0];
            String destino = aristas.get(j)[1];

            int filaOrigen = vertices.indexOf(origen);
            int filaDestino = vertices.indexOf(destino);

            if (filaOrigen == -1 || filaDestino == -1) {
                continue;
            }

            if (dirigido) {

                matriz[filaOrigen][j] = 1;
                matriz[filaDestino][j] = -1;

            } else {

                matriz[filaOrigen][j] = 1;
                matriz[filaDestino][j] = 1;
            }
        }

        return matriz;
    }

    public int[][] generarMatrizAdyacenciaAristas(
            List<String[]> aristas
    ) {

        int n = aristas.size();

        int[][] matriz = new int[n][n];

        for (int i = 0; i < n; i++) {

            String[] a1 = aristas.get(i);

            for (int j = 0; j < n; j++) {

                if (i == j) {
                    continue;
                }

                String[] a2 = aristas.get(j);

                boolean compartenVertice =
                        a1[0].equals(a2[0]) ||
                        a1[0].equals(a2[1]) ||
                        a1[1].equals(a2[0]) ||
                        a1[1].equals(a2[1]);

                if (compartenVertice) {
                    matriz[i][j] = 1;
                }
            }
        }

        return matriz;
    }
}