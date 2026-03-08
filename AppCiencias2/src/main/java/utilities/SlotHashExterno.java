package utilities;

import java.util.ArrayList;
import java.util.List;

public class SlotHashExterno {
    private final int posicion;
    private int bloque;
    private int hash = -1;
    private String clave = "";
    private final List<String> colisiones = new ArrayList<>();

    public SlotHashExterno(int posicion) {
        this.posicion = posicion;
    }

    public int getPosicion() {
        return posicion;
    }

    public int getBloque() {
        return bloque;
    }

    public void setBloque(int bloque) {
        this.bloque = bloque;
    }

    public int getHash() {
        return hash;
    }

    public void setHash(int hash) {
        this.hash = hash;
    }

    public String getClave() {
        return clave;
    }

    public void setClave(String clave) {
        this.clave = clave;
    }

    public boolean isVacio() {
        return clave == null || clave.isBlank();
    }

    public List<String> getColisiones() {
        return colisiones;
    }

    public String getColisionesTexto() {
        return String.join(", ", colisiones);
    }
}
