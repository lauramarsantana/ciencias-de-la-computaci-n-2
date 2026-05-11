package utilities;

import java.util.ArrayList;
import java.util.List;
import utilities.AristaPonderada;

public class DatosArchivo {

    private String tipo;

    // Árbol simple
    private String raiz;
    private List<String[]> relaciones = new ArrayList<>();

    // Grafo generador
    private List<String> vertices = new ArrayList<>();
    private List<AristaPonderada> aristas = new ArrayList<>();

    // Distancia entre árboles
    private String vertices1;
    private String aristas1;

    private String vertices2;
    private String aristas2;

    // =========================
    // GETTERS Y SETTERS
    // =========================

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getRaiz() {
        return raiz;
    }

    public void setRaiz(String raiz) {
        this.raiz = raiz;
    }

    public List<String[]> getRelaciones() {
        return relaciones;
    }

    public void setRelaciones(List<String[]> relaciones) {
        this.relaciones = relaciones;
    }

    public List<String> getVertices() {
        return vertices;
    }

    public void setVertices(List<String> vertices) {
        this.vertices = vertices;
    }

    public List<AristaPonderada> getAristas() {
        return aristas;
    }

    public void setAristas(List<AristaPonderada> aristas) {
        this.aristas = aristas;
    }

    public String getVertices1() {
        return vertices1;
    }

    public void setVertices1(String vertices1) {
        this.vertices1 = vertices1;
    }

    public String getAristas1() {
        return aristas1;
    }

    public void setAristas1(String aristas1) {
        this.aristas1 = aristas1;
    }

    public String getVertices2() {
        return vertices2;
    }

    public void setVertices2(String vertices2) {
        this.vertices2 = vertices2;
    }

    public String getAristas2() {
        return aristas2;
    }

    public void setAristas2(String aristas2) {
        this.aristas2 = aristas2;
    }
}