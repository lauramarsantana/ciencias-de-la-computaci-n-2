package utilities;

public class SlotHash {
    private final int posicion;   // 1..N 
    private int hash = -1;        // hash base (0..N-1)
    private String clave = "";    // vacío si no hay clave

    public SlotHash(int posicion) {
        this.posicion = posicion;
    }

    public int getPosicion() { return posicion; }

    public int getHash() { return hash; }
    public void setHash(int hash) { this.hash = hash; }

    public String getClave() { return clave; }
    public void setClave(String clave) { this.clave = clave; }

    public boolean isVacio() {
        return clave == null || clave.isBlank();
    }
    private final java.util.List<String> colisiones = new java.util.ArrayList<>();

    public java.util.List<String> getColisiones() {
        return colisiones;
    }

    public String getColisionesTexto() {
        return String.join(", ", colisiones);
    }
}
