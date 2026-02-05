package application;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import utilities.Paths;

public class App extends Application {

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) throws Exception {

        AnchorPane load = FXMLLoader.load(getClass().getResource(Paths.inicio)); // aqui adentro esta la vista cargada
        Scene scene = new Scene(load); // escena donde entra todos
        stage.setScene(scene);

        stage.show();
    }
}
