package interfaz.sara.Controladores.Usuario;

import interfaz.sara.ConexionBD.ConexionBD;
import interfaz.sara.Utilidades.GestorNavegacion;
import interfaz.sara.Utilidades.SesionUsuario;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Controlador para el componente Navbar del usuario
 * Gestiona la navegación entre las diferentes secciones
 */
public class ControladorNavbarUsuario {

    // ========== Componentes FXML ==========
    
    @FXML
    private Button botonMisReservas;
    
    @FXML
    private Button botonNuevaReserva;
    
    @FXML
    private Button botonPerfil;
    
    @FXML
    private Button botonReporte;
    
    @FXML
    private Button botonNotificaciones;
    
    @FXML
    private Button botonCerrarSesion;
    
    @FXML
    private ImageView perfilImageView;

    // ========== Métodos de inicialización ==========
    
    /**
     * Inicializa el controlador después de que se carga el FXML
     * Carga la foto de perfil del usuario
     */
    @FXML
    private void initialize() {
        // Cargar la foto de perfil del usuario
        cargarFotoPerfil();
    }
    
    /**
     * Carga la foto de perfil del usuario desde la base de datos
     */
    private void cargarFotoPerfil() {
        SesionUsuario sesion = SesionUsuario.obtenerInstancia();
        if (!sesion.estaAutenticado()) {
            return;
        }
        
        Long usuarioId = sesion.getUsuarioId();
        ConexionBD conexionBD = ConexionBD.obtenerInstancia();
        
        String sql = "SELECT profile_picture_path FROM users WHERE id = ?";
        
        try {
            Connection conexion = conexionBD.obtenerConexion();
            try (PreparedStatement statement = conexion.prepareStatement(sql)) {
                statement.setLong(1, usuarioId);
                
                try (ResultSet resultado = statement.executeQuery()) {
                    if (resultado.next()) {
                        String rutaImagen = resultado.getString("profile_picture_path");
                        if (rutaImagen != null && !rutaImagen.trim().isEmpty()) {
                            Platform.runLater(() -> actualizarImagenPerfil(rutaImagen));
                        } else {
                            Platform.runLater(() -> cargarImagenPorDefecto());
                        }
                    } else {
                        Platform.runLater(() -> cargarImagenPorDefecto());
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al cargar foto de perfil del navbar: " + e.getMessage());
            Platform.runLater(() -> cargarImagenPorDefecto());
        }
    }
    
    /**
     * Actualiza la imagen de perfil en el navbar
     * 
     * @param rutaImagen Ruta de la imagen de perfil
     */
    private void actualizarImagenPerfil(String rutaImagen) {
        if (perfilImageView == null) {
            return;
        }
        
        try {
            if (rutaImagen != null && !rutaImagen.trim().isEmpty()) {
                File archivoImagen = new File(rutaImagen);
                if (archivoImagen.exists()) {
                    Image imagen = new Image(new FileInputStream(archivoImagen));
                    perfilImageView.setImage(imagen);
                    return;
                }
            }
            
            // Si no se encontró, cargar imagen por defecto
            cargarImagenPorDefecto();
        } catch (FileNotFoundException e) {
            System.err.println("No se encontró la imagen de perfil: " + rutaImagen);
            cargarImagenPorDefecto();
        } catch (Exception e) {
            System.err.println("Error al cargar imagen de perfil en navbar: " + e.getMessage());
            cargarImagenPorDefecto();
        }
    }
    
    /**
     * Carga una imagen de perfil por defecto
     */
    private void cargarImagenPorDefecto() {
        if (perfilImageView == null) {
            return;
        }
        
        try {
            // Intentar cargar desde recursos
            InputStream imagenStream = getClass().getResourceAsStream(
                "/interfaz/sara/imagenes/perfil_placeholder.png"
            );
            if (imagenStream != null) {
                Image imagen = new Image(imagenStream);
                perfilImageView.setImage(imagen);
            } else {
                perfilImageView.setImage(null);
            }
        } catch (Exception e) {
            System.err.println("Error al cargar imagen por defecto en navbar: " + e.getMessage());
            perfilImageView.setImage(null);
        }
    }
    
    /**
     * Actualiza la imagen de perfil (método público para ser llamado desde otros controladores)
     * 
     * @param rutaImagen Nueva ruta de la imagen de perfil
     */
    public void actualizarFotoPerfil(String rutaImagen) {
        Platform.runLater(() -> actualizarImagenPerfil(rutaImagen));
    }
    
    /**
     * Establece qué botón del navbar está activo
     * 
     * @param vistaActiva El nombre de la vista activa: "misReservas", "nuevaReserva", "perfil"
     */
    public void establecerVistaActiva(String vistaActiva) {
        // Resetear todos los botones
        botonMisReservas.getStyleClass().remove("navbar-button-active");
        botonNuevaReserva.getStyleClass().remove("navbar-button-active");
        botonPerfil.getStyleClass().remove("navbar-button-active");
        botonReporte.getStyleClass().remove("navbar-button-active");
        if (botonNotificaciones != null) {
            botonNotificaciones.getStyleClass().remove("navbar-button-active");
        }
        
        if (!botonMisReservas.getStyleClass().contains("navbar-button")) {
            botonMisReservas.getStyleClass().add("navbar-button");
        }
        if (!botonNuevaReserva.getStyleClass().contains("navbar-button")) {
            botonNuevaReserva.getStyleClass().add("navbar-button");
        }
        if (!botonPerfil.getStyleClass().contains("navbar-button")) {
            botonPerfil.getStyleClass().add("navbar-button");
        }
        if (!botonReporte.getStyleClass().contains("navbar-button")) {
            botonReporte.getStyleClass().add("navbar-button");
        }
        if (botonNotificaciones != null && !botonNotificaciones.getStyleClass().contains("navbar-button")) {
            botonNotificaciones.getStyleClass().add("navbar-button");
        }
        
        // Activar el botón correspondiente
        switch (vistaActiva) {
            case "misReservas":
                botonMisReservas.getStyleClass().remove("navbar-button");
                botonMisReservas.getStyleClass().add("navbar-button-active");
                break;
            case "nuevaReserva":
                botonNuevaReserva.getStyleClass().remove("navbar-button");
                botonNuevaReserva.getStyleClass().add("navbar-button-active");
                break;
            case "perfil":
                botonPerfil.getStyleClass().remove("navbar-button");
                botonPerfil.getStyleClass().remove("navbar-button-active");
                if (!botonPerfil.getStyleClass().contains("navbar-button-profile")) {
                    botonPerfil.getStyleClass().add("navbar-button-profile");
                }
                botonPerfil.getStyleClass().add("active");
                break;
            case "reporte":
                botonReporte.getStyleClass().remove("navbar-button");
                botonReporte.getStyleClass().add("navbar-button-active");
                break;
            case "notificaciones":
                if (botonNotificaciones != null) {
                    botonNotificaciones.getStyleClass().remove("navbar-button");
                    botonNotificaciones.getStyleClass().add("navbar-button-active");
                }
                break;
        }
    }

    // ========== Métodos de navegación ==========
    
    /**
     * Navega a la vista de Mis Reservas
     */
    @FXML
    private void manejarMisReservas() {
        GestorNavegacion gestorNavegacion = GestorNavegacion.obtenerInstancia();
        gestorNavegacion.navegarAVistaPrincipalUsuario();
    }
    
    /**
     * Navega a la vista de Nueva Reserva
     */
    @FXML
    private void manejarNuevaReserva() {
        GestorNavegacion gestorNavegacion = GestorNavegacion.obtenerInstancia();
        gestorNavegacion.navegarAVistaNuevaReserva();
    }
    
    /**
     * Navega a la vista de Perfil
     */
    @FXML
    private void manejarPerfil() {
        GestorNavegacion gestorNavegacion = GestorNavegacion.obtenerInstancia();
        gestorNavegacion.navegarAVistaPerfil();
    }
    
    /**
     * Navega a la vista de Reporte
     */
    @FXML
    private void manejarReporte() {
        GestorNavegacion gestorNavegacion = GestorNavegacion.obtenerInstancia();
        gestorNavegacion.navegarAVistaReporte();
    }
    
    /**
     * Navega a la vista de Notificaciones
     */
    @FXML
    private void manejarNotificaciones() {
        GestorNavegacion gestorNavegacion = GestorNavegacion.obtenerInstancia();
        gestorNavegacion.navegarAVistaNotificaciones();
    }
    
    /**
     * Cierra la sesión del usuario y navega al login
     */
    @FXML
    private void manejarCerrarSesion() {
        SesionUsuario sesion = SesionUsuario.obtenerInstancia();
        sesion.cerrarSesion();
        
        GestorNavegacion gestorNavegacion = GestorNavegacion.obtenerInstancia();
        gestorNavegacion.navegarALogin();
    }
}

