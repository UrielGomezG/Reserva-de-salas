package interfaz.sara;

import interfaz.sara.Utilidades.GestorNavegacion;
import javafx.application.Application;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Clase principal de la aplicación JavaFX SARA
 * Inicializa la aplicación y carga la vista de login
 */
public class SARAApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        stage.setTitle("SARA - Sistema de Reservas de Salas");
        
        // Establecer tamaños mínimos para la ventana
        stage.setMinWidth(1024);
        stage.setMinHeight(768);
        
        // Establecer tamaño inicial (se puede redimensionar)
        stage.setWidth(1280);
        stage.setHeight(800);
        
        // Permitir redimensionamiento
        stage.setResizable(true);
        
        // Centrar la ventana en la pantalla
        stage.centerOnScreen();
        
        GestorNavegacion gestorNavegacion = GestorNavegacion.obtenerInstancia();
        gestorNavegacion.establecerEscenarioPrincipal(stage);
        gestorNavegacion.navegarALogin();
        
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

