package interfaz.sara.Controladores;

import interfaz.sara.ConexionBD.ConexionBD;
import interfaz.sara.Utilidades.GestorNavegacion;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Controlador para la vista de registro (VistaRegistro.fxml)
 * Maneja el registro de nuevos usuarios en el sistema SARA
 */
public class ControladorRegistro {

    // ========== Componentes FXML ==========
    
    @FXML
    private TextField campoUsuario;

    @FXML
    private TextField campoEmail;

    @FXML
    private TextField campoMatricula;

    @FXML
    private PasswordField campoContrasena;

    @FXML
    private Button botonRegistrarse;

    @FXML
    private Label mensajeError;

    // ========== Métodos de inicialización ==========
    
    /**
     * Inicializa el controlador después de que se carga el FXML
     * Configura los valores iniciales y prepara los componentes
     */
    @FXML
    private void initialize() {
        // Configurar el mensaje de error inicialmente oculto
        mensajeError.setVisible(false);
        mensajeError.setManaged(false);
        
        // Permitir que el botón se active presionando Enter en los campos
        campoUsuario.setOnAction(e -> manejarRegistro());
        campoEmail.setOnAction(e -> manejarRegistro());
        campoMatricula.setOnAction(e -> manejarRegistro());
        campoContrasena.setOnAction(e -> manejarRegistro());
    }

    // ========== Métodos de manejo de eventos ==========
    
