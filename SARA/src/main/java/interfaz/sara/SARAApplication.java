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

    /**
     * Método principal de inicio de la aplicación JavaFX
     * Carga la vista de login y configura la ventana principal
     * 
     * @param stage El escenario (ventana) principal de la aplicación
     * @throws IOException Si ocurre un error al cargar el archivo FXML
     */
    @Override
    public void start(Stage stage) throws IOException {
        // Configurar el escenario (ventana)
        stage.setTitle("SARA - Sistema de Reservas de Salas");
        stage.setMaximized(true); // Maximizar ventana a pantalla completa
        stage.setFullScreen(false); // No modo pantalla completa (solo maximizado)
        
        // Establecer el escenario principal en el gestor de navegación
        GestorNavegacion gestorNavegacion = GestorNavegacion.obtenerInstancia();
        gestorNavegacion.establecerEscenarioPrincipal(stage);
        
        // Navegar a la vista de login
        gestorNavegacion.navegarALogin();
        
        // Mostrar la ventana
        stage.show();
    }

    /**
     * Método principal para ejecutar la aplicación
     * 
     * @param args Argumentos de línea de comandos
     */
    public static void main(String[] args) {
        launch(args);
    }
}

