package utilities;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FuncionOrdinalService {

    public ResultadoOrdinal calcularFuncionOrdinal(List<VerticeOrdinal> vertices,
                                                   List<AristaDirigida> aristas) {

        Map<String, Integer> gradosEntrada = new HashMap<>();
        Map<String, Integer> etiquetas = new HashMap<>();

        List<String> ordenEtiquetado = new ArrayList<>();
        List<String> pasos = new ArrayList<>();

        for (VerticeOrdinal v : vertices) {
            gradosEntrada.put(v.getNombre(), 0);
            etiquetas.put(v.getNombre(), 0);
            v.setEtiquetaOrdinal(0);
        }

        for (AristaDirigida arista : aristas) {
            String destino = arista.getDestino();
            gradosEntrada.put(destino, gradosEntrada.get(destino) + 1);
        }

        int etiquetaActual = 1;

        while (ordenEtiquetado.size() < vertices.size()) {
            
            List<VerticeOrdinal> candidatos = new ArrayList<>();

            for (VerticeOrdinal v : vertices) {
                if (etiquetas.get(v.getNombre()) == 0
                        && gradosEntrada.get(v.getNombre()) == 0) {
                    candidatos.add(v);
                }
            }

            if (candidatos.isEmpty()) {
                pasos.add("Se detiene el proceso: se encontró un ciclo y no se puede continuar.");
                return new ResultadoOrdinal(ordenEtiquetado, pasos, etiquetas, true);
            }

            candidatos.sort(
                    Comparator.comparingInt(VerticeOrdinal::getY)
                              .thenComparingInt(VerticeOrdinal::getX)
            );

            VerticeOrdinal elegido = candidatos.get(0);

            etiquetas.put(elegido.getNombre(), etiquetaActual);
            elegido.setEtiquetaOrdinal(etiquetaActual);
            ordenEtiquetado.add(elegido.getNombre());

            pasos.add(
                    "Etiqueta "
                    + etiquetaActual
                    + ": vértice "
                    + elegido.getNombre()
                    + " seleccionado."
            );

            etiquetaActual++;

            for (AristaDirigida arista : aristas) {
                if (arista.getOrigen().equals(elegido.getNombre())) {
                    String destino = arista.getDestino();

                    gradosEntrada.put(
                            destino,
                            gradosEntrada.get(destino) - 1
                    );
                }
            }
        }

        pasos.add("La función ordinal se completó correctamente.");
        return new ResultadoOrdinal(ordenEtiquetado, pasos, etiquetas, false);
    }
}