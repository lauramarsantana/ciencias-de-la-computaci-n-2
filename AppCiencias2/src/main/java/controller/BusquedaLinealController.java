package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class BusquedaLinealController {

    @FXML
    private TextField inputField;

    @FXML
    private Label resultadoLabel;

    @FXML
    private void realizarBusqueda() {
        try {
            int valor = Integer.parseInt(inputField.getText());
            int[] datos = {5, 10, 15, 20, 25}; // ejemplo de datos

            boolean encontrado = false;
            for (int i = 0; i < datos.length; i++) {
                if (datos[i] == valor) {
                    resultadoLabel.setText("Valor encontrado en la posición " + i);
                    encontrado = true;
                    break;
                }
            }

            if (!encontrado) {
                resultadoLabel.setText("Valor no encontrado");
            }
        } catch (NumberFormatException e) {
            resultadoLabel.setText("Ingrese un número válido");
        }
    }
}
