package interfaz.sara.Controladores;

import interfaz.sara.ConexionBD.ConexionBD;
import interfaz.sara.Utilidades.GestorNavegacion;
import interfaz.sara.Utilidades.SesionUsuario;
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
 * Controlador para la vista de login
 * Gestiona la autenticación de usuarios (soporta hash SHA-256 y texto plano para migración)
 */
public class ControladorLogin {

    @FXML
    private TextField campoUsuario;
    @FXML
    private PasswordField campoContrasena;
    @FXML
    private Button botonIniciarSesion;
    @FXML
    private Label mensajeError;

    @FXML
    private void initialize() {
        mensajeError.setVisible(false);
        mensajeError.setManaged(false);
        campoUsuario.setOnAction(e -> manejarIniciarSesion());
        campoContrasena.setOnAction(e -> manejarIniciarSesion());
    }

    @FXML
    private void manejarIniciarSesion() {
        ocultarMensajeError();
        
        String usuario = campoUsuario.getText().trim();
        String contrasena = campoContrasena.getText();
        
        if (!validarCampos(usuario, contrasena)) {
            return;
        }
        
        if (autenticarUsuario(usuario, contrasena)) {
            SesionUsuario sesion = SesionUsuario.obtenerInstancia();
            if (sesion.esAdmin()) {
                navegarAPantallaAdmin();
            } else {
                navegarAPantallaPrincipal();
            }
        } else {
            mostrarMensajeError("Usuario o contraseña incorrectos. Por favor, intente nuevamente.");
        }
    }

    private boolean validarCampos(String usuario, String contrasena) {
        if (usuario.isEmpty()) {
            mostrarMensajeError("Por favor, ingrese su nombre de usuario, correo o matrícula.");
            campoUsuario.requestFocus();
            return false;
        }
        
        if (usuario.length() < 3) {
            mostrarMensajeError("El campo de usuario debe tener al menos 3 caracteres.");
            campoUsuario.requestFocus();
            return false;
        }
        
        if (usuario.contains("@")) {
            if (!validarFormatoEmail(usuario)) {
                mostrarMensajeError("Por favor, ingrese un correo electrónico válido con el dominio @utez.edu.mx");
                campoUsuario.requestFocus();
                return false;
            }
            
            if (!usuario.toLowerCase().endsWith("@utez.edu.mx")) {
                mostrarMensajeError("El correo electrónico debe tener el dominio @utez.edu.mx");
                campoUsuario.requestFocus();
                return false;
            }
        }
        
        if (contrasena.isEmpty()) {
            mostrarMensajeError("Por favor, ingrese su contraseña.");
            campoContrasena.requestFocus();
            return false;
        }
        
        if (contrasena.length() < 8) {
            mostrarMensajeError("La contraseña debe tener al menos 8 caracteres.");
            campoContrasena.requestFocus();
            return false;
        }
        
        return true;
    }
    
