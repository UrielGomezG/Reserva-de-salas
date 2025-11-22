package interfaz.reservadesalas.Controladores;

import java.io.IOException;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

public class ControladorPerfil extends ControladorPrincipal{

    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private TextField matriculaField;
    @FXML private TextField passwordField;
    @FXML private TextField confirmPasswordField;

    @FXML
    public void initialize() {
        cargarDatosUsuario();
    }

    private void cargarDatosUsuario() {
        nameField.setText("Muchacho Alias Tony");
        emailField.setText("muchacho123@utez.edu.mx");
        matriculaField.setText("muchacho123");
    }

    @FXML
    private void handleCambiarFoto(ActionEvent event) {
        System.out.println("Acción: Abrir selector de archivos para cambiar foto.");
    }

    @FXML
    private void handleGuardarCambios(ActionEvent event) {
        String name = nameField.getText();
        String email = emailField.getText();
        String matricula = matriculaField.getText();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        System.out.println("Acción: Guardando cambios del perfil...");
        
        if (!password.equals(confirmPassword)) {
            System.err.println("Error: Las contraseñas no coinciden.");
            return;
        }

        System.out.println("Datos a actualizar: Nombre=" + name + ", Email=" + email + ", Matrícula=" + matricula);
        if (!password.isEmpty()) {
            System.out.println("Se intentará actualizar la contraseña.");
        }
        
        System.out.println("Cambios guardados con éxito.");
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
            String path = "/interfaz/reservadesalas/Vista/" + fxmlName;
            FXMLLoader loader = new FXMLLoader(getClass().getResource(path));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = stage.getScene();
            scene.setRoot(root);
            String css = fxmlName.equals("VistaCalendario.fxml")
                    ? getClass().getResource("/interfaz/reservadesalas/CSS/EstilosCalendario.css").toExternalForm()
                    : getClass().getResource("/interfaz/reservadesalas/CSS/EstilosInicio.css").toExternalForm();
            scene.getStylesheets().clear();
            scene.getStylesheets().add(css);
            root.applyCss();
            stage.setTitle(title);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error al cargar la vista " + fxmlName + ": " + e.getMessage());
        }
    }
}