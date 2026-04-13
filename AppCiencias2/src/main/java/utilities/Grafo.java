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

    /// Operación Union de grafos, Grafo union (de este metodo obtenemos un grafo)
    public static Grafo union(Grafo g1, Grafo g2) {
        // 1. Creamos el nuevo objeto donde irá el resultado
        Grafo g3 = new Grafo("G1 U G2");

        // 2. Metemos todos los VÉRTICES de g1 al resultado
        // .values() saca todos los objetos Vertice del mapa
        for (Vertice v : g1.getVertices().values()) {
            g3.agregarVertice(v);
        }

        // 3. Metemos todos los VÉRTICES de g2
        // Si un vértice ya existe (mismo nombre), el Map NO lo duplica
        for (Vertice v : g2.getVertices().values()) {
            g3.agregarVertice(v);
        }

        // 4. Metemos las ARISTAS de g1
        for (Arista a : g1.getAristas()) {
            g3.agregarArista(a);
        }

        // 5. Metemos las ARISTAS de g2
        for (Arista a : g2.getAristas()) {
            g3.agregarArista(a);
        }

        return g3;
    }
}