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

public class ControladorRegistro {

    @FXML private TextField campoUsuario;
    @FXML private TextField campoEmail;
    @FXML private TextField campoMatricula;
    @FXML private PasswordField campoContrasena;

    @FXML private Button botonRegistrarse;
    @FXML private Button botonIrALogin;

    private UsuarioService usuarioService;

    @FXML
    public void initialize() {
        usuarioService = UsuarioService.getInstancia();
    }

    @FXML
    protected void alPulsarBotonRegistrarse() {
        String usuario = campoUsuario.getText().trim();
        String email = campoEmail.getText().trim();
        String matricula = campoMatricula.getText().trim();
        String contrasena = campoContrasena.getText();

        if (usuario.isEmpty() || email.isEmpty() || matricula.isEmpty() || contrasena.isEmpty()) {
            mostrarAlerta(AlertType.WARNING, "Campos vacíos", "Todos los campos son obligatorios.");
            return;
        }

        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            mostrarAlerta(AlertType.WARNING, "Email inválido", "Por favor, ingrese un email válido.");
            return;
        }

        if (contrasena.length() < 6) {
            mostrarAlerta(AlertType.WARNING, "Contraseña débil", "La contraseña debe tener al menos 6 caracteres.");
            return;
        }

        boolean registroExitoso = usuarioService.registrarUsuario(usuario, email, matricula, contrasena);

        if (registroExitoso) {
            mostrarAlerta(AlertType.INFORMATION, "Registro exitoso", "Su cuenta ha sido creada correctamente.");
            alPulsarBotonIrALogin();
        } else {
            mostrarAlerta(AlertType.ERROR, "Error de registro", "El email o la matrícula ya están registrados.");
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
    protected void alPulsarBotonIrALogin() {
        try {
            FXMLLoader loader = new FXMLLoader(ResourceManager.getViewResource("VistaLogin.fxml"));
            Parent raizLogin = loader.load();

            Scene escenaActual = botonIrALogin.getScene();
            Scene escenaLogin = new Scene(raizLogin, escenaActual.getWidth(), escenaActual.getHeight());

            String css = ResourceManager.getStyleExternalForm("EstilosLogin.css");
            if (css != null) {
                escenaLogin.getStylesheets().add(css);
            }

            Stage escenario = (Stage) escenaActual.getWindow();
            escenario.setScene(escenaLogin);
            escenario.setTitle("Starsoft - Iniciar Sesión");

        } catch (IOException e) {
            System.err.println("Error al cargar la vista de login: " + e.getMessage());
            e.printStackTrace();
        }
    }
}