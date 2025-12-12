package interfaz.sara.Controladores;

import interfaz.sara.Utilidades.GestorNavegacion;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;

import java.sql.SQLException;

/**
 * Controlador para la vista de validar código (VistaValidarCodigo.fxml)
 * Maneja la validación del código de recuperación enviado por correo
 */
public class ControladorValidarCodigo {

    // ========== Componentes FXML ==========
    
    @FXML
    private TextField campoCodigo;

    @FXML
    private Button botonValidarCodigo;

    @FXML
    private Label mensajeError;

    @FXML
    private Label labelDescripcion;

    // ========== Variables de estado ==========
    
    /** Correo del usuario que está recuperando la contraseña */
    private String correoUsuario;

    // ========== Métodos de inicialización ==========
    
    /**
     * Inicializa el controlador después de que se carga el FXML
     */
    @FXML
    private void initialize() {
        // Configurar el mensaje de error inicialmente oculto
        mensajeError.setVisible(false);
        mensajeError.setManaged(false);
        
        // Obtener el correo del gestor de navegación
        GestorNavegacion gestorNavegacion = GestorNavegacion.obtenerInstancia();
        correoUsuario = gestorNavegacion.obtenerCorreoRecuperacion();
        
        // Actualizar la descripción con el correo
        if (correoUsuario != null && !correoUsuario.isEmpty()) {
            labelDescripcion.setText("Ingresa el código de 6 dígitos que enviamos a:\n" + correoUsuario);
        }
        
        // Limitar el campo a 6 dígitos
        campoCodigo.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && newValue.length() > 6) {
                campoCodigo.setText(newValue.substring(0, 6));
            }
            // Solo permitir números
            if (!newValue.matches("\\d*")) {
                campoCodigo.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });
        
        // Permitir que el botón se active presionando Enter
        campoCodigo.setOnAction(e -> manejarValidarCodigo());
    }

    // ========== Métodos de manejo de eventos ==========
    
    /**
     * Maneja el evento de clic en el botón "Validar Código"
     * Valida el código ingresado con el código enviado por correo
     */
    @FXML
    private void manejarValidarCodigo() {
        // Ocultar mensajes previos
        ocultarMensaje();
        
        // Obtener el código ingresado
        String codigo = campoCodigo.getText().trim();
        
        // Validar que el campo no esté vacío
        if (codigo.isEmpty()) {
            mostrarMensaje("Por favor, ingrese el código de verificación.", true);
            campoCodigo.requestFocus();
            return;
        }
        
        // Validar que el código tenga 6 dígitos
        if (codigo.length() != 6) {
            mostrarMensaje("El código debe tener 6 dígitos.", true);
            campoCodigo.requestFocus();
            return;
        }
        
        // Validar que solo contenga números
        if (!codigo.matches("\\d{6}")) {
            mostrarMensaje("El código solo debe contener números.", true);
            campoCodigo.requestFocus();
            return;
        }
        
        // Verificar que tenemos el correo
        if (correoUsuario == null || correoUsuario.isEmpty()) {
            mostrarMensaje("Error: No se encontró el correo del usuario. Por favor, vuelve a solicitar el código.", true);
            navegarARecuperarContrasena();
            return;
        }
        
        // Validar el código con la base de datos
        try {
            EmailRecoveryService servicio = EmailRecoveryService.obtenerInstancia();
            boolean codigoValido = servicio.validarCodigo(correoUsuario, codigo);
            
            if (codigoValido) {
                // Código válido - marcar como usado y navegar a cambiar contraseña
                servicio.marcarCodigoComoUsado(correoUsuario, codigo);
                mostrarMensaje("Código válido. Redirigiendo...", false);
                
                // Navegar a la vista de cambiar contraseña después de un breve delay
                javafx.application.Platform.runLater(() -> {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    navegarACambiarContrasena(correoUsuario);
                });
            } else {
                // Código inválido o expirado
                mostrarMensaje("El código ingresado es inválido o ha expirado. Por favor, solicita un nuevo código.", true);
                campoCodigo.clear();
                campoCodigo.requestFocus();
            }
            
        } catch (SQLException e) {
            System.err.println("Error al validar código: " + e.getMessage());
            mostrarMensaje("Error al validar el código. Por favor, intente más tarde.", true);
        }
    }
    
    /**
     * Maneja el evento de clic en el enlace "Volver"
     * Navega de vuelta a la pantalla de recuperar contraseña
     */
    @FXML
    private void manejarVolver() {
        navegarARecuperarContrasena();
    }

    // ========== Métodos de navegación ==========
    
    /**
     * Navega a la pantalla de cambiar contraseña
     * 
     * @param correo El correo del usuario
     */
    private void navegarACambiarContrasena(String correo) {
        GestorNavegacion gestorNavegacion = GestorNavegacion.obtenerInstancia();
        gestorNavegacion.navegarAVistaCambiarContrasena(correo);
    }
    
    /**
     * Navega a la pantalla de recuperar contraseña
     */
    private void navegarARecuperarContrasena() {
        GestorNavegacion gestorNavegacion = GestorNavegacion.obtenerInstancia();
        gestorNavegacion.navegarAVistaRecuperarContrasena();
    }

    // ========== Métodos de interfaz de usuario ==========
    
    /**
     * Muestra un mensaje de error al usuario
     * 
     * @param mensaje El mensaje de error a mostrar
     * @param esError true si es un mensaje de error, false si es de éxito
     */
    private void mostrarMensaje(String mensaje, boolean esError) {
        mensajeError.setText(mensaje);
        mensajeError.setVisible(true);
        mensajeError.setManaged(true);
        
        if (esError) {
            mensajeError.setTextFill(Color.RED);
        } else {
            mensajeError.setTextFill(Color.GREEN);
        }
    }

    /**
     * Oculta el mensaje de error
     */
    private void ocultarMensaje() {
        mensajeError.setVisible(false);
        mensajeError.setManaged(false);
        mensajeError.setText("");
    }
}

