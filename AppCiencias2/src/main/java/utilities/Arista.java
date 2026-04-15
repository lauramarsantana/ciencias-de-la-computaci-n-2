package utilities;

import java.util.Objects;

public class Arista {
    private String name;
    private Vertice verticeOrigen;
    private Vertice verticeDestino;

    public Arista(String name, Vertice verticeOrigen, Vertice verticeDestino) {
        this.name = name;
        this.verticeOrigen = verticeOrigen;
        this.verticeDestino = verticeDestino;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Vertice getVerticeOrigen() {
        return verticeOrigen;
    }

    public void setVerticeOrigen(Vertice verticeOrigen) {
        this.verticeOrigen = verticeOrigen;
    }

    public Vertice getVerticeDestino() {
        return verticeDestino;
    }

    public void setVerticeDestino(Vertice verticeDestino) {
        this.verticeDestino = verticeDestino;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Arista arista = (Arista) o;
        // Solo son iguales si tienen el mismo origen y mismo destino exacto
        return Objects.equals(verticeOrigen, arista.verticeOrigen) &&
                Objects.equals(verticeDestino, arista.verticeDestino);
    }

    @Override
    public int hashCode() {
        return Objects.hash(verticeOrigen, verticeDestino);
    }
}
