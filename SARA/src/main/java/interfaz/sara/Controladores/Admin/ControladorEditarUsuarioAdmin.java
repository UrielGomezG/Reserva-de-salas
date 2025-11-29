package interfaz.sara.Controladores.Admin;

import interfaz.sara.ConexionBD.ConexionBD;
import interfaz.sara.Utilidades.GestorNavegacion;
import interfaz.sara.Utilidades.SesionUsuario;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Controlador para la vista de editar usuario del administrador (VistaEditarUsuarioAdmin.fxml)
 * Permite editar los datos del usuario, cambiar su rol, habilitar/deshabilitar y eliminar cuenta
 */
public class ControladorEditarUsuarioAdmin {

    // ========== Componentes FXML ==========
    
    @FXML
    private VBox sidebarInclude;
    
    @FXML
    private ImageView profileImageView;
    
    @FXML
    private Label labelNombreUsuario;
    
    @FXML
    private Label labelEmailUsuario;
    
    @FXML
    private TextField campoNombre;
    
    @FXML
    private TextField campoEmail;
    
    @FXML
    private TextField campoMatricula;
    
    @FXML
    private PasswordField campoContrasena;
    
    @FXML
    private ComboBox<String> comboRoles;
    
    @FXML
    private CheckBox checkCuentaActiva;

    // ========== Variables de estado ==========
    
    /** ID del usuario que se está editando */
    private Long usuarioId;
    
    /** Mapeo de nombres de roles a IDs */
    private Map<String, Long> mapaRoles;
    
    /** Rol actual del usuario */
    private Long rolIdActual;

    // ========== Métodos de inicialización ==========
    
    /**
     * Inicializa el controlador después de que se carga el FXML
     */
    @FXML
    private void initialize() {
        // Verificar que el usuario es admin
        SesionUsuario sesion = SesionUsuario.obtenerInstancia();
        if (!sesion.estaAutenticado() || !sesion.esAdmin()) {
            mostrarAlerta("Acceso Denegado", "No autorizado", 
                         "Solo los administradores pueden acceder a esta sección.", 
                         Alert.AlertType.ERROR);
            return;
        }
        
        // Obtener el ID del usuario seleccionado
        GestorNavegacion gestorNavegacion = GestorNavegacion.obtenerInstancia();
        usuarioId = gestorNavegacion.obtenerUsuarioIdSeleccionado();
        
        if (usuarioId == null) {
            mostrarAlerta("Error", "Usuario no seleccionado", 
                         "No se ha seleccionado un usuario para editar.", 
                         Alert.AlertType.ERROR);
            return;
        }
        
        // Inicializar mapeo de roles
        mapaRoles = new HashMap<>();
        
        // Cargar roles disponibles
        cargarRoles();
        
        // Cargar datos del usuario desde la base de datos
        cargarDatosUsuario();
        
        // Configurar el sidebar
        Platform.runLater(() -> {
            configurarSidebar();
        });
    }
    
