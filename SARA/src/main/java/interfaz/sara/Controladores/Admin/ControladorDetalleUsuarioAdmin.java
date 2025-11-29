package interfaz.sara.Controladores.Admin;

import interfaz.sara.ConexionBD.ConexionBD;
import interfaz.sara.Utilidades.GestorNavegacion;
import interfaz.sara.Utilidades.SesionUsuario;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Controlador para la vista de detalle de usuario del administrador (VistaDetalleUsuarioAdmin.fxml)
 * Muestra información detallada de un usuario específico
 */
public class ControladorDetalleUsuarioAdmin {

    // ========== Componentes FXML ==========
    
    @FXML
    private VBox sidebarInclude;
    
    @FXML
    private ImageView profileImageView;
    
    @FXML
    private Label labelNombrePrincipal;
    
    @FXML
    private Label labelRol;
    
    @FXML
    private Label labelEmailPrincipal;
    
    @FXML
    private Label labelEmailDetalle;
    
    @FXML
    private Label labelMatricula;
    
    @FXML
    private Label labelFechaRegistro;
    
    @FXML
    private Label labelTotalReservas;
    
    @FXML
    private Label labelSalaMasUsada;

    // ========== Variables de estado ==========
    
    /** ID del usuario que se está visualizando */
    private Long usuarioId;

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
                         "No se ha seleccionado un usuario para ver sus detalles.", 
                         Alert.AlertType.ERROR);
            return;
        }
        
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
     * Carga los datos del usuario desde la base de datos
     */
    private void cargarDatosUsuario() {
        ConexionBD conexionBD = ConexionBD.obtenerInstancia();
        
        String sql = "SELECT u.id, u.username, u.email, u.matricula, u.is_active, " +
                    "u.created_at, u.profile_picture_path, " +
                    "COALESCE(GROUP_CONCAT(DISTINCT r.name SEPARATOR ', '), 'Sin rol') as roles " +
                    "FROM users u " +
                    "LEFT JOIN user_roles ur ON u.id = ur.user_id " +
                    "LEFT JOIN roles r ON ur.role_id = r.id " +
                    "WHERE u.id = ? " +
                    "GROUP BY u.id, u.username, u.email, u.matricula, u.is_active, u.created_at, u.profile_picture_path";
        
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
                        String roles = resultado.getString("roles");
                        String rutaImagen = resultado.getString("profile_picture_path");
                        
                        // Actualizar labels principales
                        labelNombrePrincipal.setText(username != null ? username : "Sin nombre");
                        labelRol.setText(roles != null ? roles : "Sin rol");
                        labelEmailPrincipal.setText(email != null ? email : "Sin email");
                        
                        // Actualizar labels de detalle
                        labelEmailDetalle.setText(email != null ? email : "Sin email");
                        labelMatricula.setText(matricula != null ? matricula : "Sin matrícula");
                        
                        // Formatear fecha de registro
                        if (resultado.getTimestamp("created_at") != null) {
                            LocalDateTime fechaCreacion = resultado.getTimestamp("created_at").toLocalDateTime();
                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
                            labelFechaRegistro.setText(fechaCreacion.format(formatter));
                        } else {
                            labelFechaRegistro.setText("No disponible");
                        }
                        
                        // Cargar imagen de perfil
                        cargarImagenPerfil(rutaImagen);
                        
                        // Cargar estadísticas del usuario
                        cargarEstadisticasUsuario();
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
     * Carga las estadísticas del usuario (total de reservas, sala más usada)
     */
    private void cargarEstadisticasUsuario() {
        ConexionBD conexionBD = ConexionBD.obtenerInstancia();
        
        // Consulta para obtener total de reservas
        String sqlReservas = "SELECT COUNT(*) as total_reservas " +
                            "FROM reservations " +
                            "WHERE user_id = ?";
        
        // Consulta para obtener sala más utilizada
        String sqlSalaMasUsada = "SELECT r.name as nombre_sala, COUNT(*) as veces_usada " +
                                 "FROM reservations res " +
                                 "INNER JOIN rooms r ON res.room_id = r.id " +
                                 "WHERE res.user_id = ? " +
                                 "GROUP BY r.id, r.name " +
                                 "ORDER BY veces_usada DESC " +
                                 "LIMIT 1";
        
        try {
            Connection conexion = conexionBD.obtenerConexion();
            
            // Obtener total de reservas
            try (PreparedStatement statement = conexion.prepareStatement(sqlReservas)) {
                statement.setLong(1, usuarioId);
                
                try (ResultSet resultado = statement.executeQuery()) {
                    if (resultado.next()) {
                        int totalReservas = resultado.getInt("total_reservas");
                        labelTotalReservas.setText(String.valueOf(totalReservas));
                    } else {
                        labelTotalReservas.setText("0");
                    }
                }
            }
            
            // Obtener sala más utilizada
            try (PreparedStatement statement = conexion.prepareStatement(sqlSalaMasUsada)) {
                statement.setLong(1, usuarioId);
                
                try (ResultSet resultado = statement.executeQuery()) {
                    if (resultado.next()) {
                        String nombreSala = resultado.getString("nombre_sala");
                        int vecesUsada = resultado.getInt("veces_usada");
                        labelSalaMasUsada.setText(nombreSala + " (" + vecesUsada + " veces)");
                    } else {
                        labelSalaMasUsada.setText("Ninguna");
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al cargar estadísticas: " + e.getMessage());
            labelTotalReservas.setText("Error al cargar");
            labelSalaMasUsada.setText("Error al cargar");
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
     * Navega de vuelta a la lista de usuarios
     */
    @FXML
    private void manejarVolver() {
        GestorNavegacion gestorNavegacion = GestorNavegacion.obtenerInstancia();
        gestorNavegacion.navegarAVistaUsuariosAdmin();
    }
    
    /**
     * Maneja el clic en el botón "Editar Usuario"
     * Navega a la vista de edición de usuario
     */
    @FXML
    private void manejarEditarUsuario() {
        GestorNavegacion gestorNavegacion = GestorNavegacion.obtenerInstancia();
        gestorNavegacion.navegarAVistaEditarUsuarioAdmin(usuarioId);
    }
    
    /**
     * Maneja el clic en el botón "Eliminar Usuario"
     */
    @FXML
    private void manejarEliminarUsuario() {
        // TODO: Implementar confirmación y eliminación de usuario
        mostrarAlerta("Información", "Eliminar Usuario", 
                     "Eliminando usuario: " + labelNombrePrincipal.getText() + 
                     "\nLa funcionalidad de eliminación estará disponible próximamente.", 
                     Alert.AlertType.INFORMATION);
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

