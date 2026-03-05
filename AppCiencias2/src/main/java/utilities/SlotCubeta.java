package utilities;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class SlotCubeta {
    private final SimpleIntegerProperty cubeta = new SimpleIntegerProperty();
    private final SimpleStringProperty fila1 = new SimpleStringProperty("");
    private final SimpleStringProperty fila2 = new SimpleStringProperty("");

    public SlotCubeta(int cubeta, String fila1, String fila2) {
        this.cubeta.set(cubeta);
        this.fila1.set(fila1 == null ? "" : fila1);
        this.fila2.set(fila2 == null ? "" : fila2);
    }

    public int getCubeta() { return cubeta.get(); }
    public void setCubeta(int v) { cubeta.set(v); }

    public String getFila1() { return fila1.get(); }
    public void setFila1(String v) { fila1.set(v == null ? "" : v); }

    public String getFila2() { return fila2.get(); }
    public void setFila2(String v) { fila2.set(v == null ? "" : v); }
}