    /**
     * Configura el sidebar para resaltar el botón de usuarios como activo
     */
    private void configurarSidebar() {
        try {
            if (sidebarInclude != null) {
                // Obtener botones directamente y aplicar estilos
                Button btnUsuarios = (Button) sidebarInclude.lookup("#botonUsuarios");
                Button btnSalas = (Button) sidebarInclude.lookup("#botonSalas");
                Button btnReservas = (Button) sidebarInclude.lookup("#botonReservas");
                Button btnReportes = (Button) sidebarInclude.lookup("#botonReportes");
                Button btnPerfil = (Button) sidebarInclude.lookup("#botonPerfil");
                Button btnNotificaciones = (Button) sidebarInclude.lookup("#botonNotificaciones");
                
                if (btnUsuarios != null) {
                    // Resetear todos
                    resetearBotonesSidebar(btnSalas, btnReservas, btnReportes, btnPerfil, btnNotificaciones);
                    
                    // Activar usuarios
                    btnUsuarios.getStyleClass().remove("sidebar-button");
                    if (!btnUsuarios.getStyleClass().contains("sidebar-button-active")) {
                        btnUsuarios.getStyleClass().add("sidebar-button-active");
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error al configurar sidebar: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Resetea los botones del sidebar al estado inactivo
     */
    private void resetearBotonesSidebar(Button... botones) {
        for (Button btn : botones) {
            if (btn != null) {
                btn.getStyleClass().remove("sidebar-button-active");
                if (!btn.getStyleClass().contains("sidebar-button")) {
                    btn.getStyleClass().add("sidebar-button");
                }
            }
        }
    }

    // ========== Métodos de carga de datos ==========
    
    /**
     * Carga los roles disponibles desde la base de datos
     */
    private void cargarRoles() {
        ConexionBD conexionBD = ConexionBD.obtenerInstancia();
        
        String sql = "SELECT id, name FROM roles ORDER BY name";
        
        try {
            Connection conexion = conexionBD.obtenerConexion();
            try (PreparedStatement statement = conexion.prepareStatement(sql);
                 ResultSet resultado = statement.executeQuery()) {
                
                ObservableList<String> nombresRoles = FXCollections.observableArrayList();
                
                while (resultado.next()) {
                    Long id = resultado.getLong("id");
                    String nombre = resultado.getString("name");
                    mapaRoles.put(nombre, id);
                    nombresRoles.add(nombre);
                }
                
                comboRoles.setItems(nombresRoles);
            }
        } catch (SQLException e) {
            System.err.println("Error al cargar roles: " + e.getMessage());
            mostrarAlerta("Error", "Error al cargar roles", 
                         "No se pudieron cargar los roles disponibles.", 
                         Alert.AlertType.ERROR);
        }
    }
    
    /**
     * Carga los datos del usuario desde la base de datos
     */
    private void cargarDatosUsuario() {
        ConexionBD conexionBD = ConexionBD.obtenerInstancia();
        
        String sql = "SELECT u.id, u.username, u.email, u.matricula, u.is_active, " +
                    "u.profile_picture_path, ur.role_id " +
                    "FROM users u " +
                    "LEFT JOIN user_roles ur ON u.id = ur.user_id " +
                    "WHERE u.id = ? " +
                    "LIMIT 1";
        
        try {
            Connection conexion = conexionBD.obtenerConexion();
            try (PreparedStatement statement = conexion.prepareStatement(sql)) {
                statement.setLong(1, usuarioId);
                
                try (ResultSet resultado = statement.executeQuery()) {
                    if (resultado.next()) {
                        // Cargar datos básicos
                        String username = resultado.getString("username");
                        String email = resultado.getString("email");
                        String matricula = resultado.getString("matricula");
                        boolean activo = resultado.getInt("is_active") == 1;
                        String rutaImagen = resultado.getString("profile_picture_path");
                        
                        // Obtener el rol actual
                        Long roleId = resultado.getLong("role_id");
                        if (roleId > 0) {
                            rolIdActual = roleId;
                        }
                        
                        // Actualizar campos del formulario
                        campoNombre.setText(username != null ? username : "");
                        campoEmail.setText(email != null ? email : "");
                        campoMatricula.setText(matricula != null ? matricula : "");
                        checkCuentaActiva.setSelected(activo);
                        
                        // Actualizar labels de información
                        labelNombreUsuario.setText(username != null ? username : "Sin nombre");
                        labelEmailUsuario.setText(email != null ? email : "Sin email");
                        
                        // Cargar imagen de perfil
                        cargarImagenPerfil(rutaImagen);
                        
                        // Cargar y seleccionar el rol actual
                        cargarRolActual();
                    } else {
                        mostrarAlerta("Error", "Usuario no encontrado", 
                                     "No se encontró el usuario en la base de datos.", 
                                     Alert.AlertType.ERROR);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al cargar datos del usuario: " + e.getMessage());
            mostrarAlerta("Error", "Error al cargar datos", 
                         "No se pudieron cargar los datos del usuario. Por favor, intente más tarde.", 
                         Alert.AlertType.ERROR);
        }
    }
    
    /**
     * Carga y selecciona el rol actual del usuario en el ComboBox
     */
    private void cargarRolActual() {
        if (rolIdActual == null) {
            return;
        }
        
        ConexionBD conexionBD = ConexionBD.obtenerInstancia();
        
        String sql = "SELECT name FROM roles WHERE id = ?";
        
        try {
            Connection conexion = conexionBD.obtenerConexion();
            try (PreparedStatement statement = conexion.prepareStatement(sql)) {
                statement.setLong(1, rolIdActual);
                
                try (ResultSet resultado = statement.executeQuery()) {
                    if (resultado.next()) {
                        String nombreRol = resultado.getString("name");
                        comboRoles.getSelectionModel().select(nombreRol);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al cargar rol actual: " + e.getMessage());
        }
    }
    
    /**
     * Carga la imagen de perfil desde la ruta especificada
     * 
     * @param rutaImagen Ruta de la imagen (puede ser null o vacía)
     */
    private void cargarImagenPerfil(String rutaImagen) {
        try {
            if (rutaImagen != null && !rutaImagen.trim().isEmpty()) {
                File archivoImagen = new File(rutaImagen);
                if (archivoImagen.exists()) {
                    Image imagen = new Image(new FileInputStream(archivoImagen));
                    profileImageView.setImage(imagen);
                    return;
                }
            }
            
            // Si no hay imagen o no se encontró, cargar imagen por defecto
            cargarImagenPorDefecto();
        } catch (FileNotFoundException e) {
            System.err.println("No se encontró la imagen de perfil: " + rutaImagen);
            cargarImagenPorDefecto();
        } catch (Exception e) {
            System.err.println("Error al cargar imagen de perfil: " + e.getMessage());
            cargarImagenPorDefecto();
        }
    }
    
    /**
     * Carga una imagen de perfil por defecto
     */
    private void cargarImagenPorDefecto() {
        try {
            InputStream imagenStream = getClass().getResourceAsStream(
                "/interfaz/sara/imagenes/perfil_placeholder.png"
            );
            if (imagenStream != null) {
                Image imagen = new Image(imagenStream);
                profileImageView.setImage(imagen);
            } else {
                profileImageView.setImage(null);
            }
        } catch (Exception e) {
            System.err.println("Error al cargar imagen por defecto: " + e.getMessage());
        }
    }

    // ========== Métodos de manejo de eventos ==========
    
    /**
     * Maneja el clic en el botón "Volver"
     * Navega de vuelta a la vista de detalle del usuario
     */
    @FXML
    private void manejarVolver() {
        GestorNavegacion gestorNavegacion = GestorNavegacion.obtenerInstancia();
        gestorNavegacion.navegarAVistaDetalleUsuarioAdmin(usuarioId);
    }
    
    /**
     * Maneja el clic en el botón "Guardar Cambios"
     * Valida y guarda los cambios en la base de datos
     */
    @FXML
    private void manejarGuardarCambios() {
        // Validar campos
        if (!validarCampos()) {
            return;
        }
        
        // Validar contraseña si se proporcionó
        String nuevaContrasena = campoContrasena.getText().trim();
        if (!nuevaContrasena.isEmpty() && nuevaContrasena.length() < 6) {
            mostrarAlerta("Error de validación", "Contraseña muy corta", 
                         "La contraseña debe tener al menos 6 caracteres.", 
                         Alert.AlertType.ERROR);
            return;
        }
        
        // Guardar cambios en la base de datos
        guardarCambiosEnBD(nuevaContrasena);
    }
    
    /**
     * Valida que los campos requeridos estén completos
     * 
     * @return true si todos los campos son válidos, false en caso contrario
     */
    private boolean validarCampos() {
        String nombre = campoNombre.getText().trim();
        String correo = campoEmail.getText().trim();
        String matricula = campoMatricula.getText().trim();
        
        if (nombre.isEmpty()) {
            mostrarAlerta("Error de validación", "Campo requerido", 
                         "El nombre es obligatorio. Por favor, ingrese el nombre del usuario.", 
                         Alert.AlertType.ERROR);
            campoNombre.requestFocus();
            return false;
        }
        
        if (correo.isEmpty()) {
            mostrarAlerta("Error de validación", "Campo requerido", 
                         "El correo electrónico es obligatorio. Por favor, ingrese el correo.", 
                         Alert.AlertType.ERROR);
            campoEmail.requestFocus();
            return false;
        }
        
        // Validar formato de correo básico
        if (!correo.contains("@") || !correo.contains(".")) {
            mostrarAlerta("Error de validación", "Correo inválido", 
                         "Por favor, ingrese un correo electrónico válido.", 
                         Alert.AlertType.ERROR);
            campoEmail.requestFocus();
            return false;
        }
        
        if (matricula.isEmpty()) {
            mostrarAlerta("Error de validación", "Campo requerido", 
                         "La matrícula es obligatoria. Por favor, ingrese la matrícula.", 
                         Alert.AlertType.ERROR);
            campoMatricula.requestFocus();
            return false;
        }
        
        return true;
    }
    
    /**
     * Guarda los cambios del usuario en la base de datos
     * 
     * @param nuevaContrasena Nueva contraseña (puede ser null o vacía si no se cambió)
     */
    private void guardarCambiosEnBD(String nuevaContrasena) {
        ConexionBD conexionBD = ConexionBD.obtenerInstancia();
        
        String nombre = campoNombre.getText().trim();
        String correo = campoEmail.getText().trim();
        String matricula = campoMatricula.getText().trim();
        boolean cuentaActiva = checkCuentaActiva.isSelected();
        String rolSeleccionado = comboRoles.getSelectionModel().getSelectedItem();
        
        Connection conexion = null;
        
        try {
            conexion = conexionBD.obtenerConexion();
            conexion.setAutoCommit(false); // Iniciar transacción
            
            // Actualizar datos del usuario
            String sqlUsuario;
            PreparedStatement statementUsuario = null;
            
            if (nuevaContrasena != null && !nuevaContrasena.isEmpty()) {
                // Actualizar incluyendo la contraseña
                sqlUsuario = "UPDATE users SET username = ?, email = ?, matricula = ?, " +
                           "password_hash = ?, is_active = ? WHERE id = ?";
                statementUsuario = conexion.prepareStatement(sqlUsuario);
                statementUsuario.setString(1, nombre);
                statementUsuario.setString(2, correo);
                statementUsuario.setString(3, matricula);
                
                // Hash de la contraseña
                String hashContrasena = hashPassword(nuevaContrasena);
                statementUsuario.setString(4, hashContrasena);
                statementUsuario.setInt(5, cuentaActiva ? 1 : 0);
                statementUsuario.setLong(6, usuarioId);
            } else {
                // Actualizar sin cambiar la contraseña
                sqlUsuario = "UPDATE users SET username = ?, email = ?, matricula = ?, " +
                           "is_active = ? WHERE id = ?";
                statementUsuario = conexion.prepareStatement(sqlUsuario);
                statementUsuario.setString(1, nombre);
                statementUsuario.setString(2, correo);
                statementUsuario.setString(3, matricula);
                statementUsuario.setInt(4, cuentaActiva ? 1 : 0);
                statementUsuario.setLong(5, usuarioId);
            }
            
            int filasAfectadas = statementUsuario.executeUpdate();
            statementUsuario.close();
            
            if (filasAfectadas == 0) {
                conexion.rollback();
                mostrarAlerta("Error", "Error al guardar", 
                             "No se pudieron guardar los cambios. El usuario no existe.", 
                             Alert.AlertType.ERROR);
                return;
            }
            
            // Actualizar el rol del usuario
            if (rolSeleccionado != null && mapaRoles.containsKey(rolSeleccionado)) {
                Long nuevoRolId = mapaRoles.get(rolSeleccionado);
                
                // Eliminar roles anteriores
                String sqlEliminarRol = "DELETE FROM user_roles WHERE user_id = ?";
                try (PreparedStatement stmtEliminar = conexion.prepareStatement(sqlEliminarRol)) {
                    stmtEliminar.setLong(1, usuarioId);
                    stmtEliminar.executeUpdate();
                }
                
                // Insertar nuevo rol
                String sqlInsertarRol = "INSERT INTO user_roles (user_id, role_id) VALUES (?, ?)";
                try (PreparedStatement stmtInsertar = conexion.prepareStatement(sqlInsertarRol)) {
                    stmtInsertar.setLong(1, usuarioId);
                    stmtInsertar.setLong(2, nuevoRolId);
                    stmtInsertar.executeUpdate();
                }
            }
            
            // Confirmar transacción
            conexion.commit();
            
            // Actualizar labels de información
            labelNombreUsuario.setText(nombre);
            labelEmailUsuario.setText(correo);
            
            // Limpiar campo de contraseña
            campoContrasena.clear();
            
            mostrarAlerta("Éxito", "Cambios guardados", 
                         "Los cambios del usuario han sido guardados correctamente.", 
                         Alert.AlertType.INFORMATION);
            
        } catch (SQLException e) {
            try {
                if (conexion != null) {
                    conexion.rollback();
                }
            } catch (SQLException ex) {
                System.err.println("Error al revertir transacción: " + ex.getMessage());
            }
            
            System.err.println("Error al guardar cambios: " + e.getMessage());
            
            // Verificar si es un error de duplicado
            if (e.getMessage().contains("Duplicate entry")) {
                if (e.getMessage().contains("email")) {
                    mostrarAlerta("Error", "Correo ya registrado", 
                                 "El correo electrónico ingresado ya está registrado. Por favor, use otro correo.", 
                                 Alert.AlertType.ERROR);
                } else if (e.getMessage().contains("matricula")) {
                    mostrarAlerta("Error", "Matrícula ya registrada", 
                                 "La matrícula ingresada ya está registrada. Por favor, use otra matrícula.", 
                                 Alert.AlertType.ERROR);
                } else {
                    mostrarAlerta("Error", "Error al guardar", 
                                 "No se pudieron guardar los cambios debido a un error en la base de datos.", 
                                 Alert.AlertType.ERROR);
                }
            } else {
                mostrarAlerta("Error", "Error al guardar", 
                             "No se pudieron guardar los cambios. Por favor, intente más tarde.", 
                             Alert.AlertType.ERROR);
            }
        } finally {
            try {
                if (conexion != null) {
                    conexion.setAutoCommit(true);
                }
            } catch (SQLException e) {
                System.err.println("Error al restaurar autocommit: " + e.getMessage());
            }
        }
    }
    
    /**
     * Maneja el clic en el botón "Deshabilitar Cuenta"
     * Cambia el estado activo/inactivo de la cuenta
     */
    @FXML
    private void manejarDeshabilitarCuenta() {
        boolean estadoActual = checkCuentaActiva.isSelected();
        String accion = estadoActual ? "deshabilitar" : "habilitar";
        
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar Acción");
        confirmacion.setHeaderText(accion.substring(0, 1).toUpperCase() + accion.substring(1) + " Cuenta");
        confirmacion.setContentText("¿Está seguro que desea " + accion + " la cuenta de " + 
                                   labelNombreUsuario.getText() + "?");
        
        confirmacion.showAndWait().ifPresent(response -> {
            if (response == javafx.scene.control.ButtonType.OK) {
                // Cambiar el estado del checkbox
                checkCuentaActiva.setSelected(!estadoActual);
                
                // Guardar el cambio
                String contrasenaVacia = "";
                guardarCambiosEnBD(contrasenaVacia);
            }
        });
    }
    
    /**
     * Maneja el clic en el botón "Eliminar Usuario"
     * Elimina permanentemente la cuenta del usuario
     */
    @FXML
    private void manejarEliminarUsuario() {
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar Eliminación");
        confirmacion.setHeaderText("Eliminar Usuario Permanentemente");
        confirmacion.setContentText("¿Está seguro que desea eliminar permanentemente la cuenta de " + 
                                   labelNombreUsuario.getText() + "?\n\n" +
                                   "Esta acción no se puede deshacer. Se eliminarán todos los datos del usuario.");
        
        confirmacion.showAndWait().ifPresent(response -> {
            if (response == javafx.scene.control.ButtonType.OK) {
                eliminarUsuarioDeBD();
            }
        });
    }
    
    /**
     * Elimina el usuario de la base de datos
     */
    private void eliminarUsuarioDeBD() {
        ConexionBD conexionBD = ConexionBD.obtenerInstancia();
        Connection conexion = null;
        
        try {
            conexion = conexionBD.obtenerConexion();
            conexion.setAutoCommit(false);
            
            // Eliminar roles del usuario
            String sqlEliminarRoles = "DELETE FROM user_roles WHERE user_id = ?";
            try (PreparedStatement stmt = conexion.prepareStatement(sqlEliminarRoles)) {
                stmt.setLong(1, usuarioId);
                stmt.executeUpdate();
            }
            
            // Eliminar el usuario
            String sqlEliminarUsuario = "DELETE FROM users WHERE id = ?";
            try (PreparedStatement stmt = conexion.prepareStatement(sqlEliminarUsuario)) {
                stmt.setLong(1, usuarioId);
                int filasAfectadas = stmt.executeUpdate();
                
                if (filasAfectadas > 0) {
                    conexion.commit();
                    mostrarAlerta("Éxito", "Usuario eliminado", 
                                 "El usuario ha sido eliminado permanentemente.", 
                                 Alert.AlertType.INFORMATION);
                    
                    // Volver a la lista de usuarios
                    manejarVolver();
                } else {
                    conexion.rollback();
                    mostrarAlerta("Error", "Error al eliminar", 
                                 "No se pudo eliminar el usuario.", 
                                 Alert.AlertType.ERROR);
                }
            }
            
        } catch (SQLException e) {
            try {
                if (conexion != null) {
                    conexion.rollback();
                }
            } catch (SQLException ex) {
                System.err.println("Error al revertir transacción: " + ex.getMessage());
            }
            
            System.err.println("Error al eliminar usuario: " + e.getMessage());
            
            // Verificar si hay restricciones de clave foránea
            if (e.getMessage().contains("foreign key constraint")) {
                mostrarAlerta("Error", "No se puede eliminar", 
                             "No se puede eliminar este usuario porque tiene registros relacionados " +
                             "(reservas, reportes, etc.). Considere deshabilitar la cuenta en su lugar.", 
                             Alert.AlertType.ERROR);
            } else {
                mostrarAlerta("Error", "Error al eliminar", 
                             "No se pudo eliminar el usuario. Por favor, intente más tarde.", 
                             Alert.AlertType.ERROR);
            }
        } finally {
            try {
                if (conexion != null) {
                    conexion.setAutoCommit(true);
                }
            } catch (SQLException e) {
                System.err.println("Error al restaurar autocommit: " + e.getMessage());
            }
        }
    }
    
    /**
     * Genera un hash simple de la contraseña
     * TODO: Mejorar con bcrypt o similar para mayor seguridad
     * 
     * @param password Contraseña en texto plano
     * @return Hash de la contraseña
     */
    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            System.err.println("Error al generar hash: " + e.getMessage());
            return password;
        }
    }
    
    /**
     * Muestra una alerta al usuario
     * 
     * @param titulo Título de la alerta
     * @param encabezado Encabezado de la alerta
     * @param mensaje Mensaje de la alerta
     * @param tipo Tipo de alerta
     */
    private void mostrarAlerta(String titulo, String encabezado, String mensaje, Alert.AlertType tipo) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(encabezado);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}

