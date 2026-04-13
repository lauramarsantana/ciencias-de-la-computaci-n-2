package utilities;

public class AristaPonderada {
    private String origen;
    private String destino;
    private int peso;

    public AristaPonderada(String origen, String destino, int peso) {
        this.origen = origen;
        this.destino = destino;
        this.peso = peso;
    }

    public String getOrigen() {
        return origen;
    }

    public String getDestino() {
        return destino;
    }

    public int getPeso() {
        return peso;
    }

    @Override
    public String toString() {
        return origen + " - " + destino + " (" + peso + ")";
    }
}
