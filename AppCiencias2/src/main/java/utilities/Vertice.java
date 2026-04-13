package utilities;

import java.util.Objects;

public class Vertice {
    private String name;
    private double positionX;
    private double positionY;

    public Vertice(String name, double positionX, double positionY){
        this.name = name;
        this.positionX = positionX;
        this.positionY = positionY;
    }

    public String getName() {
        return name;
    }

    public double getPositionX() {
        return positionX;
    }

    public double getPositionY() {
        return positionY;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPositionX(double positionX) {
        this.positionX = positionX;
    }

    public void setPositionY(double positionY) {
        this.positionY = positionY;
    }

    /// que el nodo no se repita:
    // Esto es lo que hace que el HashMap sepa si el vértice ya existe
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Vertice vertice = (Vertice) o;
        return Objects.equals(name, vertice.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
