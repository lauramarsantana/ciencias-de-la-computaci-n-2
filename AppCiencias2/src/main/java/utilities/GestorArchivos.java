package utilities;

import java.io.File;
import java.io.PrintWriter;
import java.util.List;

public class GestorArchivos {

    public static void guardar(File file, List<String> vertices, List<AristaPonderada> aristas) {
        try (PrintWriter pw = new PrintWriter(file)) {
            pw.println("TIPO=GENERAL");
            pw.println("VERTICES=" + String.join(",", vertices));
            pw.println("ARISTAS");
            for (AristaPonderada a : aristas) {
                // Guardamos: origen|destino|peso
                pw.println(a.getOrigen() + "|" + a.getDestino() + "|" + a.getPeso());
            }
            pw.println("END");
        } catch (Exception e) { /* Alerta de error */ }
    }
}