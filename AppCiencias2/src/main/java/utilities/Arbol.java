package utilities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

public class Arbol {

    private NodoArbol raiz;
    private Map<String, NodoArbol> nodos;

    public Arbol() {
        nodos = new LinkedHashMap<>();
    }

    public NodoArbol getRaiz() {
        return raiz;
    }

    public Map<String, NodoArbol> getNodos() {
        return nodos;
    }

    public NodoArbol obtenerOCrearNodo(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del nodo no puede estar vacío.");
        }

        nombre = nombre.trim();

        if (!nodos.containsKey(nombre)) {
            nodos.put(nombre, new NodoArbol(nombre));
        }

        return nodos.get(nombre);
    }

    public void agregarRaiz(String nombreRaiz) {
        if (nombreRaiz == null || nombreRaiz.trim().isEmpty()) {
            throw new IllegalArgumentException("Debes ingresar un valor para la raíz.");
        }

        if (raiz != null) {
            throw new IllegalArgumentException("La raíz ya fue definida.");
        }

        raiz = obtenerOCrearNodo(nombreRaiz);
    }

    public void agregarRelacion(String padreNombre, String hijoNombre) {
        if (padreNombre == null || padreNombre.trim().isEmpty()
                || hijoNombre == null || hijoNombre.trim().isEmpty()) {
            throw new IllegalArgumentException("Padre e hijo deben tener un valor.");
        }

        padreNombre = padreNombre.trim();
        hijoNombre = hijoNombre.trim();

        if (padreNombre.equals(hijoNombre)) {
            throw new IllegalArgumentException(
                "Un nodo no puede ser padre de sí mismo."
            );
        }

        NodoArbol padre = obtenerOCrearNodo(padreNombre);
        NodoArbol hijo = obtenerOCrearNodo(hijoNombre);

        if (raiz != null && hijo == raiz) {
            throw new IllegalArgumentException(
                "La raíz no puede tener padre."
            );
        }

        if (hijo.getPadre() != null) {
            throw new IllegalArgumentException(
                "El nodo " + hijoNombre + " ya tiene padre."
            );
        }

        if (padre.getHijos().contains(hijo)) {
            throw new IllegalArgumentException(
                "La relación " + padreNombre + " - " + hijoNombre + " ya existe."
            );
        }

        padre.agregarHijo(hijo);
        hijo.setPadre(padre);
    }

    public void prepararArbol() {
        if (nodos.isEmpty()) {
            throw new IllegalArgumentException("Debes crear al menos un nodo.");
        }

        if (raiz == null) {
            determinarRaiz();
        } else {
            validarRaizUnica();
        }

        validarSinCiclosYConexo();
        asignarNiveles();
    }

    public void construirDesdeTexto(String textoRelaciones) {
        nodos.clear();
        raiz = null;

        if (textoRelaciones == null || textoRelaciones.trim().isEmpty()) {
            throw new IllegalArgumentException("Debes ingresar al menos una relación.");
        }

        String[] relaciones = textoRelaciones.split(",");

        for (String relacion : relaciones) {
            String limpia = relacion.trim();

            if (limpia.isEmpty()) {
                continue;
            }

            String[] partes = limpia.split("-");

            if (partes.length != 2) {
                throw new IllegalArgumentException(
                    "Formato inválido en la relación: " + limpia + ". Usa Padre-Hijo."
                );
            }

            String padre = partes[0].trim();
            String hijo = partes[1].trim();

            if (padre.equals(hijo)) {
                throw new IllegalArgumentException(
                    "Un árbol no puede tener bucles: " + limpia
                );
            }

            agregarRelacion(padre, hijo);
        }

        determinarRaiz();
        validarSinCiclosYConexo();
        asignarNiveles();
    }

    private void validarRaizUnica() {
        for (NodoArbol nodo : nodos.values()) {
            if (nodo != raiz && nodo.getPadre() == null) {
                throw new IllegalArgumentException(
                    "Hay más de una raíz. El árbol no es conexo."
                );
            }
        }

        if (raiz.getPadre() != null) {
            throw new IllegalArgumentException("La raíz no puede tener padre.");
        }
    }

    private void determinarRaiz() {
        List<NodoArbol> posiblesRaices = new ArrayList<>();

        for (NodoArbol nodo : nodos.values()) {
            if (nodo.getPadre() == null) {
                posiblesRaices.add(nodo);
            }
        }

        if (posiblesRaices.isEmpty()) {
            throw new IllegalArgumentException("No se encontró raíz. Puede haber un ciclo.");
        }

        if (posiblesRaices.size() > 1) {
            throw new IllegalArgumentException(
                "Hay más de una raíz. El árbol no es conexo."
            );
        }

        raiz = posiblesRaices.get(0);
    }

    private void validarSinCiclosYConexo() {
        if (raiz == null) {
            throw new IllegalArgumentException("No hay raíz definida.");
        }

        List<NodoArbol> visitados = new ArrayList<>();
        recorrerDFS(raiz, visitados);

        if (visitados.size() != nodos.size()) {
            throw new IllegalArgumentException(
                "La estructura no es un árbol válido. Puede no ser conexa o tener ciclo."
            );
        }
    }

    private void recorrerDFS(NodoArbol actual, List<NodoArbol> visitados) {
        if (visitados.contains(actual)) {
            throw new IllegalArgumentException("Se detectó un ciclo en el árbol.");
        }

        visitados.add(actual);

        for (NodoArbol hijo : actual.getHijos()) {
            recorrerDFS(hijo, visitados);
        }
    }

    public void asignarNiveles() {
        if (raiz == null) {
            return;
        }

        Queue<NodoArbol> cola = new LinkedList<>();
        raiz.setNivel(0);
        cola.offer(raiz);

        while (!cola.isEmpty()) {
            NodoArbol actual = cola.poll();

            for (NodoArbol hijo : actual.getHijos()) {
                hijo.setNivel(actual.getNivel() + 1);
                cola.offer(hijo);
            }
        }
    }

    public int contarNodos() {
        return nodos.size();
    }

    public int contarAristas() {
        return nodos.size() > 0 ? nodos.size() - 1 : 0;
    }

    public List<NodoArbol> obtenerHojas() {
        List<NodoArbol> hojas = new ArrayList<>();

        for (NodoArbol nodo : nodos.values()) {
            if (nodo.getHijos().isEmpty()) {
                hojas.add(nodo);
            }
        }

        return hojas;
    }

    public Map<Integer, List<NodoArbol>> obtenerNodosPorNivel() {
        Map<Integer, List<NodoArbol>> niveles = new LinkedHashMap<>();

        for (NodoArbol nodo : nodos.values()) {
            niveles.putIfAbsent(nodo.getNivel(), new ArrayList<>());
            niveles.get(nodo.getNivel()).add(nodo);
        }

        return niveles;
    }

    public List<String> obtenerAristasComoTexto() {
        List<String> aristas = new ArrayList<>();

        for (NodoArbol nodo : nodos.values()) {
            for (NodoArbol hijo : nodo.getHijos()) {
                aristas.add(nodo.getNombre() + " - " + hijo.getNombre());
            }
        }

        return aristas;
    }

    public List<String> obtenerNivelesComoTexto() {
        List<String> niveles = new ArrayList<>();

        for (NodoArbol nodo : nodos.values()) {
            niveles.add("Nodo " + nodo.getNombre() + " -> Nivel " + nodo.getNivel());
        }

        return niveles;
    }

    private Map<NodoArbol, List<NodoArbol>> construirAdyacenciaNoDirigida() {
        Map<NodoArbol, List<NodoArbol>> ady = new HashMap<>();

        for (NodoArbol nodo : nodos.values()) {
            ady.put(nodo, new ArrayList<>());
        }

        for (NodoArbol nodo : nodos.values()) {
            for (NodoArbol hijo : nodo.getHijos()) {
                ady.get(nodo).add(hijo);
                ady.get(hijo).add(nodo);
            }
        }

        return ady;
    }

    public List<NodoArbol> hallarCentroOBicentro() {
        List<NodoArbol> resultado = new ArrayList<>();

        if (nodos.isEmpty()) {
            return resultado;
        }

        if (nodos.size() == 1) {
            resultado.add(raiz);
            return resultado;
        }

        Map<NodoArbol, List<NodoArbol>> ady = construirAdyacenciaNoDirigida();
        Map<NodoArbol, Integer> grado = new HashMap<>();
        Queue<NodoArbol> hojas = new LinkedList<>();

        for (NodoArbol nodo : nodos.values()) {
            int g = ady.get(nodo).size();
            grado.put(nodo, g);

            if (g <= 1) {
                hojas.offer(nodo);
            }
        }

        int restantes = nodos.size();

        while (restantes > 2) {
            int cantidadHojas = hojas.size();
            restantes -= cantidadHojas;

            for (int i = 0; i < cantidadHojas; i++) {
                NodoArbol hoja = hojas.poll();

                for (NodoArbol vecino : ady.get(hoja)) {
                    grado.put(vecino, grado.get(vecino) - 1);

                    if (grado.get(vecino) == 1) {
                        hojas.offer(vecino);
                    }
                }
            }
        }

        resultado.addAll(hojas);
        return resultado;
    }

    public String centroOBicentroComoTexto() {
        List<NodoArbol> centros = hallarCentroOBicentro();

        if (centros.isEmpty()) {
            return "No se pudo determinar el centro.";
        }

        if (centros.size() == 1) {
            return "Centro: " + centros.get(0).getNombre();
        }

        return "Bicentro: " + centros.get(0).getNombre() + " y " + centros.get(1).getNombre();
    }
}