package utilities;

public class NodoMulti {
    public String letra; // Solo tendrá valor si es una hoja
    public NodoMulti[] hijos;
    public boolean esHoja = false;

    public NodoMulti(int M) {
        this.hijos =  new NodoMulti[M];// El tamaño depende de lo que elija el usuario M=2^m , m=# digitos a asociar.
    }
}
