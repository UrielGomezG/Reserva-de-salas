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
 * Controlador para el registro de nuevos usuarios
 * Valida datos y crea cuentas con contraseñas hasheadas
 */
public class ControladorRegistro {

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

    @FXML
    private void initialize() {
        mensajeError.setVisible(false);
        mensajeError.setManaged(false);
        campoUsuario.setOnAction(e -> manejarRegistro());
        campoEmail.setOnAction(e -> manejarRegistro());
        campoMatricula.setOnAction(e -> manejarRegistro());
        campoContrasena.setOnAction(e -> manejarRegistro());
    }

    @FXML
    private void manejarRegistro() {
        ocultarMensajeError();
        
        String usuario = campoUsuario.getText().trim();
        String email = campoEmail.getText().trim();
        String matricula = campoMatricula.getText().trim();
        String contrasena = campoContrasena.getText();
        
        if (!validarCampos(usuario, email, matricula, contrasena)) {
            return;
        }
        
        if (!validarFormatoEmail(email)) {
            mostrarMensajeError("Por favor, ingrese un correo electrónico válido.");
            campoEmail.requestFocus();
            return;
        }
        
        if (registrarUsuario(usuario, email, matricula, contrasena)) {
            mostrarMensajeExito("¡Registro exitoso! Redirigiendo al inicio de sesión...");
            new Thread(() -> {
                try {
                    Thread.sleep(1000);
                    Platform.runLater(() -> navegarALogin());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
        } else {
            mostrarMensajeError("Error al registrar usuario. Por favor, verifique los datos e intente nuevamente.");
        }
    }
    
    @FXML
    private void manejarIrALogin() {
        navegarALogin();
    }

    private boolean validarCampos(String usuario, String email, String matricula, String contrasena) {
        if (usuario.isEmpty()) {
            mostrarMensajeError("Por favor, ingrese su nombre de usuario.");
            campoUsuario.requestFocus();
            return false;
        }
        
        if (usuario.length() < 3 || usuario.length() > 50) {
            mostrarMensajeError("El nombre de usuario debe tener entre 3 y 50 caracteres.");
            campoUsuario.requestFocus();
            return false;
        }
        
        if (!usuario.matches("^[a-zA-Z0-9_]+$")) {
            mostrarMensajeError("El nombre de usuario solo puede contener letras, números y guiones bajos.");
            campoUsuario.requestFocus();
            return false;
        }
        
        if (email.isEmpty()) {
            mostrarMensajeError("Por favor, ingrese su correo institucional.");
            campoEmail.requestFocus();
            return false;
        }
        
        if (email.length() > 100) {
            mostrarMensajeError("El correo electrónico no puede exceder 100 caracteres.");
            campoEmail.requestFocus();
            return false;
        }
        
        if (!validarFormatoEmail(email)) {
            mostrarMensajeError("Por favor, ingrese un correo electrónico válido con el dominio @utez.edu.mx");
            campoEmail.requestFocus();
            return false;
        }
        
        if (!email.toLowerCase().endsWith("@utez.edu.mx")) {
            mostrarMensajeError("El correo electrónico debe tener el dominio @utez.edu.mx");
            campoEmail.requestFocus();
            return false;
        }
        
        if (matricula.isEmpty()) {
            mostrarMensajeError("Por favor, ingrese su matrícula.");
            campoMatricula.requestFocus();
            return false;
        }
        
        if (matricula.length() < 5 || matricula.length() > 20) {
            mostrarMensajeError("La matrícula debe tener entre 5 y 20 caracteres.");
            campoMatricula.requestFocus();
            return false;
        }
        
        if (contrasena.isEmpty()) {
            mostrarMensajeError("Por favor, ingrese su contraseña.");
            campoContrasena.requestFocus();
            return false;
        }
        
        if (contrasena.length() < 8 || contrasena.length() > 100) {
            mostrarMensajeError("La contraseña debe tener entre 8 y 100 caracteres.");
            campoContrasena.requestFocus();
            return false;
        }
        
        if (!validarContrasenaSegura(contrasena)) {
            mostrarMensajeError("La contraseña debe contener al menos un carácter especial (ej: !@#$%^&*()_+-=[]{}|;:,.<>?).");
            campoContrasena.requestFocus();
            return false;
        }
        
        return true;
    }
    
    private boolean validarFormatoEmail(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@utez\\.edu\\.mx$");
    }
    
    private boolean validarContrasenaSegura(String contrasena) {
        String caracteresEspeciales = "!@#$%^&*()_+-=[]{}|;:,.<>?";
        for (char c : contrasena.toCharArray()) {
            if (caracteresEspeciales.indexOf(c) >= 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * Registra un nuevo usuario hasheando la contraseña antes de guardarla
     */
    private boolean registrarUsuario(String usuario, String email, String matricula, String contrasena) {
        ConexionBD conexionBD = ConexionBD.obtenerInstancia();
        
        try (Connection conexion = conexionBD.obtenerConexion()) {
            if (existeUsuario(conexion, usuario, email, matricula)) {
                mostrarMensajeError("El usuario, correo electrónico o matrícula ya están registrados.");
                return false;
            }
            
            String hashContrasena = interfaz.sara.Utilidades.PasswordHasher.hashPassword(contrasena);
            if (hashContrasena == null || hashContrasena.isEmpty() || 
                hashContrasena.length() != 64 || !hashContrasena.matches("^[a-f0-9]{64}$")) {
                mostrarMensajeError("Error al procesar la contraseña. Por favor, intente nuevamente.");
                return false;
            }
            
            String sql = "INSERT INTO users (username, email, matricula, password_hash, is_active) " +
                        "VALUES (?, ?, ?, ?, 1)";
            
            try (PreparedStatement statement = conexion.prepareStatement(sql)) {
                statement.setString(1, usuario);
                statement.setString(2, email);
                statement.setString(3, matricula);
                statement.setString(4, hashContrasena);
                
                return statement.executeUpdate() > 0;
            }
            
        } catch (SQLException e) {
            System.err.println("Error al registrar usuario: " + e.getMessage());
            if (e.getMessage().contains("Duplicate entry")) {
                mostrarMensajeError("El usuario, correo electrónico o matrícula ya están registrados.");
            } else {
                mostrarMensajeError("Error de conexión a la base de datos. Por favor, intente más tarde.");
            }
            return false;
        }
    }
    
    private boolean existeUsuario(Connection conexion, String usuario, String email, String matricula) throws SQLException {
        String sql = "SELECT COUNT(*) as count FROM users WHERE username = ? OR email = ? OR matricula = ?";
        
        try (PreparedStatement statement = conexion.prepareStatement(sql)) {
            statement.setString(1, usuario);
            statement.setString(2, email);
            statement.setString(3, matricula);
            
            try (ResultSet resultado = statement.executeQuery()) {
                return resultado.next() && resultado.getInt("count") > 0;
            }
        }
    }

    private void navegarALogin() {
        GestorNavegacion.obtenerInstancia().navegarALogin();
    }

    private void mostrarMensajeError(String mensaje) {
        mensajeError.setText(mensaje);
        mensajeError.setVisible(true);
        mensajeError.setManaged(true);
        mensajeError.setTextFill(Color.RED);
    }
    
    private void mostrarMensajeExito(String mensaje) {
        mensajeError.setText(mensaje);
        mensajeError.setVisible(true);
        mensajeError.setManaged(true);
        mensajeError.setTextFill(Color.GREEN);
    }

    private void ocultarMensajeError() {
        mensajeError.setVisible(false);
        mensajeError.setManaged(false);
        mensajeError.setText("");
    }

    public void limpiarFormulario() {
        campoUsuario.clear();
        campoEmail.clear();
        campoMatricula.clear();
        campoContrasena.clear();
        ocultarMensajeError();
    }
}
