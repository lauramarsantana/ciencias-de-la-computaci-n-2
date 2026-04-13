package utilities;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class SlotIndice {

    private final SimpleIntegerProperty bloque = new SimpleIntegerProperty();
    private final SimpleStringProperty contenido = new SimpleStringProperty("");

    public SlotIndice(int bloque, String contenido) {
        this.bloque.set(bloque);
        this.contenido.set(contenido);
    }

    public int getBloque() { return bloque.get(); }
    public void setBloque(int v) { bloque.set(v); }

    public String getContenido() { return contenido.get(); }
    public void setContenido(String v) { contenido.set(v); }
}