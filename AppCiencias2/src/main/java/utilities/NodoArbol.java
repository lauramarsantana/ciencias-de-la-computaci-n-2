package utilities;

import java.util.ArrayList;
import java.util.List;

public class NodoArbol {
    private String nombre;
    private NodoArbol padre;
    private List<NodoArbol> hijos;
    private int nivel;
    private double x;
    private double y;

    public NodoArbol(String nombre) {
        this.nombre = nombre;
        this.hijos = new ArrayList<>();
        this.padre = null;
        this.nivel = 0;
    }

    public String getNombre() {
        return nombre;
    }

    public NodoArbol getPadre() {
        return padre;
    }

    public void setPadre(NodoArbol padre) {
        this.padre = padre;
    }

    public List<NodoArbol> getHijos() {
        return hijos;
    }

    public void agregarHijo(NodoArbol hijo) {
        hijos.add(hijo);
    }

    public int getNivel() {
        return nivel;
    }

    public void setNivel(int nivel) {
        this.nivel = nivel;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    @Override
    public String toString() {
        return nombre;
    }
}