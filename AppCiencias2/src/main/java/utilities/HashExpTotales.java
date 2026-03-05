package utilities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HashExpTotales {

    public static final int FILAS = 2;
    private final double EXPAND = 0.75;  // >= 75% expande
    private final double SHRINK = 0.25;  // <= 25% reduce

    private int n; // número de cubetas (2,4,8,...)
    private final List<Cubeta> cubetas = new ArrayList<>();
    private final List<String> pendientes = new ArrayList<>(); // colisiones "por fuera"

    public HashExpTotales(int nInicial) {
        if (nInicial < 2) nInicial = 2;
        this.n = ajustarPotenciaDe2(nInicial);
        crearCubetas();
    }

    private int ajustarPotenciaDe2(int x) {
        int p = 1;
        while (p < x) p <<= 1;
        return Math.max(2, p);
    }

    private void crearCubetas() {
        cubetas.clear();
        for (int i = 0; i < n; i++) cubetas.add(new Cubeta());
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

    public List<String> getPendientes() {
        return Collections.unmodifiableList(pendientes);
    }

    public int totalOcupados() {
        int c = 0;
        for (Cubeta b : cubetas) c += b.ocupados();
        return c;
    }

    public double densidadOcupacional() {
        int espacios = n * FILAS;
        return espacios == 0 ? 0 : (double) totalOcupados() / espacios;
    }

    public boolean contiene(String clave) {
        if (clave == null || clave.isBlank()) return false;
        if (pendientes.contains(clave)) return true;

        int idx = hash(clave);
        if (idx == -1) return false;

        return cubetas.get(idx).filaDe(clave) != -1;
    }

    /** Inserta: si cubeta llena -> pendiente y expande hasta reubicar. */
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
            out.add(new SlotCubeta(i,
                    b.fila1 == null ? "" : b.fila1,
                    b.fila2 == null ? "" : b.fila2
            ));
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
            if (b.fila1 != null && !b.fila1.isBlank()) {
                todas.add(b.fila1.trim());
            }
            if (b.fila2 != null && !b.fila2.isBlank()) {
                todas.add(b.fila2.trim());
            }
        }

        for (String p : pendientes) {
            if (p != null && !p.isBlank()) {
                todas.add(p.trim());
            }
        }

        pendientes.clear();

        n = ajustarPotenciaDe2(nuevoN);
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
        private String fila1;
        private String fila2;

        int ocupados() {
            int c = 0;
            if (fila1 != null && !fila1.isBlank()) c++;
            if (fila2 != null && !fila2.isBlank()) c++;
            return c;
        }

        boolean insertar(String clave) {
            if (fila1 == null || fila1.isBlank()) {
                fila1 = clave;
                return true;
            }
            if (fila2 == null || fila2.isBlank()) {
                fila2 = clave;
                return true;
            }
            return false;
        }

        int filaDe(String clave) {
            if (fila1 != null && fila1.equals(clave)) return 1;
            if (fila2 != null && fila2.equals(clave)) return 2;
            return -1;
        }

        boolean eliminar(String clave) {
            if (fila1 != null && fila1.equals(clave)) {
                fila1 = "";
                return true;
            }
            if (fila2 != null && fila2.equals(clave)) {
                fila2 = "";
                return true;
            }
            return false;
        }
    }
}