package utilities;

public class RegistroDato {

    private int posicion;
    private String clave;
    private int bloque;

    public RegistroDato(int posicion, String clave, int bloque) {
        this.posicion = posicion;
        this.clave = clave;
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

    public int getBloque() {
        return bloque;
    }

    public void setBloque(int bloque) {
        this.bloque = bloque;
    }
}