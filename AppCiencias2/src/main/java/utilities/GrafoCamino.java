package utilities;

import java.util.ArrayList;
import java.util.List;

public class GrafoCamino {

    private List<VerticeCamino> vertices;
    private List<AristaCamino> aristas;

    public GrafoCamino() {
        this.vertices = new ArrayList<>();
        this.aristas = new ArrayList<>();
    }

    public void agregarVertice(String nombre, int x, int y) {
        if (buscarVertice(nombre) != null) {
            throw new IllegalArgumentException("Ese vértice ya existe.");
        }

        vertices.add(new VerticeCamino(nombre, x, y));
    }

    public void agregarArista(String origen, String destino, int peso) {
        if (origen.equals(destino)) {
            throw new IllegalArgumentException("Un vértice no puede apuntarse a sí mismo.");
        }

        if (buscarVertice(origen) == null || buscarVertice(destino) == null) {
            throw new IllegalArgumentException("Ambos vértices deben existir.");
        }

        for (AristaCamino a : aristas) {
            if (a.getOrigen().equals(origen) && a.getDestino().equals(destino)) {
                throw new IllegalArgumentException("Esa arista ya existe.");
            }
        }

        aristas.add(new AristaCamino(origen, destino, peso));
    }

    public VerticeCamino buscarVertice(String nombre) {
        for (VerticeCamino v : vertices) {
            if (v.getNombre().equals(nombre)) {
                return v;
            }
        }
        return null;
    }

    public List<VerticeCamino> getVertices() {
        return vertices;
    }

    public List<AristaCamino> getAristas() {
        return aristas;
    }

    public int contarVertices() {
        return vertices.size();
    }

    public int contarAristas() {
        return aristas.size();
    }
}