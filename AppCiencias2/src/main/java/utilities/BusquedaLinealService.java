package utilities;

public class BusquedaLinealService {

    public record ResultadoBusqueda(
            boolean encontrado,
            int indice,
            int comparaciones,
            long nanos
    ) {}

    public ResultadoBusqueda buscar(int[] arr, int objetivo) {
        int comparaciones = 0;
        long inicio = System.nanoTime();

        for (int i = 0; i < arr.length; i++) {
            comparaciones++;
            if (arr[i] == objetivo) {
                long fin = System.nanoTime();
                return new ResultadoBusqueda(true, i, comparaciones, fin - inicio);
            }
        }

        long fin = System.nanoTime();
        return new ResultadoBusqueda(false, -1, comparaciones, fin - inicio);
    }
}
