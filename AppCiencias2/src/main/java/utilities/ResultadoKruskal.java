package utilities;

import java.util.List;

public class ResultadoKruskal {

    private List<AristaPonderada> seleccionadas;
    private List<AristaPonderada> descartadasPorCiclo;

    public ResultadoKruskal(List<AristaPonderada> seleccionadas, List<AristaPonderada> descartadasPorCiclo) {
        this.seleccionadas = seleccionadas;
        this.descartadasPorCiclo = descartadasPorCiclo;
    }

    public List<AristaPonderada> getSeleccionadas() {
        return seleccionadas;
    }

    public List<AristaPonderada> getDescartadasPorCiclo() {
        return descartadasPorCiclo;
    }
}