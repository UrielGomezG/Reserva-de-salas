package interfaz.reservadesalas.Controladores;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class ControladorRegistro {

    // Campos del formulario
    @FXML private TextField campoUsuario;
    @FXML private TextField campoEmail;
    @FXML private TextField campoMatricula;
    @FXML private PasswordField campoContrasena;

    // Botones
    @FXML private Button botonRegistrarse;
    @FXML private Button botonIrALogin;

    @FXML
    protected void alPulsarBotonRegistrarse() {
        String usuario = campoUsuario.getText();
        String email = campoEmail.getText();
        String matricula = campoMatricula.getText();
        String contrasena = campoContrasena.getText();

        if (usuario.isEmpty() || email.isEmpty() || matricula.isEmpty() || contrasena.isEmpty()) {
            System.out.println("Error: Todos los campos son obligatorios.");
        } else {
            System.out.println("--- Nuevo Registro ---");
            System.out.println("Usuario: " + usuario);
            System.out.println("Email: " + email);
            System.out.println("Matrícula: " + matricula);
            System.out.println("Contraseña: (oculta)");
            System.out.println("----------------------");

            // Aquí iría la lógica para guardar el nuevo usuario en la base de datos
            // ...

            alPulsarBotonIrALogin();
        }
    }

    @FXML
    protected void alPulsarBotonIrALogin() {
        System.out.println("Navegando de vuelta a la pantalla de Login...");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/interfaz/reservadesalas/vista/VistaLogin.fxml"));
            Parent raizLogin = loader.load();

            Scene escenaActual = botonIrALogin.getScene();
            Scene escenaLogin = new Scene(raizLogin, escenaActual.getWidth(), escenaActual.getHeight());

            // Cargamos el archivo CSS específico de Login
            URL cssUrl = getClass().getResource("/interfaz/reservadesalas/css/EstilosLogin.css");
            if (cssUrl != null) {
                escenaLogin.getStylesheets().add(cssUrl.toExternalForm());
            } else {
                System.out.println("Advertencia: No se pudo encontrar 'EstilosLogin.css'");
            }

            Stage escenario = (Stage) escenaActual.getWindow();
            escenario.setScene(escenaLogin);
            escenario.setTitle("RoomReserve - Iniciar Sesión");

        } catch (IOException e) {
            System.err.println("Error al cargar la vista de login: " + e.getMessage());
            e.printStackTrace();
        }
    }
}