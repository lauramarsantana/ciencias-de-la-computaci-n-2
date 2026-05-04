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
        // CAMBIO AQUÍ: LinkedHashMap en lugar de HashMap
        this.vertices = new LinkedHashMap<>();
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

    public void setNombre(String nombre) {
        this.nombre = nombre;
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

        // 1. Copiamos todos los vértices
        for (Vertice v : g1.getVertices().values()) {
            g3.agregarVertice(new Vertice(v.getName(), v.getPositionX(), v.getPositionY()));
        }

        List<Vertice> listaV = new ArrayList<>(g3.getVertices().values());

        // 2. Generamos todos los pares posibles (i, j) una sola vez
        for (int i = 0; i < listaV.size(); i++) {
            for (int j = i + 1; j < listaV.size(); j++) {
                Vertice vA = listaV.get(i);
                Vertice vB = listaV.get(j);

                // Creamos una arista temporal para comparar
                Arista posible = new Arista("temp", vA, vB);

                // Revisamos si esa arista existe en el grafo original
                boolean existeEnOriginal = false;
                for (Arista aOrig : g1.getAristas()) {
                    if (aOrig.equals(posible)) { // Aquí entra nuestro nuevo equals flexible
                        existeEnOriginal = true;
                        break;
                    }
                }

                // Si NO existe en el original, es parte del complemento
                if (!existeEnOriginal) {
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

    public static Grafo fusionarVertices(Grafo g, String nombreV1, String nombreV2) {
        if (!g.getVertices().containsKey(nombreV1) || !g.getVertices().containsKey(nombreV2)) {
            return g;
        }

        Grafo res = new Grafo("Fusión de " + nombreV1 + " y " + nombreV2);
        String nuevoNombre = nombreV1 + "_" + nombreV2;

        // 1. Copiar vértices
        for (Vertice v : g.getVertices().values()) {
            if (!v.getName().equals(nombreV1) && !v.getName().equals(nombreV2)) {
                res.agregarVertice(new Vertice(v.getName(), 0, 0));
            }
        }
        res.agregarVertice(new Vertice(nuevoNombre, 0, 0));

        // 2. RECONECTAR ARISTAS CON IDENTIFICADOR ÚNICO
        int contador = 0;
        for (Arista a : g.getAristas()) {
            String or = a.getVerticeOrigen().getName();
            String des = a.getVerticeDestino().getName();

            if (or.equals(nombreV1) || or.equals(nombreV2)) or = nuevoNombre;
            if (des.equals(nombreV1) || des.equals(nombreV2)) des = nuevoNombre;

            Vertice vOr = res.getVertices().get(or);
            Vertice vDes = res.getVertices().get(des);

            // El secreto: Le ponemos un ID único al nombre para que no se pisen
            String idUnico = or + "-" + des + "-id" + (contador++);
            res.agregarArista(new Arista(idUnico, vOr, vDes));
        }
        return res;
    }
    public void añadirVertice(String nombre) {
        if (!vertices.containsKey(nombre)) {
            // Lo creamos en la posición (0,0); reacomodarCircular lo pondrá en su lugar luego
            vertices.put(nombre, new Vertice(nombre, 0, 0));
        }
    }

    public static Grafo copiar(Grafo original) {
        Grafo copia = new Grafo(original.getNombre() + " (Modificado)");

        // Copiar vértices
        for (Vertice v : original.getVertices().values()) {
            copia.agregarVertice(new Vertice(v.getName(), v.getPositionX(), v.getPositionY()));
        }

        // Copiar aristas re-vinculando a los nuevos vértices
        for (Arista a : original.getAristas()) {
            Vertice o = copia.getVertices().get(a.getVerticeOrigen().getName());
            Vertice d = copia.getVertices().get(a.getVerticeDestino().getName());
            copia.agregarArista(new Arista(a.getName(), o, d));
        }

        return copia;
    }
    public void eliminarVertice(String nombre) {
        if (vertices.containsKey(nombre)) {
            // 1. Eliminar el vértice del mapa
            vertices.remove(nombre);

            // 2. Eliminar todas las aristas que incidían en él
            // Usamos removeIf que es muy eficiente en Java
            aristas.removeIf(a -> a.getVerticeOrigen().getName().equals(nombre) ||
                    a.getVerticeDestino().getName().equals(nombre));
        }
    }

    public static Grafo contraerArista(Grafo g, String nombreBusqueda) {
        // 1. Buscar la arista ignorando el orden (1-3 es igual a 3-1)
        Arista objetivo = null;
        for (Arista a : g.getAristas()) {
            String n1 = a.getVerticeOrigen().getName();
            String n2 = a.getVerticeDestino().getName();
            String inversa = n2 + "-" + n1;
            String normal = n1 + "-" + n2;

            if (normal.equals(nombreBusqueda) || inversa.equals(nombreBusqueda)) {
                objetivo = a;
                break;
            }
        }

        if (objetivo == null) return null;

        // 2. Identificar extremos
        String v1 = objetivo.getVerticeOrigen().getName();
        String v2 = objetivo.getVerticeDestino().getName();

        // 3. Usamos la fusión para mantener las otras conexiones
        Grafo res = fusionarVertices(g, v1, v2);
        String nuevoNodo = v1 + "_" + v2;

        // 4. EL TRUCO: Eliminar los bucles que se crearon en el nuevo nodo
        // Esto quita la arista contraída que "sobra"
        res.getAristas().removeIf(a ->
                a.getVerticeOrigen().getName().equals(nuevoNodo) &&
                        a.getVerticeDestino().getName().equals(nuevoNodo)
        );

        return res;
    }
    public static Grafo adicionarArista(Grafo g, String v1Nombre, String v2Nombre) {
        // 1. Creamos la copia para el Panel 3
        Grafo res = copiar(g);

        // 2. Buscamos los vértices en la copia
        Vertice origen = res.getVertices().get(v1Nombre);
        Vertice destino = res.getVertices().get(v2Nombre);

        if (origen != null && destino != null) {
            // 3. Creamos la arista (ej: "1-3")
            String nombreArista = v1Nombre + "-" + v2Nombre;
            res.agregarArista(new Arista(nombreArista, origen, destino));
            return res;
        }

        return null; // Si uno de los vértices no existe
    }

    public static Grafo eliminarArista(Grafo g, String nombreBusqueda) {
        // 1. Creamos la copia para el Panel 3
        Grafo res = copiar(g);

        // 2. Buscamos y eliminamos la arista ignorando el orden
        // Si el usuario escribe "1-2", borramos tanto "1-2" como "2-1"
        res.getAristas().removeIf(a -> {
            String n1 = a.getVerticeOrigen().getName();
            String n2 = a.getVerticeDestino().getName();
            String normal = n1 + "-" + n2;
            String inversa = n2 + "-" + n1;

            return normal.equals(nombreBusqueda) || inversa.equals(nombreBusqueda);
        });

        return res;
    }

    public static Grafo productoCartesiano(Grafo g1, Grafo g2) {
        // Usamos un nombre que identifique la operación
        Grafo res = new Grafo("Producto Cartesiano");

        // Ordenar las listas para asegurar consistencia (a antes que b, c antes que d)
        List<Vertice> listaG1 = new ArrayList<>(g1.getVertices().values());
        listaG1.sort((v1, v2) -> v1.getName().compareTo(v2.getName()));

        List<Vertice> listaG2 = new ArrayList<>(g2.getVertices().values());
        listaG2.sort((v1, v2) -> v1.getName().compareTo(v2.getName()));

        // CREACIÓN DE VÉRTICES: G1 controla las filas, G2 las columnas
        for (Vertice u : listaG1) {
            for (Vertice v : listaG2) {
                String nombre = u.getName() + v.getName();
                res.agregarVertice(new Vertice(nombre, 0, 0));
            }
        }

        // 2. Crear aristas: Solo una dirección para evitar duplicados visuales
        for (Vertice u1 : g1.getVertices().values()) {
            for (Vertice v1 : g2.getVertices().values()) {
                for (Vertice u2 : g1.getVertices().values()) {
                    for (Vertice v2 : g2.getVertices().values()) {

                        String n1 = u1.getName() + v1.getName();
                        String n2 = u2.getName() + v2.getName();

                        // Evitar duplicados y el mismo nodo
                        if (n1.compareTo(n2) >= 0) continue;

                        boolean conectar = false;
                        // Misma u, arista en G2
                        if (u1.getName().equals(u2.getName()) && g2.existeArista(v1.getName(), v2.getName())) {
                            conectar = true;
                        }
                        // Misma v, arista en G1
                        else if (v1.getName().equals(v2.getName()) && g1.existeArista(u1.getName(), u2.getName())) {
                            conectar = true;
                        }

                        if (conectar) {
                            res.agregarArista(new Arista(n1 + "-" + n2, res.getVertices().get(n1), res.getVertices().get(n2)));
                        }
                    }
                }
            }
        }
        return res;
    }

    // Asegúrate de tener este método en Grafo.java para las validaciones
    public boolean existeArista(String v1, String v2) {
        for (Arista a : aristas) {
            String n1 = a.getVerticeOrigen().getName();
            String n2 = a.getVerticeDestino().getName();
            if ((n1.equals(v1) && n2.equals(v2)) || (n1.equals(v2) && n2.equals(v1))) return true;
        }
        return false;
    }

    public static Grafo productoTensorial(Grafo g1, Grafo g2) {
        Grafo res = new Grafo("Producto Tensorial");

        // 1. Listas ordenadas para la rejilla (Igual que el Cartesiano)
        List<Vertice> listaG1 = new ArrayList<>(g1.getVertices().values());
        listaG1.sort((v1, v2) -> v1.getName().compareTo(v2.getName()));

        List<Vertice> listaG2 = new ArrayList<>(g2.getVertices().values());
        listaG2.sort((v1, v2) -> v1.getName().compareTo(v2.getName()));

        // 2. Crear vértices en orden de LinkedHashMap
        for (Vertice u : listaG1) {
            for (Vertice v : listaG2) {
                res.agregarVertice(new Vertice(u.getName() + v.getName(), 0, 0));
            }
        }

        // 3. Lógica de Aristas TENSORIAL: (u,v) ~ (u',v') si u ~ u' Y v ~ v'
        for (Arista a1 : g1.getAristas()) {
            for (Arista a2 : g2.getAristas()) {
                // Combinación 1: (u -> u') y (v -> v')
                String n1 = a1.getVerticeOrigen().getName() + a2.getVerticeOrigen().getName();
                String n2 = a1.getVerticeDestino().getName() + a2.getVerticeDestino().getName();

                // Combinación 2: (u -> u') y (v' -> v) - Las "diagonales"
                String n3 = a1.getVerticeOrigen().getName() + a2.getVerticeDestino().getName();
                String n4 = a1.getVerticeDestino().getName() + a2.getVerticeOrigen().getName();

                conectarSiExisten(res, n1, n2);
                conectarSiExisten(res, n3, n4);
            }
        }
        return res;
    }
    // Método auxiliar para evitar código repetido y aristas duplicadas
    private static void conectarSiExisten(Grafo g, String nombreA, String nombreB) {
        Vertice vA = g.getVertices().get(nombreA);
        Vertice vB = g.getVertices().get(nombreB);
        if (vA != null && vB != null) {
            Arista nueva = new Arista(nombreA + "-" + nombreB, vA, vB);
            if (!g.getAristas().contains(nueva)) {
                g.agregarArista(nueva);
            }
        }
    }

    public static Grafo composicion(Grafo g1, Grafo g2) {
        Grafo res = new Grafo("Composición");

        List<Vertice> listaG1 = new ArrayList<>(g1.getVertices().values());
        listaG1.sort((v1, v2) -> v1.getName().compareTo(v2.getName()));

        List<Vertice> listaG2 = new ArrayList<>(g2.getVertices().values());
        listaG2.sort((v1, v2) -> v1.getName().compareTo(v2.getName()));

        // 1. Crear los vértices en la rejilla
        for (Vertice u : listaG1) {
            for (Vertice v : listaG2) {
                res.agregarVertice(new Vertice(u.getName() + v.getName(), 0, 0));
            }
        }

        // 2. Aplicar las condiciones de tus apuntes
        for (Vertice u : listaG1) {
            for (Vertice uP : listaG1) {
                for (Vertice v : listaG2) {
                    for (Vertice vP : listaG2) {

                        String id1 = u.getName() + v.getName();
                        String id2 = uP.getName() + vP.getName();

                        if (id1.equals(id2)) continue;

                        // Condición 1: u1 R v1 en G1 (u conecta con uP)
                        boolean r1 = g1.existeArista(u.getName(), uP.getName());

                        // Condición 2: u1 = v1 Y u2 R v2 en G2
                        boolean r2 = u.getName().equals(uP.getName()) &&
                                g2.existeArista(v.getName(), vP.getName());

                        if (r1 || r2) {
                            conectarSiExisten(res, id1, id2);
                        }
                    }
                }
            }
        }
        return res;
    }

    public Map<String, Integer> colorearGreedy() {
        Map<String, Integer> resultado = new HashMap<>();
        List<String> nodos = new ArrayList<>(this.vertices.keySet());

        for (String nodo : nodos) {
            Set<Integer> coloresUsados = new HashSet<>();
            // Revisar colores de vecinos
            for (Arista a : this.aristas) {
                if (a.getVerticeOrigen().getName().equals(nodo)) {
                    if (resultado.containsKey(a.getVerticeDestino().getName()))
                        coloresUsados.add(resultado.get(a.getVerticeDestino().getName()));
                } else if (a.getVerticeDestino().getName().equals(nodo)) {
                    if (resultado.containsKey(a.getVerticeOrigen().getName()))
                        coloresUsados.add(resultado.get(a.getVerticeOrigen().getName()));
                }
            }
            // Asignar el primer color disponible
            int color = 0;
            while (coloresUsados.contains(color)) color++;
            resultado.put(nodo, color);
        }
        return resultado;
    }

    public Map<Arista, Integer> colorearAristasGreedy() {
        Map<Arista, Integer> resultado = new HashMap<>();

        for (Arista arista : this.aristas) {
            Set<Integer> coloresProhibidos = new HashSet<>();

            // Revisar colores de aristas adyacentes (que comparten un vértice)
            for (Arista otra : this.aristas) {
                if (otra != arista && resultado.containsKey(otra)) {
                    if (otra.comparteVertice(arista)) {
                        coloresProhibidos.add(resultado.get(otra));
                    }
                }
            }

            int color = 0;
            while (coloresProhibidos.contains(color)) color++;
            resultado.put(arista, color);
        }
        return resultado;
    }

    public List<Set<String>> obtenerConjuntosIndependientes() {
        Map<String, Integer> coloreado = this.colorearGreedy();
        Map<Integer, Set<String>> grupos = new HashMap<>();

        coloreado.forEach((vertice, color) -> {
            grupos.computeIfAbsent(color, k -> new HashSet<>()).add(vertice);
        });

        return new ArrayList<>(grupos.values());
    }
}