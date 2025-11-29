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

/**
 * Controlador para la vista de detalle de sala del administrador (VistaDetalleSalaAdmin.fxml)
 * Muestra información detallada de una sala y permite editarla, activarla/desactivarla o eliminarla
 */
public class ControladorDetalleSalaAdmin {

    // ========== Componentes FXML ==========
    
    @FXML
    private VBox sidebarInclude;
    
    @FXML
    private ImageView imagenSalaView;
    
    @FXML
    private Label lblNombreSala;
    
    @FXML
    private Label lblTipo;
    
    @FXML
    private Label lblEstado;
    
    @FXML
    private Label lblCodigo;
    
    @FXML
    private Label lblCapacidad;
    
    @FXML
    private Label lblUbicacion;
    
    @FXML
    private Label lblTipoDetalle;

    // ========== Variables de estado ==========
    
    /** ID de la sala que se está visualizando */
    private Long salaId;

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
        
        // Obtener el ID de la sala seleccionada
        GestorNavegacion gestorNavegacion = GestorNavegacion.obtenerInstancia();
        salaId = gestorNavegacion.obtenerSalaIdSeleccionado();
        
        if (salaId == null) {
            mostrarAlerta("Error", "Sala no seleccionada", 
                         "No se ha seleccionado una sala para ver sus detalles.", 
                         Alert.AlertType.ERROR);
            return;
        }
        
        // Cargar datos de la sala desde la base de datos
        cargarDatosSala();
        
