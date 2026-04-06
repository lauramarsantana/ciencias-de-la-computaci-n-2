package application;

import controller.InicioController;
import controller.LayoutController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class App extends Application {
    
    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/layout.fxml"));
        AnchorPane root = loader.load();

        LayoutController controller = loader.getController();

        FXMLLoader contentLoader = new FXMLLoader(getClass().getResource("/inicio.fxml"));
        AnchorPane content = contentLoader.load();

        // obtener el controlador de inicio y pasarle la referencia
        InicioController inicioController = contentLoader.getController();
        inicioController.setLayoutController(controller);

        controller.setContent(content);

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setWidth(1280);
        stage.setHeight(800);
        stage.show();
    }
}
