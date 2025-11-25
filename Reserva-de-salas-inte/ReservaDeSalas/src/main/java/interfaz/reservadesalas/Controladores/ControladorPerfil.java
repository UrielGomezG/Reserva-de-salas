package interfaz.reservadesalas.Controladores;

import java.io.IOException;
import interfaz.reservadesalas.Modelo.Usuario;
import interfaz.reservadesalas.util.ResourceManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

public class ControladorPerfil extends ControladorPrincipal{

    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private TextField matriculaField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;

    @FXML
    public void initialize() {
        super.initialize();
        cargarDatosUsuario();
    }

    private void cargarDatosUsuario() {
        if (nameField == null || emailField == null || matriculaField == null) {
            System.err.println("Error: Campos del formulario no están inicializados.");
            return;
        }
        
        Usuario usuarioActual = usuarioService.getUsuarioActual();
        if (usuarioActual != null) {
            nameField.setText(usuarioActual.getNombre() != null ? usuarioActual.getNombre() : "");
            emailField.setText(usuarioActual.getEmail() != null ? usuarioActual.getEmail() : "");
            matriculaField.setText(usuarioActual.getMatricula() != null ? usuarioActual.getMatricula() : "");
            emailField.setEditable(false); // El email no se puede cambiar
        } else {
            // Datos por defecto si no hay usuario logueado
            nameField.setText("Muchacho Alias Tony");
            emailField.setText("muchacho123@utez.edu.mx");
            matriculaField.setText("muchacho123");
            emailField.setEditable(false);
        }
    }

    @FXML
    private void handleCambiarFoto(ActionEvent event) {
        System.out.println("Acción: Abrir selector de archivos para cambiar foto.");
    }

    @FXML
    protected void handleGuardarCambios(ActionEvent event) {
        if (nameField == null || emailField == null || matriculaField == null || 
            passwordField == null || confirmPasswordField == null) {
            mostrarAlerta(AlertType.ERROR, "Error", "Los campos del formulario no están inicializados.");
            return;
        }
        
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String matricula = matriculaField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (name.isEmpty() || matricula.isEmpty()) {
            mostrarAlerta(AlertType.WARNING, "Campos vacíos", "El nombre y la matrícula son obligatorios.");
            return;
        }

        if (!password.isEmpty()) {
            if (!password.equals(confirmPassword)) {
                mostrarAlerta(AlertType.ERROR, "Error de validación", "Las contraseñas no coinciden.");
                return;
            }
            if (password.length() < 6) {
                mostrarAlerta(AlertType.WARNING, "Contraseña débil", "La contraseña debe tener al menos 6 caracteres.");
                return;
            }
        }

        Usuario usuarioActual = usuarioService.getUsuarioActual();
        if (usuarioActual != null) {
            Usuario usuarioActualizado = new Usuario();
            usuarioActualizado.setNombre(name);
            usuarioActualizado.setEmail(email);
            usuarioActualizado.setMatricula(matricula);
            if (!password.isEmpty()) {
                usuarioActualizado.setContrasena(password);
            } else {
                usuarioActualizado.setContrasena(usuarioActual.getContrasena());
            }

            boolean exito = usuarioService.actualizarUsuario(usuarioActualizado);
            if (exito) {
                mostrarAlerta(AlertType.INFORMATION, "Éxito", "Los cambios se han guardado correctamente.");
                passwordField.clear();
                confirmPasswordField.clear();
            } else {
                mostrarAlerta(AlertType.ERROR, "Error", "No se pudieron guardar los cambios.");
            }
        } else {
            mostrarAlerta(AlertType.WARNING, "Sesión no iniciada", "Debe iniciar sesión para actualizar su perfil.");
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
    private void cambiarAVistaInicio(MouseEvent event) {
        navegar(event, "VistaInicio.fxml", "Starsoft - Mis Reservas");
    }

    @FXML
    private void cambiarAVistaCalendario(MouseEvent event) {
        navegar(event, "VistaCalendario.fxml", "Starsoft - Calendario de Reservas");
    }

    @FXML
    private void cambiarAVistaReporte(MouseEvent event) {
        navegar(event, "VistaReporte.fxml", "Starsoft - Reporte");
    }
    
    @FXML
    private void cambiarAVistaPerfil(MouseEvent event) {
        System.out.println("Navegación: Ya estás en la Vista de Perfil.");
    }
    
    private void navegar(MouseEvent event, String fxmlName, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(ResourceManager.getViewResource(fxmlName));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = stage.getScene();
            scene.setRoot(root);
            String css = fxmlName.equals("VistaCalendario.fxml")
                    ? ResourceManager.getStyleExternalForm("EstilosCalendario.css")
                    : ResourceManager.getStyleExternalForm("EstilosInicio.css");
            if (css != null) {
                scene.getStylesheets().clear();
                scene.getStylesheets().add(css);
            }
            root.applyCss();
            stage.setTitle(title);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error al cargar la vista " + fxmlName + ": " + e.getMessage());
        }
    }
}