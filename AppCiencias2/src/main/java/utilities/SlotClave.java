package utilities;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class SlotClave {
    private final SimpleIntegerProperty posicion = new SimpleIntegerProperty();
    private final SimpleStringProperty clave = new SimpleStringProperty("");

    public SlotClave(int posicion, String clave) {
        this.posicion.set(posicion);
        this.clave.set(clave);
    }

    public int getPosicion() { return posicion.get(); }
    public void setPosicion(int v) { posicion.set(v); }

    public String getClave() { return clave.get(); }
    public void setClave(String v) { clave.set(v); }
}
