package interfaz.sara.Controladores;

import interfaz.sara.ConexionBD.ConexionBD;
import interfaz.sara.Utilidades.GestorNavegacion;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.paint.Color;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Controlador para la vista de cambiar contraseña (VistaCambiarContrasena.fxml)
 * Maneja el cambio de contraseña después de validar el código de recuperación
 */
public class ControladorCambiarContrasena {

    // ========== Componentes FXML ==========
    
    @FXML
    private PasswordField campoNuevaContrasena;

    @FXML
    private PasswordField campoConfirmarContrasena;

    @FXML
    private Button botonCambiarContrasena;

    @FXML
    private Label mensajeError;

    // ========== Variables de estado ==========
    
    /** Correo del usuario que está cambiando la contraseña */
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
        
        // Permitir que el botón se active presionando Enter
        campoNuevaContrasena.setOnAction(e -> campoConfirmarContrasena.requestFocus());
        campoConfirmarContrasena.setOnAction(e -> manejarCambiarContrasena());
    }

    // ========== Métodos de manejo de eventos ==========
    
    /**
     * Maneja el evento de clic en el botón "Cambiar Contraseña"
     * Valida y actualiza la contraseña en la base de datos
     */
    @FXML
    private void manejarCambiarContrasena() {
        // Ocultar mensajes previos
        ocultarMensaje();
        
        // Obtener las contraseñas ingresadas
        String nuevaContrasena = campoNuevaContrasena.getText();
        String confirmarContrasena = campoConfirmarContrasena.getText();
        
        // Validar campos
        if (!validarCampos(nuevaContrasena, confirmarContrasena)) {
            return;
        }
        
        // Verificar que tenemos el correo
        if (correoUsuario == null || correoUsuario.isEmpty()) {
            mostrarMensaje("Error: No se encontró el correo del usuario. Por favor, vuelve a solicitar el código.", true);
            navegarARecuperarContrasena();
            return;
        }
        
        // Actualizar la contraseña en la base de datos
        try {
            if (actualizarContrasenaEnBD(correoUsuario, nuevaContrasena)) {
                mostrarMensaje("¡Contraseña actualizada exitosamente! Redirigiendo al login...", false);
                
                // Navegar al login después de un breve delay
                javafx.application.Platform.runLater(() -> {
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    navegarALogin();
                });
            } else {
                mostrarMensaje("Error al actualizar la contraseña. Por favor, intente más tarde.", true);
            }
            
        } catch (SQLException e) {
            System.err.println("Error al actualizar contraseña: " + e.getMessage());
            mostrarMensaje("Error al actualizar la contraseña. Por favor, intente más tarde.", true);
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
     * Valida que los campos de contraseña sean válidos
     * 
     * @param nuevaContrasena La nueva contraseña
     * @param confirmarContrasena La confirmación de la contraseña
     * @return true si los campos son válidos, false en caso contrario
     */
    private boolean validarCampos(String nuevaContrasena, String confirmarContrasena) {
        if (nuevaContrasena.isEmpty()) {
            mostrarMensaje("Por favor, ingrese su nueva contraseña.", true);
            campoNuevaContrasena.requestFocus();
            return false;
        }
        
        if (nuevaContrasena.length() < 8) {
            mostrarMensaje("La contraseña debe tener al menos 8 caracteres.", true);
            campoNuevaContrasena.requestFocus();
            return false;
        }
        
        if (nuevaContrasena.length() > 100) {
            mostrarMensaje("La contraseña no puede exceder 100 caracteres.", true);
            campoNuevaContrasena.requestFocus();
            return false;
        }
        
        // Validar que la contraseña contenga al menos un carácter especial
        if (!validarContrasenaSegura(nuevaContrasena)) {
            mostrarMensaje("La contraseña debe contener al menos un carácter especial (ej: !@#$%^&*()_+-=[]{}|;:,.<>?).", true);
            campoNuevaContrasena.requestFocus();
            return false;
        }
        
        if (confirmarContrasena.isEmpty()) {
            mostrarMensaje("Por favor, confirme su nueva contraseña.", true);
            campoConfirmarContrasena.requestFocus();
            return false;
        }
        
        if (!nuevaContrasena.equals(confirmarContrasena)) {
            mostrarMensaje("Las contraseñas no coinciden. Por favor, verifique e intente nuevamente.", true);
            campoConfirmarContrasena.requestFocus();
            return false;
        }
        
        return true;
    }
    
    /**
     * Valida que la contraseña sea segura
     * Debe tener al menos 8 caracteres y contener al menos un carácter especial
     * 
     * @param contrasena La contraseña a validar
     * @return true si la contraseña es segura, false en caso contrario
     */
    private boolean validarContrasenaSegura(String contrasena) {
        // Verificar que tenga al menos un carácter especial
        String caracteresEspeciales = "!@#$%^&*()_+-=[]{}|;:,.<>?";
        boolean tieneCaracterEspecial = false;
        
        for (char c : contrasena.toCharArray()) {
            if (caracteresEspeciales.indexOf(c) >= 0) {
                tieneCaracterEspecial = true;
                break;
            }
        }
        
        return tieneCaracterEspecial;
    }

    // ========== Métodos de base de datos ==========
    
    /**
     * Actualiza la contraseña del usuario en la base de datos
     * 
     * @param correo El correo del usuario
     * @param nuevaContrasena La nueva contraseña
     * @return true si la actualización fue exitosa, false en caso contrario
     * @throws SQLException Si hay un error al actualizar la base de datos
     */
    private boolean actualizarContrasenaEnBD(String correo, String nuevaContrasena) throws SQLException {
        ConexionBD conexionBD = ConexionBD.obtenerInstancia();
        Connection conexion = conexionBD.obtenerConexion();
        
        // Obtener el ID del usuario
        EmailRecoveryService servicio = EmailRecoveryService.obtenerInstancia();
        Long userId = servicio.obtenerUserId(correo);
        
        if (userId == null) {
            return false;
        }
        
        // Hash de la contraseña usando UTF-8
        String hashContrasena = interfaz.sara.Utilidades.PasswordHasher.hashPassword(nuevaContrasena);
        if (hashContrasena == null) {
            System.err.println("Error al generar hash de contraseña");
            return false;
        }
        
        // Actualizar la contraseña
        String sql = "UPDATE users SET password_hash = ? WHERE id = ?";
        
        try (PreparedStatement statement = conexion.prepareStatement(sql)) {
            statement.setString(1, hashContrasena);
            statement.setLong(2, userId);
            
            int filasAfectadas = statement.executeUpdate();
            return filasAfectadas > 0;
        }
    }

    // ========== Métodos de navegación ==========
    
    /**
     * Navega a la pantalla de login
     */
    private void navegarALogin() {
        GestorNavegacion gestorNavegacion = GestorNavegacion.obtenerInstancia();
        gestorNavegacion.navegarALogin();
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