    /**
     * Maneja el evento de clic en el botón "Registrarse"
     * Valida los datos y registra al nuevo usuario
     */
    @FXML
    private void manejarRegistro() {
        // Ocultar mensajes de error previos
        ocultarMensajeError();
        
        // Obtener los valores de los campos
        String usuario = campoUsuario.getText().trim();
        String email = campoEmail.getText().trim();
        String matricula = campoMatricula.getText().trim();
        String contrasena = campoContrasena.getText();
        
        // Validar que todos los campos estén completos
        if (!validarCampos(usuario, email, matricula, contrasena)) {
            return;
        }
        
        // Validar formato del email
        if (!validarFormatoEmail(email)) {
            mostrarMensajeError("Por favor, ingrese un correo electrónico válido.");
            campoEmail.requestFocus();
            return;
        }
        
        // Intentar registrar al usuario
        if (registrarUsuario(usuario, email, matricula, contrasena)) {
            // Registro exitoso - navegar al login después de un breve retraso
            mostrarMensajeExito("¡Registro exitoso! Redirigiendo al inicio de sesión...");
            
            // Navegar al login después de 1 segundo
            new Thread(() -> {
                try {
                    Thread.sleep(1000);
                    Platform.runLater(() -> navegarALogin());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        } else {
            // Error en el registro
            mostrarMensajeError("Error al registrar usuario. Por favor, verifique los datos e intente nuevamente.");
        }
    }
    
    /**
     * Maneja el evento de clic en el enlace "Inicia sesión"
     * Navega de vuelta a la pantalla de login
     */
    @FXML
    private void manejarIrALogin() {
        navegarALogin();
    }

    // ========== Métodos de validación ==========
    
    /**
     * Valida que todos los campos requeridos estén completos
     * 
     * @param usuario El nombre de usuario ingresado
     * @param email El correo electrónico ingresado
     * @param matricula La matrícula ingresada
     * @param contrasena La contraseña ingresada
     * @return true si todos los campos son válidos, false en caso contrario
     */
    private boolean validarCampos(String usuario, String email, String matricula, String contrasena) {
        if (usuario.isEmpty()) {
            mostrarMensajeError("Por favor, ingrese su nombre de usuario.");
            campoUsuario.requestFocus();
            return false;
        }
        
        if (email.isEmpty()) {
            mostrarMensajeError("Por favor, ingrese su correo institucional.");
            campoEmail.requestFocus();
            return false;
        }
        
        if (matricula.isEmpty()) {
            mostrarMensajeError("Por favor, ingrese su matrícula.");
            campoMatricula.requestFocus();
            return false;
        }
        
        if (contrasena.isEmpty()) {
            mostrarMensajeError("Por favor, ingrese su contraseña.");
            campoContrasena.requestFocus();
            return false;
        }
        
        // Validar longitud mínima de contraseña
        if (contrasena.length() < 6) {
            mostrarMensajeError("La contraseña debe tener al menos 6 caracteres.");
            campoContrasena.requestFocus();
            return false;
        }
        
        return true;
    }
    
    /**
     * Valida el formato del correo electrónico
     * 
     * @param email El correo electrónico a validar
     * @return true si el formato es válido, false en caso contrario
     */
    private boolean validarFormatoEmail(String email) {
        // Expresión regular básica para validar email
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        return email.matches(emailRegex);
    }

    // ========== Métodos de registro ==========
    
    /**
     * Registra un nuevo usuario en la base de datos
     * 
     * @param usuario El nombre de usuario
     * @param email El correo electrónico
     * @param matricula La matrícula
     * @param contrasena La contraseña en texto plano
     * @return true si el registro es exitoso, false en caso contrario
     */
    private boolean registrarUsuario(String usuario, String email, String matricula, String contrasena) {
        ConexionBD conexionBD = ConexionBD.obtenerInstancia();
        Connection conexion = null;
        PreparedStatement statement = null;
        ResultSet resultado = null;
        
        try {
            // Obtener conexión a la base de datos
            conexion = conexionBD.obtenerConexion();
            
            // Verificar si el usuario, email o matrícula ya existen
            if (existeUsuario(conexion, usuario, email, matricula)) {
                mostrarMensajeError("El usuario, correo electrónico o matrícula ya están registrados.");
                return false;
            }
            
            // Consulta SQL para insertar el nuevo usuario
            // NOTA: En producción, la contraseña debe ser hasheada con BCrypt o similar
            String sql = "INSERT INTO users (username, email, matricula, password_hash, is_active) " +
                        "VALUES (?, ?, ?, ?, 1)";
            
            statement = conexion.prepareStatement(sql);
            statement.setString(1, usuario);
            statement.setString(2, email);
            statement.setString(3, matricula);
            statement.setString(4, contrasena); // TODO: Hash de contraseña antes de guardar
            
            int filasAfectadas = statement.executeUpdate();
            
            if (filasAfectadas > 0) {
                System.out.println("Usuario registrado exitosamente: " + usuario);
                return true;
            } else {
                System.out.println("Error: No se pudo insertar el usuario");
                return false;
            }
            
        } catch (SQLException e) {
            System.err.println("Error al registrar usuario en la base de datos");
            System.err.println("Mensaje: " + e.getMessage());
            
            // Manejar errores específicos de base de datos
            if (e.getMessage().contains("Duplicate entry")) {
                mostrarMensajeError("El usuario, correo electrónico o matrícula ya están registrados.");
            } else {
                mostrarMensajeError("Error de conexión a la base de datos. Por favor, intente más tarde.");
            }
            
            e.printStackTrace();
            return false;
            
        } finally {
            // Cerrar recursos
            try {
                if (resultado != null) resultado.close();
                if (statement != null) statement.close();
                // No cerramos la conexión aquí, la reutilizamos (singleton)
            } catch (SQLException e) {
                System.err.println("Error al cerrar recursos de base de datos");
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Verifica si un usuario, email o matrícula ya existen en la base de datos
     * 
     * @param conexion La conexión a la base de datos
     * @param usuario El nombre de usuario a verificar
     * @param email El correo electrónico a verificar
     * @param matricula La matrícula a verificar
     * @return true si existe alguno de los valores, false en caso contrario
     */
    private boolean existeUsuario(Connection conexion, String usuario, String email, String matricula) throws SQLException {
        String sql = "SELECT COUNT(*) as count FROM users " +
                    "WHERE username = ? OR email = ? OR matricula = ?";
        
        PreparedStatement statement = conexion.prepareStatement(sql);
        statement.setString(1, usuario);
        statement.setString(2, email);
        statement.setString(3, matricula);
        
        ResultSet resultado = statement.executeQuery();
        
        if (resultado.next()) {
            int count = resultado.getInt("count");
            resultado.close();
            statement.close();
            return count > 0;
        }
        
        resultado.close();
        statement.close();
        return false;
    }

    // ========== Métodos de navegación ==========
    
    /**
     * Navega a la pantalla de login
     */
    private void navegarALogin() {
        GestorNavegacion gestorNavegacion = GestorNavegacion.obtenerInstancia();
        gestorNavegacion.navegarALogin();
    }

    // ========== Métodos de interfaz de usuario ==========
    
    /**
     * Muestra un mensaje de error al usuario
     * 
     * @param mensaje El mensaje de error a mostrar
     */
    private void mostrarMensajeError(String mensaje) {
        mensajeError.setText(mensaje);
        mensajeError.setVisible(true);
        mensajeError.setManaged(true);
        mensajeError.setTextFill(Color.RED);
    }
    
    /**
     * Muestra un mensaje de éxito al usuario
     * 
     * @param mensaje El mensaje de éxito a mostrar
     */
    private void mostrarMensajeExito(String mensaje) {
        mensajeError.setText(mensaje);
        mensajeError.setVisible(true);
        mensajeError.setManaged(true);
        mensajeError.setTextFill(Color.GREEN);
    }

    /**
     * Oculta el mensaje de error/éxito
     */
    private void ocultarMensajeError() {
        mensajeError.setVisible(false);
        mensajeError.setManaged(false);
        mensajeError.setText("");
    }

    // ========== Métodos públicos auxiliares (si se necesitan) ==========
    
    /**
     * Limpia todos los campos del formulario
     */
    public void limpiarFormulario() {
        campoUsuario.clear();
        campoEmail.clear();
        campoMatricula.clear();
        campoContrasena.clear();
        ocultarMensajeError();
    }
}

