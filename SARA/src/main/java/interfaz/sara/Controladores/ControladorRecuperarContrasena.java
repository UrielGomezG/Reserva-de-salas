package interfaz.sara.Controladores;

import interfaz.sara.Utilidades.GestorNavegacion;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;

import jakarta.mail.MessagingException;
import java.sql.SQLException;

/**
 * Controlador para la vista de recuperar contraseña (VistaRecuperarContrasena.fxml)
 * Maneja el envío del código de recuperación por correo electrónico
 */
public class ControladorRecuperarContrasena {

    // ========== Componentes FXML ==========
    
    @FXML
    private TextField campoCorreo;

    @FXML
    private Button botonEnviarCodigo;

    @FXML
    private Label mensajeEstado;

    // ========== Métodos de inicialización ==========
    
    /**
     * Inicializa el controlador después de que se carga el FXML
     */
    @FXML
    private void initialize() {
        // Configurar el mensaje de estado inicialmente oculto
        mensajeEstado.setVisible(false);
        mensajeEstado.setManaged(false);
        
        // Permitir que el botón se active presionando Enter
        campoCorreo.setOnAction(e -> manejarEnviarCodigo());
    }

    // ========== Métodos de manejo de eventos ==========
    
    /**
     * Maneja el evento de clic en el botón "Enviar Código"
     * Valida el correo y envía el código de recuperación
     */
    @FXML
    private void manejarEnviarCodigo() {
        // Ocultar mensajes previos
        ocultarMensaje();
        
        // Obtener el correo ingresado
        String correo = campoCorreo.getText().trim();
        
        // Validar que el campo no esté vacío
        if (correo.isEmpty()) {
            mostrarMensaje("Por favor, ingrese su correo electrónico.", true);
            campoCorreo.requestFocus();
            return;
        }
        
        // Validar formato de correo
        if (!validarFormatoEmail(correo)) {
            mostrarMensaje("Por favor, ingrese un correo electrónico válido con el dominio @utez.edu.mx", true);
            campoCorreo.requestFocus();
            return;
        }
        
        // Validar que el dominio sea @utez.edu.mx
        if (!correo.toLowerCase().endsWith("@utez.edu.mx")) {
            mostrarMensaje("El correo electrónico debe tener el dominio @utez.edu.mx", true);
            campoCorreo.requestFocus();
            return;
        }
        
        // Intentar enviar el código
        try {
            EmailRecoveryService servicio = EmailRecoveryService.obtenerInstancia();
            String codigo = servicio.enviarCodigoRecuperacion(correo);
            
            if (codigo != null) {
                // Código enviado exitosamente
                mostrarMensaje("Código de recuperación enviado a tu correo electrónico.", false);
                
                // Navegar a la vista de validar código después de un breve delay
                javafx.application.Platform.runLater(() -> {
                    try {
                        Thread.sleep(1500);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    navegarAValidarCodigo(correo);
                });
            } else {
                // El correo no existe en la base de datos
                mostrarMensaje("El correo electrónico ingresado no está registrado en el sistema.", true);
                campoCorreo.requestFocus();
            }
            
        } catch (MessagingException e) {
            System.err.println("Error al enviar correo: " + e.getMessage());
            
            // Detectar errores específicos de Gmail
            String mensajeError = e.getMessage();
            String mensajeUsuario;
            
            if (mensajeError != null && mensajeError.contains("Application-specific password required")) {
                mensajeUsuario = "Error de autenticación: Se requiere una contraseña de aplicación de Gmail.\n" +
                                "Por favor, contacte al administrador del sistema para configurar las credenciales correctas.";
            } else if (mensajeError != null && mensajeError.contains("Invalid credentials")) {
                mensajeUsuario = "Error de autenticación: Las credenciales del correo son incorrectas.\n" +
                                "Por favor, contacte al administrador del sistema.";
            } else if (mensajeError != null && mensajeError.contains("Could not connect to SMTP host")) {
                mensajeUsuario = "Error de conexión: No se pudo conectar al servidor de correo.\n" +
                                "Por favor, verifique su conexión a internet e intente más tarde.";
            } else {
                mensajeUsuario = "Error al enviar el código. Por favor, verifica la configuración del servidor de correo o intenta más tarde.";
            }
            
            mostrarMensaje(mensajeUsuario, true);
        } catch (SQLException e) {
            System.err.println("Error al consultar la base de datos: " + e.getMessage());
            mostrarMensaje("Error al verificar el correo. Por favor, intente más tarde.", true);
        }
    }
    
    /**
     * Maneja el evento de clic en el enlace "Iniciar sesión"
     * Navega de vuelta a la pantalla de login
     */
    @FXML
    private void manejarVolverALogin() {
        navegarALogin();
    }

    // ========== Métodos de validación ==========
    
    /**
     * Valida el formato del correo electrónico usando expresión regular
     * 
     * @param email El correo electrónico a validar
     * @return true si el formato es válido, false en caso contrario
     */
    private boolean validarFormatoEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@utez\\.edu\\.mx$";
        return email.matches(emailRegex);
    }

    // ========== Métodos de navegación ==========
    
    /**
     * Navega a la pantalla de validar código
     * 
     * @param correo El correo del usuario
     */
    private void navegarAValidarCodigo(String correo) {
        GestorNavegacion gestorNavegacion = GestorNavegacion.obtenerInstancia();
        gestorNavegacion.navegarAVistaValidarCodigo(correo);
    }
    
    /**
     * Navega a la pantalla de login
     */
    private void navegarALogin() {
        GestorNavegacion gestorNavegacion = GestorNavegacion.obtenerInstancia();
        gestorNavegacion.navegarALogin();
    }

    // ========== Métodos de interfaz de usuario ==========
    
    /**
     * Muestra un mensaje de estado al usuario
     * 
     * @param mensaje El mensaje a mostrar
     * @param esError true si es un mensaje de error, false si es de éxito
     */
    private void mostrarMensaje(String mensaje, boolean esError) {
        mensajeEstado.setText(mensaje);
        mensajeEstado.setVisible(true);
        mensajeEstado.setManaged(true);
        
        if (esError) {
            mensajeEstado.setTextFill(Color.RED);
        } else {
            mensajeEstado.setTextFill(Color.GREEN);
        }
    }

    /**
     * Oculta el mensaje de estado
     */
    private void ocultarMensaje() {
        mensajeEstado.setVisible(false);
        mensajeEstado.setManaged(false);
        mensajeEstado.setText("");
    }
}

