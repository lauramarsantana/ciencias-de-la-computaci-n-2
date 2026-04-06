package utilities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HashExpTotales {

    private final double EXPAND = 0.75;  // >= 75% expande
    private final double SHRINK = 0.25;  // <= 25% reduce

    private int n; // número de cubetas
    private int filas; // cantidad de filas por cubeta

    private final List<Cubeta> cubetas = new ArrayList<>();
    private final List<String> pendientes = new ArrayList<>();

    public HashExpTotales(int nInicial, int filas) {
    if (nInicial < 2) nInicial = 2;
    if (filas < 2) filas = 2;

    this.n = nInicial;
    this.filas = filas;
    crearCubetas();
}

    private void crearCubetas() {
        cubetas.clear();
        for (int i = 0; i < n; i++) {
            cubetas.add(new Cubeta(filas));
        }
    }

    private int hash(String clave) {
        if (clave == null) return -1;

        clave = clave.trim();
        if (clave.isEmpty()) return -1;

        int k = Integer.parseInt(clave);
        int r = k % n;
        return (r < 0) ? r + n : r;
    }

    public int getN() {
        return n;
    }

    public int getFilas() {
        return filas;
    }

    public List<String> getPendientes() {
        return Collections.unmodifiableList(pendientes);
    }

    public int totalOcupados() {
        int c = 0;
        for (Cubeta b : cubetas) {
            c += b.ocupados();
        }
        return c;
    }

    public double densidadOcupacional() {
        int espacios = n * filas;
        return espacios == 0 ? 0 : (double) totalOcupados() / espacios;
    }

    public boolean contiene(String clave) {
        if (clave == null || clave.isBlank()) return false;
        if (pendientes.contains(clave)) return true;

        int idx = hash(clave);
        if (idx == -1) return false;

        return cubetas.get(idx).filaDe(clave) != -1;
    }

    public void insertar(String clave) {
        if (clave == null || clave.isBlank()) return;
        clave = clave.trim();

        if (contiene(clave)) return;

        int idx = hash(clave);
        if (idx == -1) return;

        Cubeta b = cubetas.get(idx);

        if (!b.insertar(clave)) {
            pendientes.add(clave);
            expandirHastaSinPendientes();
            return;
        }

        if (densidadOcupacional() >= EXPAND) {
            expandirUnaVez();
            reubicarPendientesSiSePuede();
        }
    }

    public String buscarInfo(String clave) {
        if (clave == null || clave.isBlank()) return null;
        clave = clave.trim();

        if (pendientes.contains(clave)) {
            return "La clave " + clave + " está pendiente (fuera del cuadro por colisión).";
        }

        int idx = hash(clave);
        if (idx == -1) return null;

        int fila = cubetas.get(idx).filaDe(clave);
        if (fila == -1) return null;

        return "Encontrada en cubeta " + idx + ", fila " + fila + " (h(k)=" + idx + ").";
    }

    public boolean eliminar(String clave) {
        if (clave == null || clave.isBlank()) return false;
        clave = clave.trim();

        if (pendientes.remove(clave)) {
            if (densidadOcupacional() <= SHRINK && n > 2) {
                reducirUnaVez();
                reubicarPendientesSiSePuede();
            }
            return true;
        }

        int idx = hash(clave);
        if (idx == -1) return false;

        boolean ok = cubetas.get(idx).eliminar(clave);
        if (!ok) return false;

        reubicarPendientesSiSePuede();

        if (densidadOcupacional() <= SHRINK && n > 2) {
            reducirUnaVez();
            reubicarPendientesSiSePuede();
        }
        return true;
    }

    public List<SlotCubeta> snapshotTabla() {
        List<SlotCubeta> out = new ArrayList<>();

        for (int i = 0; i < n; i++) {
            Cubeta b = cubetas.get(i);
            SlotCubeta slot = new SlotCubeta(i, filas);

            for (int f = 0; f < filas; f++) {
                slot.setFila(f, b.getFila(f));
            }

            out.add(slot);
        }

        return out;
    }

    private void expandirHastaSinPendientes() {
        int seguridad = 20;
        while (!pendientes.isEmpty() && seguridad-- > 0) {
            expandirUnaVez();
            reubicarPendientesSiSePuede();
        }
    }

    private void expandirUnaVez() {
        rehashConNuevoN(n * 2);
    }

    private void reducirUnaVez() {
        int nuevo = Math.max(2, n / 2);
        rehashConNuevoN(nuevo);
    }

    private void rehashConNuevoN(int nuevoN) {
        List<String> todas = new ArrayList<>();

        for (Cubeta b : cubetas) {
            todas.addAll(b.obtenerClaves());
        }

        for (String p : pendientes) {
            if (p != null && !p.isBlank()) {
                todas.add(p.trim());
            }
        }

        pendientes.clear();

        n = Math.max(2, nuevoN);
        crearCubetas();

        for (String c : todas) {
            if (c == null || c.isBlank()) continue;

            int idx = hash(c);
            if (idx == -1) continue;

            if (!cubetas.get(idx).insertar(c)) {
                pendientes.add(c);
            }
        }
    }

    private void reubicarPendientesSiSePuede() {
        if (pendientes.isEmpty()) return;

        List<String> still = new ArrayList<>();

        for (String c : pendientes) {
            if (c == null || c.isBlank()) continue;

            int idx = hash(c);
            if (idx == -1) continue;

            if (!cubetas.get(idx).insertar(c)) {
                still.add(c);
            }
        }

        pendientes.clear();
        pendientes.addAll(still);
    }

    private static class Cubeta {
        private final List<String> filas;

        public Cubeta(int cantidadFilas) {
            filas = new ArrayList<>();
            for (int i = 0; i < cantidadFilas; i++) {
                filas.add("");
            }
        }

        int ocupados() {
            int c = 0;
            for (String fila : filas) {
                if (fila != null && !fila.isBlank()) {
                    c++;
                }
            }
            return c;
        }

        boolean insertar(String clave) {
            for (int i = 0; i < filas.size(); i++) {
                String valor = filas.get(i);
                if (valor == null || valor.isBlank()) {
                    filas.set(i, clave);
                    return true;
                }
            }
            return false;
        }

        int filaDe(String clave) {
            for (int i = 0; i < filas.size(); i++) {
                String valor = filas.get(i);
                if (valor != null && valor.equals(clave)) {
                    return i + 1;
                }
            }
            return -1;
        }

        boolean eliminar(String clave) {
            for (int i = 0; i < filas.size(); i++) {
                String valor = filas.get(i);
                if (valor != null && valor.equals(clave)) {
                    filas.set(i, "");
                    return true;
                }
            }
            return false;
        }

        String getFila(int indice) {
            if (indice < 0 || indice >= filas.size()) return "";
            return filas.get(indice);
        }

        List<String> obtenerClaves() {
            List<String> claves = new ArrayList<>();
            for (String valor : filas) {
                if (valor != null && !valor.isBlank()) {
                    claves.add(valor.trim());
                }
            }
            return claves;
        }
    }
}