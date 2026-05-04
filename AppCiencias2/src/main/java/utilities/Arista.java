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

        // Es igual si (Origen=Origen y Destino=Destino)
        // O si están invertidos (Origen=Destino y Destino=Origen)
        return (Objects.equals(verticeOrigen, arista.verticeOrigen) && Objects.equals(verticeDestino, arista.verticeDestino)) ||
                (Objects.equals(verticeOrigen, arista.verticeDestino) && Objects.equals(verticeDestino, arista.verticeOrigen));
    }

    @Override
    public int hashCode() {
        // Usamos la suma de los hashCodes de los vértices para que el orden no importe
        return Objects.hashCode(verticeOrigen) + Objects.hashCode(verticeDestino);
    }

    public boolean comparteVertice(Arista otra) {
        // Usamos verticeOrigen y verticeDestino que son tus variables reales
        return this.verticeOrigen.getName().equals(otra.verticeOrigen.getName()) ||
                this.verticeOrigen.getName().equals(otra.verticeDestino.getName()) ||
                this.verticeDestino.getName().equals(otra.verticeOrigen.getName()) ||
                this.verticeDestino.getName().equals(otra.verticeDestino.getName());
    }
}
