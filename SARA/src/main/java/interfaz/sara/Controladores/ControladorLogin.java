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
 * Controlador para la vista de login (VistaLogin.fxml)
 * Maneja la autenticación de usuarios en el sistema SARA
 */
public class ControladorLogin {

    // ========== Componentes FXML ==========
    
    @FXML
    private TextField campoUsuario;

    @FXML
    private PasswordField campoContrasena;

    @FXML
    private Button botonIniciarSesion;

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
        campoUsuario.setOnAction(e -> manejarIniciarSesion());
        campoContrasena.setOnAction(e -> manejarIniciarSesion());
    }

    // ========== Métodos de manejo de eventos ==========
    
    /**
     * Maneja el evento de clic en el botón "Iniciar Sesión"
     * Valida las credenciales y realiza la autenticación
     */
    @FXML
    private void manejarIniciarSesion() {
        // Ocultar mensajes de error previos
        ocultarMensajeError();
        
        // Obtener los valores de los campos
        String usuario = campoUsuario.getText().trim();
        String contrasena = campoContrasena.getText();
        
        // Validar que los campos no estén vacíos
        if (!validarCampos(usuario, contrasena)) {
            return;
        }
        
        // Intentar autenticar al usuario
        if (autenticarUsuario(usuario, contrasena)) {
            // Login exitoso - navegar según el rol del usuario
            SesionUsuario sesion = SesionUsuario.obtenerInstancia();
            if (sesion.esAdmin()) {
                navegarAPantallaAdmin();
            } else {
                navegarAPantallaPrincipal(usuario);
            }
        } else {
            // Credenciales incorrectas
            mostrarMensajeError("Usuario o contraseña incorrectos. Por favor, intente nuevamente.");
        }
    }

    // ========== Métodos de validación ==========
    
    /**
     * Valida que los campos de usuario y contraseña no estén vacíos
     * 
     * @param usuario El nombre de usuario ingresado
     * @param contrasena La contraseña ingresada
     * @return true si ambos campos son válidos, false en caso contrario
     */
    private boolean validarCampos(String usuario, String contrasena) {
        if (usuario.isEmpty()) {
            mostrarMensajeError("Por favor, ingrese su nombre de usuario.");
            campoUsuario.requestFocus();
            return false;
        }
        
        if (contrasena.isEmpty()) {
            mostrarMensajeError("Por favor, ingrese su contraseña.");
            campoContrasena.requestFocus();
            return false;
        }
        
        return true;
    }

    // ========== Métodos de autenticación ==========
    
    /**
     * Autentica al usuario con las credenciales proporcionadas
     * Consulta la base de datos para verificar el usuario y contraseña
     * 
     * @param usuario El nombre de usuario, email o matrícula
     * @param contrasena La contraseña en texto plano
     * @return true si la autenticación es exitosa, false en caso contrario
     */
    private boolean autenticarUsuario(String usuario, String contrasena) {
        ConexionBD conexionBD = ConexionBD.obtenerInstancia();
        Connection conexion = null;
        PreparedStatement statement = null;
        ResultSet resultado = null;
        
        try {
            // Obtener conexión a la base de datos
            conexion = conexionBD.obtenerConexion();
            
            // Consulta SQL para buscar el usuario por username, email o matrícula
            // y verificar que esté activo, incluyendo verificación de roles
            String sql = "SELECT u.id, u.username, u.email, u.password_hash, u.is_active, " +
                        "COALESCE(SUM(CASE WHEN r.name = 'ADMIN' THEN 1 ELSE 0 END), 0) as es_admin " +
                        "FROM users u " +
                        "LEFT JOIN user_roles ur ON u.id = ur.user_id " +
                        "LEFT JOIN roles r ON ur.role_id = r.id " +
                        "WHERE (u.username = ? OR u.email = ? OR u.matricula = ?) " +
                        "AND u.is_active = 1 " +
                        "GROUP BY u.id, u.username, u.email, u.password_hash, u.is_active";
            
            statement = conexion.prepareStatement(sql);
            statement.setString(1, usuario);
            statement.setString(2, usuario);
            statement.setString(3, usuario);
            
            resultado = statement.executeQuery();
            
            // Verificar si se encontró el usuario
            if (resultado.next()) {
                String passwordHash = resultado.getString("password_hash");
                Long userId = resultado.getLong("id");
                String username = resultado.getString("username");
                
                // Validar la contraseña
                // NOTA: En producción, deberías usar un algoritmo de hash seguro (BCrypt, Argon2, etc.)
                // Por ahora, comparación directa según los datos de ejemplo en la BD
                if (passwordHash != null && passwordHash.equals(contrasena)) {
                    // Verificar si el usuario es administrador
                    boolean esAdmin = resultado.getInt("es_admin") > 0;
                    
                    // Guardar información del usuario en la sesión
                    String email = resultado.getString("email");
                    SesionUsuario sesion = SesionUsuario.obtenerInstancia();
                    sesion.iniciarSesion(userId, username, email, esAdmin);
                    
                    System.out.println("Autenticación exitosa para usuario ID: " + userId + 
                                     ", Username: " + username + ", Admin: " + esAdmin);
                    return true;
                } else {
                    System.out.println("Contraseña incorrecta para usuario: " + usuario);
                    return false;
                }
            } else {
                System.out.println("Usuario no encontrado: " + usuario);
                return false;
            }
            
        } catch (SQLException e) {
            System.err.println("Error al autenticar usuario en la base de datos");
            System.err.println("Mensaje: " + e.getMessage());
            mostrarMensajeError("Error de conexión a la base de datos. Por favor, intente más tarde.");
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

    // ========== Métodos de navegación ==========
    
    /**
     * Navega a la pantalla principal después de un login exitoso
     * 
     * @param usuario El nombre del usuario autenticado
     */
    private void navegarAPantallaPrincipal(String usuario) {
        System.out.println("Login exitoso para usuario: " + usuario);
        GestorNavegacion gestorNavegacion = GestorNavegacion.obtenerInstancia();
        gestorNavegacion.navegarAVistaPrincipalUsuario();
    }
    
    /**
     * Navega a la pantalla de administrador después de un login exitoso de admin
     */
    private void navegarAPantallaAdmin() {
        System.out.println("Login exitoso para administrador");
        GestorNavegacion gestorNavegacion = GestorNavegacion.obtenerInstancia();
        gestorNavegacion.navegarAVistaUsuariosAdmin();
    }
    
    /**
     * Maneja el evento de clic en el enlace "Regístrate"
     * Navega a la pantalla de registro
     */
    @FXML
    private void manejarRegistro() {
        GestorNavegacion gestorNavegacion = GestorNavegacion.obtenerInstancia();
        gestorNavegacion.navegarARegistro();
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
     * Oculta el mensaje de error
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
        campoContrasena.clear();
        ocultarMensajeError();
    }
}

