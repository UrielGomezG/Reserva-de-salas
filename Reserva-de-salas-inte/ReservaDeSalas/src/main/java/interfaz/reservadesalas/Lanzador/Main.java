package interfaz.reservadesalas.Lanzador;

import java.io.IOException;
import interfaz.reservadesalas.util.ResourceManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws IOException {
        FXMLLoader loader = new FXMLLoader(ResourceManager.getViewResource("VistaLogin.fxml"));
        Parent root = loader.load();
        
        Scene scene = new Scene(root, 800, 600);
        // Cargar CSS usando ResourceManager
        String css = ResourceManager.getStyleExternalForm("EstilosLogin.css");
        if (css != null && !scene.getStylesheets().contains(css)) {
            scene.getStylesheets().add(css);
        }
        
        primaryStage.setTitle("Starsoft - Sistema de Reservas");
        primaryStage.setScene(scene);
        primaryStage.setMinHeight(500);
        primaryStage.setMinWidth(700);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}