package interfaz.reservadesalas.Lanzador;

import java.io.IOException;
import java.net.URL;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage escenarioPrincipal) throws IOException {
        
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/interfaz/reservadesalas/Vista/VistaLogin.fxml"));
        Parent raiz = loader.load();
        
        URL cssUrl = getClass().getResource("/interfaz/reservadesalas/CSS/EstilosLogin.css");
        if (cssUrl == null) {
            cssUrl = Thread.currentThread().getContextClassLoader().getResource("interfaz/reservadesalas/CSS/EstilosLogin.css");
        }
        String css = (cssUrl != null) ? cssUrl.toExternalForm() : null;
        
        Scene escena = new Scene(raiz, 800, 600);
        if (css != null) {
            escena.getStylesheets().add(css);
        }
        
        escenarioPrincipal.setTitle("Starsoft - Iniciar Sesión");
        escenarioPrincipal.setScene(escena);
        escenarioPrincipal.setMinHeight(500);
        escenarioPrincipal.setMinWidth(700);
        escenarioPrincipal.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}