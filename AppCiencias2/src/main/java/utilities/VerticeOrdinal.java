package utilities;

public class VerticeOrdinal {

    private String nombre;
    private int x;
    private int y;
    private int etiquetaOrdinal;

    public VerticeOrdinal(String nombre, int x, int y) {
        this.nombre = nombre;
        this.x = x;
        this.y = y;
        this.etiquetaOrdinal = 0;
    }

    public String getNombre() {
        return nombre;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getEtiquetaOrdinal() {
        return etiquetaOrdinal;
    }

    public void setEtiquetaOrdinal(int etiquetaOrdinal) {
        this.etiquetaOrdinal = etiquetaOrdinal;
    }
}