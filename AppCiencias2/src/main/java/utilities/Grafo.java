package utilities;

import java.util.*;

import utilities.Vertice;
import utilities.Arista;

public class Grafo {
    private String nombre;
    private Map<String, Vertice> vertices; // Usamos la interfaz Map
    private List<Arista> aristas;

    public Grafo(String nombre) {
        this.nombre = nombre;
        this.vertices = new HashMap<>();
        this.aristas = new ArrayList<>();
    }

    // --- MÉTODOS DE ACCESO ---
    public String getNombre() {
        return nombre;
    }
    public Map<String, Vertice> getVertices() {
        return vertices;
    }

    public List<Arista> getAristas() {
        return aristas;
    }
    // --- LÓGICA BÁSICA ---

    public void agregarVertice(Vertice v) {
        // El HashMap asegura que si entra un nombre repetido, no se duplique
        vertices.put(v.getName(), v);
    }

    public void agregarArista(Arista a) {
        // Para que la SUMA funcione y se curve, necesitamos permitir
        // que entren todas las aristas que enviamos.
        this.aristas.add(a);
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
                if (a1.equals(a2)) {
                    // Verificamos que los vértices de esta arista existan en nuestro nuevo G3
                    Vertice origenG3 = g3.getVertices().get(a1.getVerticeOrigen().getName());
                    Vertice destinoG3 = g3.getVertices().get(a1.getVerticeDestino().getName());

                    if (origenG3 != null && destinoG3 != null) {
                        g3.agregarArista(new Arista(origenG3.getName() + "-" + destinoG3.getName(), origenG3, destinoG3));
                    }
                }
            }
        }
        return g3;
    }
    public static Grafo sumaAnular(Grafo g1, Grafo g2) {
        Grafo g3 = new Grafo("G1 ⊕ G2");

        // 1. Vértices: Unión de ambos (S1 U S2)
        for (Vertice v : g1.getVertices().values()) {
            g3.agregarVertice(new Vertice(v.getName(), v.getPositionX(), v.getPositionY()));
        }
        for (Vertice v : g2.getVertices().values()) {
            g3.agregarVertice(new Vertice(v.getName(), v.getPositionX(), v.getPositionY()));
        }

        // 2. Aristas de G1 que NO están en G2
        for (Arista a1 : g1.getAristas()) {
            boolean existeEnG2 = false;
            for (Arista a2 : g2.getAristas()) {
                if (a1.equals(a2)) {
                    existeEnG2 = true;
                    break;
                }
            }
            if (!existeEnG2) {
                Vertice o = g3.getVertices().get(a1.getVerticeOrigen().getName());
                Vertice d = g3.getVertices().get(a1.getVerticeDestino().getName());
                g3.agregarArista(new Arista(a1.getName(), o, d));
            }
        }

        // 3. Aristas de G2 que NO están en G1
        for (Arista a2 : g2.getAristas()) {
            boolean existeEnG1 = false;
            for (Arista a1 : g1.getAristas()) {
                if (a2.equals(a1)) {
                    existeEnG1 = true;
                    break;
                }
            }
            if (!existeEnG1) {
                Vertice o = g3.getVertices().get(a2.getVerticeOrigen().getName());
                Vertice d = g3.getVertices().get(a2.getVerticeDestino().getName());
                g3.agregarArista(new Arista(a2.getName(), o, d));
            }
        }

        return g3;
    }

    public static Grafo complemento(Grafo g1) {
        Grafo g3 = new Grafo("Complemento de " + g1.getNombre());

        // 1. Copiamos todos los vértices a G3
        for (Vertice v : g1.getVertices().values()) {
            g3.agregarVertice(new Vertice(v.getName(), v.getPositionX(), v.getPositionY()));
        }

        // 2. Obtenemos una lista de los vértices para iterar por índices
        java.util.List<Vertice> listaV = new java.util.ArrayList<>(g3.getVertices().values());

        // 3. Comparamos cada vértice con todos los demás (sin repetir pares)
        for (int i = 0; i < listaV.size(); i++) {
            for (int j = i + 1; j < listaV.size(); j++) {
                Vertice vA = listaV.get(i);
                Vertice vB = listaV.get(j);

                // Creamos una arista temporal para ver si existe en el grafo original
                // Usamos los vértices originales de g1 para la comprobación
                Vertice vA_orig = g1.getVertices().get(vA.getName());
                Vertice vB_orig = g1.getVertices().get(vB.getName());
                Arista posible = new Arista("temp", vA_orig, vB_orig);

                // SI NO EXISTE en el original, la agregamos al complemento (G3)
                boolean existe = false;
                for (Arista aOrig : g1.getAristas()) {
                    if (aOrig.equals(posible)) {
                        existe = true;
                        break;
                    }
                }

                if (!existe) {
                    g3.agregarArista(new Arista(vA.getName() + "-" + vB.getName(), vA, vB));
                }
            }
        }
        return g3;
    }

    public static Grafo sumaNormal(Grafo g1, Grafo g2) {
        Grafo g3 = new Grafo("G1 + G2");

        // 1. Agregar vértices de G1 (con prefijo para asegurar que sean distintos)
        for (Vertice v : g1.getVertices().values()) {
            g3.agregarVertice(new Vertice("1_" + v.getName(), v.getPositionX() - 50, v.getPositionY()));
        }

        // 2. Agregar vértices de G2
        for (Vertice v : g2.getVertices().values()) {
            g3.agregarVertice(new Vertice("2_" + v.getName(), v.getPositionX() + 50, v.getPositionY()));
        }

        // 3. Agregar aristas originales de G1 (re-vinculadas)
        for (Arista a : g1.getAristas()) {
            Vertice o = g3.getVertices().get("1_" + a.getVerticeOrigen().getName());
            Vertice d = g3.getVertices().get("1_" + a.getVerticeDestino().getName());
            g3.agregarArista(new Arista(o.getName() + "-" + d.getName(), o, d));
        }

        // 4. Agregar aristas originales de G2 (re-vinculadas)
        for (Arista a : g2.getAristas()) {
            Vertice o = g3.getVertices().get("2_" + a.getVerticeOrigen().getName());
            Vertice d = g3.getVertices().get("2_" + a.getVerticeDestino().getName());
            g3.agregarArista(new Arista(o.getName() + "-" + d.getName(), o, d));
        }

        // 5. EL PASO CLAVE: Conectar TODOS los de G1 con TODOS los de G2
        for (Vertice v1 : g1.getVertices().values()) {
            for (Vertice v2 : g2.getVertices().values()) {
                Vertice o = g3.getVertices().get("1_" + v1.getName());
                Vertice d = g3.getVertices().get("2_" + v2.getName());
                // Al agregar esta, el Set de G3 ahora sí la aceptará aunque exista la inversa
                g3.agregarArista(new Arista(o.getName() + "-" + d.getName(), o, d));
            }
        }

        return g3;
    }
}