package utilities;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import java.util.ArrayList;
import java.util.List;

public class SlotCubeta {

    private final SimpleIntegerProperty cubeta = new SimpleIntegerProperty();
    private final List<SimpleStringProperty> filas = new ArrayList<>();

    public SlotCubeta(int cubeta, int cantidadFilas) {
        this.cubeta.set(cubeta);

        for (int i = 0; i < cantidadFilas; i++) {
            filas.add(new SimpleStringProperty(""));
        }
    }

    public int getCubeta() {
        return cubeta.get();
    }

    public void setCubeta(int v) {
        cubeta.set(v);
    }

    public int getCantidadFilas() {
        return filas.size();
    }

    public String getFila(int indice) {
        if (indice < 0 || indice >= filas.size()) {
            return "";
        }
        return filas.get(indice).get();
    }

    public void setFila(int indice, String valor) {
        if (indice < 0 || indice >= filas.size()) {
            return;
        }
        filas.get(indice).set(valor == null ? "" : valor);
    }

    public List<String> getFilas() {
        List<String> valores = new ArrayList<>();
        for (SimpleStringProperty fila : filas) {
            valores.add(fila.get());
        }
        return valores;
    }

    public boolean tieneEspacio() {
        for (SimpleStringProperty fila : filas) {
            if (fila.get() == null || fila.get().isBlank()) {
                return true;
            }
        }
        return false;
    }

    public int primeraFilaLibre() {
        for (int i = 0; i < filas.size(); i++) {
            if (filas.get(i).get() == null || filas.get(i).get().isBlank()) {
                return i;
            }
        }
        return -1;
    }

    public boolean contieneClave(String clave) {
        for (SimpleStringProperty fila : filas) {
            if (fila.get().equals(clave)) {
                return true;
            }
        }
        return false;
    }

    public boolean eliminarClave(String clave) {
        for (SimpleStringProperty fila : filas) {
            if (fila.get().equals(clave)) {
                fila.set("");
                return true;
            }
        }
        return false;
    }
}