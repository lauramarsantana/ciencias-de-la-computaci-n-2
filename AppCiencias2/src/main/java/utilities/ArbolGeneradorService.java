package utilities;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ArbolGeneradorService {

    public static List<AristaPonderada> kruskal(GrafoPonderado grafo, boolean maximo) {
        List<AristaPonderada> resultado = new ArrayList<>();
        List<AristaPonderada> aristasOrdenadas = new ArrayList<>(grafo.getAristas());

        if (maximo) {
            aristasOrdenadas.sort(Comparator.comparingInt(AristaPonderada::getPeso).reversed());
        } else {
            aristasOrdenadas.sort(Comparator.comparingInt(AristaPonderada::getPeso));
        }

        UnionFind uf = new UnionFind();
        for (String v : grafo.getVertices()) {
            uf.makeSet(v);
        }

        for (AristaPonderada arista : aristasOrdenadas) {
            if (uf.union(arista.getOrigen(), arista.getDestino())) {
                resultado.add(arista);
            }
        }

        return resultado;
    }

    public static int calcularPesoTotal(List<AristaPonderada> aristas) {
        int suma = 0;
        for (AristaPonderada a : aristas) {
            suma += a.getPeso();
        }
        return suma;
    }
}