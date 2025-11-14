package interfaz.reservadesalas.Lanzador;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class Main extends Application {

    @Override
    public void start(Stage escenarioPrincipal) throws IOException {

        // Carga la vista FXML
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/interfaz/reservadesalas/vista/VistaLogin.fxml"));
        Parent raiz = loader.load();

        // Carga la hoja de estilos CSS
        URL cssUrl = getClass().getResource("/interfaz/reservadesalas/CSS/EstilosLogin.css");
        String css = (cssUrl != null) ? cssUrl.toExternalForm() : null;

        Scene escena = new Scene(raiz, 800, 600); // Tamaño inicial
        if (css != null) {
            escena.getStylesheets().add(css);
        } else {
            System.out.println("Advertencia: No se pudo encontrar la hoja de estilos 'EstilosLogin.css'");
        }

        // Configura la ventana principal (Stage)
        escenarioPrincipal.setTitle("Starsoft - Iniciar Sesión");
        escenarioPrincipal.setScene(escena);
        escenarioPrincipal.setMinHeight(500); // Altura mínima
        escenarioPrincipal.setMinWidth(700);  // Anchura mínima
        escenarioPrincipal.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}