package utilities;

import java.util.ArrayList;
import java.util.List;

public class GrafoOrdinal {
    private List<VerticeOrdinal> vertices;
    private List<AristaDirigida> aristas;

    public GrafoOrdinal() {
        this.vertices = new ArrayList<>();
        this.aristas = new ArrayList<>();
    }

    public void agregarVertice(String nombre, int x, int y) {
        if (buscarVertice(nombre) != null) {
            throw new IllegalArgumentException("Ese vértice ya existe.");
        }
        vertices.add(new VerticeOrdinal(nombre, x, y));
    }

    public void agregarArista(String origen, String destino) {
        if (origen.equals(destino)) {
            throw new IllegalArgumentException("Un vértice no puede apuntarse a sí mismo.");
        }

        if (buscarVertice(origen) == null || buscarVertice(destino) == null) {
            throw new IllegalArgumentException("Ambos vértices deben existir.");
        }

        for (AristaDirigida a : aristas) {
            if (a.getOrigen().equals(origen) && a.getDestino().equals(destino)) {
                throw new IllegalArgumentException("Esa arista ya existe.");
            }
        }

        aristas.add(new AristaDirigida(origen, destino));
    }

    public VerticeOrdinal buscarVertice(String nombre) {
        for (VerticeOrdinal v : vertices) {
            if (v.getNombre().equals(nombre)) {
                return v;
            }
        }
        return null;
    }

    public List<VerticeOrdinal> getVertices() {
        return vertices;
    }

    public List<AristaDirigida> getAristas() {
        return aristas;
    }

    public int contarVertices() {
        return vertices.size();
    }

    public int contarAristas() {
        return aristas.size();
    }
}
