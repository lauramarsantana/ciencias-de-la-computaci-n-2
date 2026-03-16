package utilities;

public class EntradaIndice {

    private String clave;
    private int bloqueDestino;
    private int posicionDestino;

    public EntradaIndice(String clave, int bloqueDestino, int posicionDestino) {
        this.clave = clave;
        this.bloqueDestino = bloqueDestino;
        this.posicionDestino = posicionDestino;
    }

    public String getClave() {
        return clave;
    }

    public void setClave(String clave) {
        this.clave = clave;
    }

    public int getBloqueDestino() {
        return bloqueDestino;
    }

    public void setBloqueDestino(int bloqueDestino) {
        this.bloqueDestino = bloqueDestino;
    }

    public int getPosicionDestino() {
        return posicionDestino;
    }

    public void setPosicionDestino(int posicionDestino) {
        this.posicionDestino = posicionDestino;
    }
}