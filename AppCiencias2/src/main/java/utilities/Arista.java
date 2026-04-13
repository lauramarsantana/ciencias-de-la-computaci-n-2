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

        // A-B es lo mismo que B-A
        boolean normal = verticeOrigen.equals(arista.verticeOrigen) && verticeDestino.equals(arista.verticeDestino);
        boolean invertido = verticeOrigen.equals(arista.verticeDestino) && verticeDestino.equals(arista.verticeOrigen);

        return normal || invertido;
    }
//hashCode genera una dirección dentro de su estructura para guardar esta info
    @Override
    public int hashCode() {
        // Esto es un truco para que el orden de los vértices no afecte el hash
        return verticeOrigen.hashCode() + verticeDestino.hashCode();
    }
}