        // Configurar el sidebar
        Platform.runLater(() -> {
            configurarSidebar();
        });
    }
    
    /**
     * Configura el sidebar para resaltar el botón de salas como activo
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
                
                if (btnSalas != null) {
                    // Resetear todos
                    resetearBotonesSidebar(btnUsuarios, btnReservas, btnReportes, btnPerfil, btnNotificaciones);
                    
                    // Activar salas
                    btnSalas.getStyleClass().remove("sidebar-button");
                    if (!btnSalas.getStyleClass().contains("sidebar-button-active")) {
                        btnSalas.getStyleClass().add("sidebar-button-active");
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
     * Carga los datos de la sala desde la base de datos
     */
    private void cargarDatosSala() {
        ConexionBD conexionBD = ConexionBD.obtenerInstancia();
        
        String sql = "SELECT r.id, r.room_code, r.name, r.capacity, r.room_type, " +
                    "COALESCE(l.name, 'Sin ubicación') as location, " +
                    "r.is_enabled, r.image_path " +
                    "FROM rooms r " +
                    "LEFT JOIN locations l ON r.location_id = l.id " +
                    "WHERE r.id = ?";
        
        try {
            Connection conexion = conexionBD.obtenerConexion();
            try (PreparedStatement statement = conexion.prepareStatement(sql)) {
                statement.setLong(1, salaId);
                
                try (ResultSet resultado = statement.executeQuery()) {
                    if (resultado.next()) {
                        // Cargar datos básicos
                        int codigoSala = resultado.getInt("room_code");
                        String nombre = resultado.getString("name");
                        int capacidad = resultado.getInt("capacity");
                        String tipoSala = resultado.getString("room_type");
                        String ubicacion = resultado.getString("location");
                        boolean habilitada = resultado.getInt("is_enabled") == 1;
                        String rutaImagen = resultado.getString("image_path");
                        
                        // Actualizar labels
                        lblNombreSala.setText(nombre != null ? nombre : "Sin nombre");
                        lblTipo.setText(formatearTipoSala(tipoSala));
                        lblEstado.setText(habilitada ? "Habilitada" : "Deshabilitada");
                        lblCodigo.setText(String.valueOf(codigoSala));
                        lblCapacidad.setText(capacidad + " personas");
                        lblUbicacion.setText(ubicacion != null ? ubicacion : "Sin ubicación");
                        lblTipoDetalle.setText(formatearTipoSala(tipoSala));
                        
                        // Cargar imagen de la sala
                        cargarImagenSala(rutaImagen);
                    } else {
                        mostrarAlerta("Error", "Sala no encontrada", 
                                     "No se encontró la sala en la base de datos.", 
                                     Alert.AlertType.ERROR);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al cargar datos de la sala: " + e.getMessage());
            e.printStackTrace();
            mostrarAlerta("Error", "Error al cargar datos", 
                         "No se pudieron cargar los datos de la sala. Por favor, intente más tarde.", 
                         Alert.AlertType.ERROR);
        }
    }
    
    /**
     * Formatea el tipo de sala para mostrarlo de forma legible
     * 
     * @param tipoSala Tipo de sala desde la BD
     * @return Tipo de sala formateado
     */
    private String formatearTipoSala(String tipoSala) {
        if (tipoSala == null || tipoSala.trim().isEmpty()) {
            return "Sin tipo";
        }
        
        String tipoUpper = tipoSala.toUpperCase().trim();
        
        // Mapear códigos a nombres legibles
        switch (tipoUpper) {
            case "BOARDROOM":
            case "SALA DE JUNTAS":
                return "Sala de Juntas";
            case "LABORATORY":
            case "LABORATORIO":
                return "Laboratorio";
            case "AUDITORIUM":
            case "AUDITORIO":
                return "Auditorio";
            default:
                // Si ya está formateado, retornarlo tal cual
                if (tipoSala.equals("Sala de Juntas") || 
                    tipoSala.equals("Laboratorio") || 
                    tipoSala.equals("Auditorio")) {
                    return tipoSala;
                }
                // Si no, retornar capitalizado
                return tipoSala;
        }
    }
    
    /**
     * Carga la imagen de la sala desde la ruta especificada
     * 
     * @param rutaImagen Ruta de la imagen (puede ser null o vacía)
     */
    private void cargarImagenSala(String rutaImagen) {
        try {
            if (rutaImagen != null && !rutaImagen.trim().isEmpty()) {
                File archivoImagen = new File(rutaImagen);
                if (archivoImagen.exists()) {
                    Image imagen = new Image(new FileInputStream(archivoImagen));
                    imagenSalaView.setImage(imagen);
                    return;
                }
            }
            
            // Si no hay imagen o no se encontró, cargar imagen por defecto
            cargarImagenPorDefecto();
        } catch (FileNotFoundException e) {
            System.err.println("No se encontró la imagen de la sala: " + rutaImagen);
            cargarImagenPorDefecto();
        } catch (Exception e) {
            System.err.println("Error al cargar imagen de sala: " + e.getMessage());
            cargarImagenPorDefecto();
        }
    }
    
    /**
     * Carga una imagen de sala por defecto
     */
    private void cargarImagenPorDefecto() {
        try {
            InputStream imagenStream = getClass().getResourceAsStream(
                "/interfaz/sara/imagenes/room_placeholder.png"
            );
            if (imagenStream != null) {
                Image imagen = new Image(imagenStream);
                imagenSalaView.setImage(imagen);
            } else {
                imagenSalaView.setImage(null);
            }
        } catch (Exception e) {
            System.err.println("Error al cargar imagen por defecto: " + e.getMessage());
            imagenSalaView.setImage(null);
        }
    }

    // ========== Métodos de manejo de eventos ==========
    
    /**
     * Maneja el clic en el botón "Volver"
     * Navega de vuelta a la vista de gestión de salas
     */
    @FXML
    private void manejarVolver() {
        GestorNavegacion gestorNavegacion = GestorNavegacion.obtenerInstancia();
        gestorNavegacion.navegarAVistaSalasAdmin();
    }
    
    /**
     * Maneja el clic en el botón "Editar Sala"
     * Navega a la vista de edición de sala
     */
    @FXML
    private void manejarEditar() {
        GestorNavegacion gestorNavegacion = GestorNavegacion.obtenerInstancia();
        gestorNavegacion.navegarAVistaEditarSalaAdmin(salaId);
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