    private boolean validarFormatoEmail(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@utez\\.edu\\.mx$");
    }
    /**
     * Autentica al usuario verificando credenciales en la BD
     * Soporta contraseñas hasheadas (SHA-256) y texto plano (migración automática)
     */
    private boolean autenticarUsuario(String usuario, String contrasena) {
        ConexionBD conexionBD = ConexionBD.obtenerInstancia();
        
        String sql = "SELECT u.id, u.username, u.email, u.password_hash, u.is_active, " +
                    "COALESCE(SUM(CASE WHEN r.name = 'ADMIN' THEN 1 ELSE 0 END), 0) as es_admin " +
                    "FROM users u " +
                    "LEFT JOIN user_roles ur ON u.id = ur.user_id " +
                    "LEFT JOIN roles r ON ur.role_id = r.id " +
                    "WHERE (u.username = ? OR u.email = ? OR u.matricula = ?) " +
                    "AND u.is_active = 1 " +
                    "GROUP BY u.id, u.username, u.email, u.password_hash, u.is_active";
        
        try (Connection conexion = conexionBD.obtenerConexion();
             PreparedStatement statement = conexion.prepareStatement(sql)) {
            
            statement.setString(1, usuario);
            statement.setString(2, usuario);
            statement.setString(3, usuario);
            
            try (ResultSet resultado = statement.executeQuery()) {
                if (!resultado.next()) {
                    return false;
                }
                
                String passwordHash = resultado.getString("password_hash");
                Long userId = resultado.getLong("id");
                String username = resultado.getString("username");
                
                boolean contrasenaValida = false;
                boolean esHash = false;
                
                if (passwordHash != null && !passwordHash.isEmpty()) {
                    // Detecta si es hash SHA-256 (64 caracteres hex) o texto plano
                    if (passwordHash.length() == 64 && passwordHash.matches("^[a-f0-9]{64}$")) {
                        esHash = true;
                        contrasenaValida = interfaz.sara.Utilidades.PasswordHasher.verificarPassword(contrasena, passwordHash);
                    } else {
                        contrasenaValida = passwordHash.equals(contrasena);
                    }
                }
                
                if (!contrasenaValida) {
                    return false;
                }
                
                // Migración automática: si es texto plano, hashearlo
                if (!esHash) {
                    actualizarContrasenaAHash(userId, contrasena);
                }
                
                boolean esAdmin = resultado.getInt("es_admin") > 0;
                String email = resultado.getString("email");
                
                SesionUsuario sesion = SesionUsuario.obtenerInstancia();
                sesion.iniciarSesion(userId, username, email, esAdmin);
                
                return true;
            }
            
        } catch (SQLException e) {
            System.err.println("Error al autenticar usuario: " + e.getMessage());
            mostrarMensajeError("Error de conexión a la base de datos. Por favor, intente más tarde.");
            return false;
        }
    }

    /**
     * Migra contraseña de texto plano a hash SHA-256 automáticamente
     */
    private void actualizarContrasenaAHash(Long userId, String contrasenaPlano) {
        String hashContrasena = interfaz.sara.Utilidades.PasswordHasher.hashPassword(contrasenaPlano);
        if (hashContrasena == null) {
            System.err.println("Error al generar hash para usuario ID: " + userId);
            return;
        }
        
        String sql = "UPDATE users SET password_hash = ? WHERE id = ?";
        ConexionBD conexionBD = ConexionBD.obtenerInstancia();
        
        try (Connection conexion = conexionBD.obtenerConexion();
             PreparedStatement statement = conexion.prepareStatement(sql)) {
            
            statement.setString(1, hashContrasena);
            statement.setLong(2, userId);
            statement.executeUpdate();
            
        } catch (SQLException e) {
            System.err.println("Error al actualizar contraseña a hash: " + e.getMessage());
        }
    }
    
    private void navegarAPantallaPrincipal() {
        interfaz.sara.Utilidades.GestorNavegacionUsuario gestor = 
            interfaz.sara.Utilidades.GestorNavegacionUsuario.obtenerInstancia();
        gestor.navegarAVistaPrincipalUsuario();
    }
    
    private void navegarAPantallaAdmin() {
        interfaz.sara.Utilidades.GestorNavegacionAdmin gestor = 
            interfaz.sara.Utilidades.GestorNavegacionAdmin.obtenerInstancia();
        gestor.navegarAVistaUsuariosAdmin();
    }
    
    @FXML
    private void manejarRegistro() {
        GestorNavegacion.obtenerInstancia().navegarARegistro();
    }
    
    @FXML
    private void manejarRecuperarContrasena() {
        GestorNavegacion.obtenerInstancia().navegarAVistaRecuperarContrasena();
    }

    private void mostrarMensajeError(String mensaje) {
        mensajeError.setText(mensaje);
        mensajeError.setVisible(true);
        mensajeError.setManaged(true);
        mensajeError.setTextFill(Color.RED);
    }

    private void ocultarMensajeError() {
        mensajeError.setVisible(false);
        mensajeError.setManaged(false);
        mensajeError.setText("");
    }

    public void limpiarFormulario() {
        campoUsuario.clear();
        campoContrasena.clear();
        ocultarMensajeError();
    }
}

