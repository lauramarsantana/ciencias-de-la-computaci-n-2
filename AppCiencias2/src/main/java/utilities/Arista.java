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
// hay alguna arista repetida?
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Arista arista = (Arista) o;

        // Compara origen con origen Y destino con destino
        boolean direct = Objects.equals(verticeOrigen, arista.verticeOrigen) &&
                Objects.equals(verticeDestino, arista.verticeDestino);
        // O compara origen con destino Y destino con origen
        boolean reverse = Objects.equals(verticeOrigen, arista.verticeDestino) &&
                Objects.equals(verticeDestino, arista.verticeOrigen);
        return direct || reverse;
    }
//hashCode genera una dirección dentro de su estructura para guardar esta info
    @Override
    public int hashCode() {
        // Esto es un truco para que el orden de los vértices no afecte el hash
        return verticeOrigen.hashCode() + verticeDestino.hashCode();
    }
}
