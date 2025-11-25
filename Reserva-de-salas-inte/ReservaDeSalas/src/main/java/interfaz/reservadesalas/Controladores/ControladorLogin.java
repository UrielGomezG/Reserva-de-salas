package interfaz.reservadesalas.Controladores;

import java.io.IOException;
import interfaz.reservadesalas.Servicio.UsuarioService;
import interfaz.reservadesalas.util.ResourceManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class ControladorLogin {

    @FXML private TextField campoEmail;
    @FXML private PasswordField campoContrasena;
    @FXML private Button botonLogin;
    @FXML private Button botonRegistro;

    private UsuarioService usuarioService;

    @FXML
    public void initialize() {
        usuarioService = UsuarioService.getInstancia();
        System.out.println("Controlador de Login inicializado.");
    }

    @FXML
    protected void alPulsarBotonLogin() {
        
        String email = campoEmail.getText().trim();
        String password = campoContrasena.getText();

        if (email.isEmpty() || password.isEmpty()) {
            mostrarAlerta(AlertType.WARNING, "Campos vacíos", "Por favor, complete todos los campos.");
            return;
        }

        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            mostrarAlerta(AlertType.WARNING, "Email inválido", "Por favor, ingrese un email válido.");
            return;
        }

        boolean loginExitoso = usuarioService.iniciarSesion(email, password);
        
        if (loginExitoso) {
            try {
                FXMLLoader loader = new FXMLLoader(ResourceManager.getViewResource("VistaInicio.fxml"));
                Parent raizInicio = loader.load(); 

                Scene escenaActual = botonLogin.getScene();
                Scene escenaInicio = new Scene(raizInicio, escenaActual.getWidth(), escenaActual.getHeight());

                String css = ResourceManager.getStyleExternalForm("EstilosInicio.css");
                if (css != null) {
                    escenaInicio.getStylesheets().add(css);
                }

                Stage escenario = (Stage) escenaActual.getWindow();
                escenario.setScene(escenaInicio);
                escenario.setTitle("Starsoft - Mis Reservas");

            } catch (IOException e) {
                System.err.println("Error al cargar la vista de inicio: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            mostrarAlerta(AlertType.ERROR, "Error de autenticación", "Email o contraseña incorrectos. Intente de nuevo.");
            campoContrasena.clear();
        }
    }

    private void mostrarAlerta(AlertType tipo, String titulo, String mensaje) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
    
    @FXML
    protected void alPulsarBotonRegistro() {
        try {
            FXMLLoader loader = new FXMLLoader(ResourceManager.getViewResource("VistaRegistro.fxml"));
            Parent raizRegistro = loader.load();

            Scene escenaActual = botonRegistro.getScene();
            Scene escenaRegistro = new Scene(raizRegistro, escenaActual.getWidth(), escenaActual.getHeight());

            String css = ResourceManager.getStyleExternalForm("EstilosRegistro.css");
            if (css != null) {
                escenaRegistro.getStylesheets().add(css);
            }

            Stage escenario = (Stage) escenaActual.getWindow();
            escenario.setScene(escenaRegistro);
            escenario.setTitle("Starsoft - Crear Cuenta");

        } catch (IOException e) {
            System.err.println("Error al cargar la vista de registro: " + e.getMessage());
            e.printStackTrace();
        }
    }
}