package utilities;

public class SlotBloqueClave {

    private int bloque;
    private int posicion;
    private String clave;

    public SlotBloqueClave(int bloque, int posicion, String clave) {
        this.bloque = bloque;
        this.posicion = posicion;
        this.clave = clave;
    }

    public int getBloque() {
        return bloque;
    }

    public void setBloque(int bloque) {
        this.bloque = bloque;
    }

    public int getPosicion() {
        return posicion;
    }

    public void setPosicion(int posicion) {
        this.posicion = posicion;
    }

    public String getClave() {
        return clave;
    }

    public void setClave(String clave) {
        this.clave = clave;
    }
}
