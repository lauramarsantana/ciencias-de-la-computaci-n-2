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
        if (v == null || v.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del vértice no puede estar vacío.");
        }

        String verticeLimpio = v.trim();

        if (!vertices.contains(verticeLimpio)) {
            vertices.add(verticeLimpio);
        }
    }

    public void agregarArista(String origen, String destino, int peso) {
        if (origen == null || origen.trim().isEmpty() ||
            destino == null || destino.trim().isEmpty()) {
            throw new IllegalArgumentException("El origen y el destino de la arista no pueden estar vacíos.");
        }

        String origenLimpio = origen.trim();
        String destinoLimpio = destino.trim();

        if (!vertices.contains(origenLimpio) || !vertices.contains(destinoLimpio)) {
            throw new IllegalArgumentException(
                "La arista contiene vértices que no existen: " + origenLimpio + " - " + destinoLimpio
            );
        }

        if (origenLimpio.equals(destinoLimpio)) {
            throw new IllegalArgumentException(
                "No se permite una arista del vértice hacia sí mismo: " + origenLimpio
            );
        }

        for (AristaPonderada a : aristas) {
            boolean mismaDireccion =
                a.getOrigen().equals(origenLimpio) && a.getDestino().equals(destinoLimpio);

            boolean direccionContraria =
                a.getOrigen().equals(destinoLimpio) && a.getDestino().equals(origenLimpio);

            if (mismaDireccion || direccionContraria) {
                throw new IllegalArgumentException(
                    "La arista ya existe entre " + origenLimpio + " y " + destinoLimpio
                );
            }
        }

        aristas.add(new AristaPonderada(origenLimpio, destinoLimpio, peso));
    }
}