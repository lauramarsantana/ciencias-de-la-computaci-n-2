package utilities;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import utilities.Vertice;
import utilities.Arista;

public class Grafo {
    private String nombre;
    private Map<String, Vertice> vertices; // Usamos la interfaz Map
    private Set<Arista> aristas;           // Usamos la interfaz Set

    public Grafo(String nombre) {
        this.nombre = nombre;
        this.vertices = new HashMap<>();
        this.aristas = new HashSet<>();
    }

    // --- MÉTODOS DE ACCESO ---
    public String getNombre() {
        return nombre;
    }
    public Map<String, Vertice> getVertices() {
        return vertices;
    }
    public Set<Arista> getAristas() {
        return aristas;
    }

    // --- LÓGICA BÁSICA ---

    public void agregarVertice(Vertice v) {
        // El HashMap asegura que si entra un nombre repetido, no se duplique
        vertices.put(v.getName(), v);
    }

    public void agregarArista(Arista a) {
        // El HashSet usa el equals que hicimos en Arista para no repetir A-B y B-A
        aristas.add(a);
    }

    public static Grafo union(Grafo g1, Grafo g2) {
        Grafo g3 = new Grafo("G1 U G2");

        // 1. Pasamos vértices de G1 (creando copias para que G3 tenga sus propios objetos)
        for (Vertice v : g1.getVertices().values()) {
            g3.agregarVertice(new Vertice(v.getName(), v.getPositionX(), v.getPositionY()));
        }

        // 2. Pasamos vértices de G2
        for (Vertice v : g2.getVertices().values()) {
            g3.agregarVertice(new Vertice(v.getName(), v.getPositionX(), v.getPositionY()));
        }

        // 3. Metemos las ARISTAS de g1 RE-VINCULADAS
        for (Arista a : g1.getAristas()) {
            Vertice origenG3 = g3.getVertices().get(a.getVerticeOrigen().getName());
            Vertice destinoG3 = g3.getVertices().get(a.getVerticeDestino().getName());
            g3.agregarArista(new Arista(a.getName(), origenG3, destinoG3));
        }

        // 4. Metemos las ARISTAS de g2 RE-VINCULADAS
        for (Arista a : g2.getAristas()) {
            Vertice origenG3 = g3.getVertices().get(a.getVerticeOrigen().getName());
            Vertice destinoG3 = g3.getVertices().get(a.getVerticeDestino().getName());

            // Evitamos duplicar la arista si ya fue añadida por G1
            Arista nuevaArista = new Arista(a.getName(), origenG3, destinoG3);
            if (!g3.getAristas().contains(nuevaArista)) {
                g3.agregarArista(nuevaArista);
            }
        }

        return g3;
    }
    public static Grafo interseccion(Grafo g1, Grafo g2) {
        Grafo g3 = new Grafo("G1 ∩ G2");

        // 1. Solo agregamos vértices que existan en ambos
        for (Vertice v1 : g1.getVertices().values()) {
            if (g2.getVertices().containsKey(v1.getName())) {
                // Creamos una copia para G3
                g3.agregarVertice(new Vertice(v1.getName(), v1.getPositionX(), v1.getPositionY()));
            }
        }

        // 2. Solo agregamos aristas que existan en ambos
        // Nota: El nombre de la arista debe ser igual (ej: "A-B")
        for (Arista a1 : g1.getAristas()) {
            for (Arista a2 : g2.getAristas()) {
                if (a1.getName().equals(a2.getName())) {
                    // Verificamos que los vértices de esta arista existan en nuestro nuevo G3
                    Vertice origenG3 = g3.getVertices().get(a1.getVerticeOrigen().getName());
                    Vertice destinoG3 = g3.getVertices().get(a1.getVerticeDestino().getName());

                    if (origenG3 != null && destinoG3 != null) {
                        g3.agregarArista(new Arista(a1.getName(), origenG3, destinoG3));
                    }
                }
            }
        }
        return g3;
    }
}