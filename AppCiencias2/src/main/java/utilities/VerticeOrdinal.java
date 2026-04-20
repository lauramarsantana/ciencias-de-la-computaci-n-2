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

    public int getEtiquetaOrdinal() {
        return etiquetaOrdinal;
    }

    public void setEtiquetaOrdinal(int etiquetaOrdinal) {
        this.etiquetaOrdinal = etiquetaOrdinal;
    }
}
