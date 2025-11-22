package interfaz.reservadesalas.Controladores;

import java.io.IOException;
import java.net.URL;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class ControladorLogin {

    @FXML private TextField campoEmail;
    @FXML private PasswordField campoContrasena;
    @FXML private Button botonLogin;
    @FXML private Button botonRegistro;

    @FXML
    public void initialize() {
        System.out.println("Controlador de Login inicializado.");
    }

    @FXML
    protected void alPulsarBotonLogin() {
        
        String email = campoEmail.getText();
        String password = campoContrasena.getText();

        if (email.isEmpty() || password.isEmpty()) {
            System.out.println("Error: Email o contraseña están vacíos.");
        } else {
            boolean loginExitoso = true; 
            
            if (loginExitoso) {
                System.out.println("Login exitoso. Navegando a la pantalla de Inicio.");
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/interfaz/reservadesalas/Vista/VistaInicio.fxml"));
                    Parent raizInicio = loader.load(); 

                    Scene escenaActual = botonLogin.getScene();
                    
                    Scene escenaInicio = new Scene(raizInicio, escenaActual.getWidth(), escenaActual.getHeight());

                    URL cssUrl = getClass().getResource("/interfaz/reservadesalas/CSS/EstilosInicio.css");
                    if (cssUrl != null) {
                        escenaInicio.getStylesheets().add(cssUrl.toExternalForm());
                    } else {
                        System.out.println("Advertencia: No se pudo encontrar 'EstilosInicio.css'.");
                    }

                    Stage escenario = (Stage) escenaActual.getWindow();
                    escenario.setScene(escenaInicio);
                    escenario.setTitle("Starsoft - Mis Reservas");

                } catch (IOException e) {
                    System.err.println("Error al cargar la vista de inicio (VistaInicio.fxml): " + e.getMessage());
                    e.printStackTrace();
                }
            } else {
                 System.out.println("Error de credenciales. Intente de nuevo.");
            }
        }
    }
    
    @FXML
    protected void alPulsarBotonRegistro() {
        System.out.println("Navegando a la pantalla de Registro...");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/interfaz/reservadesalas/Vista/VistaRegistro.fxml"));
            Parent raizRegistro = loader.load();

            Scene escenaActual = botonRegistro.getScene();
            Scene escenaRegistro = new Scene(raizRegistro, escenaActual.getWidth(), escenaActual.getHeight());

            URL cssUrl = getClass().getResource("/interfaz/reservadesalas/CSS/EstilosRegistro.css");
            if (cssUrl != null) {
                escenaRegistro.getStylesheets().add(cssUrl.toExternalForm());
            } else {
                System.out.println("Advertencia: No se pudo encontrar 'EstilosRegistro.css'");
            }

            Stage escenario = (Stage) escenaActual.getWindow();
            escenario.setScene(escenaRegistro);
            escenario.setTitle("RoomReserve - Crear Cuenta");

        } catch (IOException e) {
            System.err.println("Error al cargar la vista de registro: " + e.getMessage());
            e.printStackTrace();
        }
    }
}