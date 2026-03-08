package utilities;

import java.util.ArrayList;
import java.util.List;

public class BloqueHash {

    private final int numeroBloque;
    private final List<SlotHashExterno> slots;
    private Integer siguienteBloque;

    public BloqueHash(int numeroBloque, int capacidad, int posicionInicial) {
        this.numeroBloque = numeroBloque;
        this.slots = new ArrayList<>();

        for (int i = 0; i < capacidad; i++) {
            SlotHashExterno slot = new SlotHashExterno(posicionInicial + i);
            slot.setBloque(numeroBloque);
            slots.add(slot);
        }

        this.siguienteBloque = null;
    }

    public int getNumeroBloque() {
        return numeroBloque;
    }

    public List<SlotHashExterno> getSlots() {
        return slots;
    }

    public Integer getSiguienteBloque() {
        return siguienteBloque;
    }

    public void setSiguienteBloque(Integer siguienteBloque) {
        this.siguienteBloque = siguienteBloque;
    }

    public boolean estaLleno() {
        for (SlotHashExterno slot : slots) {
            if (slot.isVacio()) {
                return false;
            }
        }
        return true;
    }

    public SlotHashExterno buscarEspacioLibre() {
        for (SlotHashExterno slot : slots) {
            if (slot.isVacio()) {
                return slot;
            }
        }
        return null;
    }

    public SlotHashExterno buscarClave(String clave) {
        for (SlotHashExterno slot : slots) {
            if (clave.equals(slot.getClave())) {
                return slot;
            }
        }
        return null;
    }
}