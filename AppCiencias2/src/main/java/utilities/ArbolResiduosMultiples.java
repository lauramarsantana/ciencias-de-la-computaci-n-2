package utilities;


import javafx.fxml.FXML;

public class ArbolResiduosMultiples {
    private NodoMulti raiz;
    private int m; // bits a agrupar (ej: 2)
    private int M; // número de hijos (ej: 2^2 = 4)

    public ArbolResiduosMultiples(int m) {
        this.m = m;
        this.M = (int) Math.pow(2, m);
        this.raiz = new NodoMulti(M);
    }

    /**
     * Toma un pedazo de bits y lo convierte en el índice del hijo.
     */
    public int obtenerIndice(String bits) {
        // Integer.parseInt con base 2 convierte "11" en 3, "10" en 2, etc.
        return Integer.parseInt(bits, 2);
    }

    public NodoMulti getRaiz(){
        return raiz;
    }

    public void insertar(String letra, String binario) {
        // 1. Asegurarnos de que el binario sea múltiplo de 'm'
        // Si m=2 y el binario mide 5, lo convertimos en 6 agregando un 0 al final
        while (binario.length() % m != 0) {
            binario += "0";
        }

        NodoMulti actual = raiz;

        // 2. Recorrer la cadena de 'm' en 'm'
        for (int i = 0; i < binario.length(); i += m) {
            // Tomamos el grupo de bits (ej: "10")
            String grupo = binario.substring(i, i + m);

            // Convertimos ese grupo en un índice (0, 1, 2 o 3 si m=2)
            int indice = obtenerIndice(grupo);

            // 3. ¿Hay camino? Si no, lo creamos
            if (actual.hijos[indice] == null) {
                actual.hijos[indice] = new NodoMulti(M);
            }

            // Nos movemos al siguiente nodo
            actual = actual.hijos[indice];
        }

        // 4. Al final del camino, marcamos como hoja y guardamos la letra
        actual.esHoja = true;
        actual.letra = letra;
    }
}
