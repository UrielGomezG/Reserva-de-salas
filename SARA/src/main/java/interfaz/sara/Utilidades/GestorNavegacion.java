package interfaz.sara.Utilidades;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Gestor de navegación para vistas comunes (login, registro, recuperación)
 * Usa patrón Singleton para mantener una única instancia
 */
public class GestorNavegacion {
    
    private static GestorNavegacion instancia;
    private Stage escenarioPrincipal;
    private Scene escenaActual;
    private static String correoRecuperacion = null;
    
    private GestorNavegacion() {
    }
    
    public static GestorNavegacion obtenerInstancia() {
        if (instancia == null) {
            synchronized (GestorNavegacion.class) {
                if (instancia == null) {
                    instancia = new GestorNavegacion();
                }
            }
        }
        return instancia;
    }
    
    public void establecerEscenarioPrincipal(Stage escenario) {
        this.escenarioPrincipal = escenario;
        if (escenario.getScene() != null) {
            this.escenaActual = escenario.getScene();
        }
    }
    
    public void navegarALogin() {
        cambiarAVista("/interfaz/sara/Vistas/VistaLogin.fxml", "/interfaz/sara/CSS/login.css");
    }
    
    public void navegarARegistro() {
        cambiarAVista("/interfaz/sara/Vistas/VistaRegistro.fxml", "/interfaz/sara/CSS/login.css");
    }
    
    public void navegarAVistaRecuperarContrasena() {
        cambiarAVista("/interfaz/sara/Vistas/VistaRecuperarContrasena.fxml", "/interfaz/sara/CSS/login.css");
    }
    
    public void navegarAVistaValidarCodigo(String correo) {
        establecerCorreoRecuperacion(correo);
        cambiarAVista("/interfaz/sara/Vistas/VistaValidarCodigo.fxml", "/interfaz/sara/CSS/login.css");
    }
    
    public void navegarAVistaCambiarContrasena(String correo) {
        establecerCorreoRecuperacion(correo);
        cambiarAVista("/interfaz/sara/Vistas/VistaCambiarContrasena.fxml", "/interfaz/sara/CSS/login.css");
    }
    
    public void establecerCorreoRecuperacion(String correo) {
        correoRecuperacion = correo;
    }
    
    public String obtenerCorreoRecuperacion() {
        return correoRecuperacion;
    }
    
    /**
     * Cambia a una vista específica cargando el FXML y aplicando estilos CSS
     */
    public void cambiarAVista(String rutaFXML, String rutaCSS) {
        if (escenarioPrincipal == null) {
            System.err.println("Error: El escenario principal no ha sido establecido");
            return;
        }
        
        try {
            boolean estabaMaximizada = escenarioPrincipal.isMaximized();
            double anchoAnterior = escenarioPrincipal.getWidth();
            double altoAnterior = escenarioPrincipal.getHeight();
            
            FXMLLoader loader = new FXMLLoader(GestorNavegacion.class.getResource(rutaFXML));
            Parent root = loader.load();
            
            Scene escena;
            if (escenaActual == null) {
                escena = new Scene(root);
            } else {
                escena = (anchoAnterior > 0 && altoAnterior > 0) 
                    ? new Scene(root, anchoAnterior, altoAnterior)
                    : new Scene(root);
            }
            
            if (rutaCSS != null && !rutaCSS.isEmpty()) {
                String css = GestorNavegacion.class.getResource(rutaCSS).toExternalForm();
                escena.getStylesheets().clear();
                escena.getStylesheets().add(css);
                
                String animationsCSS = GestorNavegacion.class.getResource("/interfaz/sara/CSS/animations.css").toExternalForm();
                escena.getStylesheets().add(animationsCSS);
            }
            
            escenarioPrincipal.setScene(escena);
            escenaActual = escena;
            
            if (estabaMaximizada) {
                escenarioPrincipal.setMaximized(true);
            }
            
            escenarioPrincipal.show();
            
        } catch (IOException e) {
            System.err.println("Error al cargar la vista: " + rutaFXML);
            System.err.println("Mensaje: " + e.getMessage());
        }
    }
    
    public Stage obtenerEscenarioPrincipal() {
        return escenarioPrincipal;
    }
    
    public Scene obtenerEscenaActual() {
        return escenaActual;
    }
}
