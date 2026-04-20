package utilities;

import java.util.List;
import java.util.Map;

public class ResultadoOrdinal {
    private List<String> ordenEtiquetado;
    private List<String> pasos;
    private Map<String, Integer> etiquetas;
    private boolean hayCiclo;

    public ResultadoOrdinal(List<String> ordenEtiquetado, List<String> pasos,
                            Map<String, Integer> etiquetas, boolean hayCiclo) {
        this.ordenEtiquetado = ordenEtiquetado;
        this.pasos = pasos;
        this.etiquetas = etiquetas;
        this.hayCiclo = hayCiclo;
    }

    public List<String> getOrdenEtiquetado() {
        return ordenEtiquetado;
    }

    public List<String> getPasos() {
        return pasos;
    }

    public Map<String, Integer> getEtiquetas() {
        return etiquetas;
    }

    public boolean isHayCiclo() {
        return hayCiclo;
    }
}