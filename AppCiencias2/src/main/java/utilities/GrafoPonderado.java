package utilities;

import java.util.ArrayList;
import java.util.List;

public class GrafoPonderado {
    private List<String> vertices;
    private List<AristaPonderada> aristas;

    public GrafoPonderado() {
        vertices = new ArrayList<>();
        aristas = new ArrayList<>();
    }

    public List<String> getVertices() {
        return vertices;
    }

    public List<AristaPonderada> getAristas() {
        return aristas;
    }

    public void agregarVertice(String v) {
        if (!vertices.contains(v)) {
            vertices.add(v);
        }
    }

    public void agregarArista(String origen, String destino, int peso) {
        aristas.add(new AristaPonderada(origen, destino, peso));
    }
}
