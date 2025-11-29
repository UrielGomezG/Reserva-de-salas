package interfaz.sara.Controladores.Admin;

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
 * Controlador para el componente Navbar del administrador
 * Gestiona la navegación entre las diferentes secciones del panel de administración
 */
public class ControladorNavbarAdmin {

    // ========== Componentes FXML ==========
    
    @FXML
    private Button botonUsuarios;
    
    @FXML
    private Button botonSalas;
    
    @FXML
    private Button botonReservas;
    
    @FXML
    private Button botonReportes;
    
    @FXML
    private Button botonNotificaciones;
    
    @FXML
    private Button botonPerfil;
    
    @FXML
    private Button botonCerrarSesion;
    
    @FXML
    private ImageView perfilImageView;
    
    /** Carpeta donde se guardan las fotos de perfil */
    private static final String CARPETA_FOTOS_PERFIL = "profile_pictures";

    // ========== Métodos de inicialización ==========
    
    /**
     * Inicializa el controlador después de que se carga el FXML
     * Carga la foto de perfil del administrador
     */
    @FXML
    private void initialize() {
        // Cargar la foto de perfil del administrador
        cargarFotoPerfil();
    }
    
    /**
     * Carga la foto de perfil del administrador desde la base de datos
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
                
                // Intentar desde la carpeta profile_pictures
                File archivoEnCarpeta = new File(CARPETA_FOTOS_PERFIL, new File(rutaImagen).getName());
                if (archivoEnCarpeta.exists()) {
                    Image imagen = new Image(new FileInputStream(archivoEnCarpeta));
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
            // Intentar cargar desde la carpeta profile_pictures
            File defaultFile = new File(CARPETA_FOTOS_PERFIL, "usuario.png");
            if (defaultFile.exists()) {
                Image imagen = new Image(new FileInputStream(defaultFile));
                perfilImageView.setImage(imagen);
                return;
            }
            
            // Intentar cargar desde recursos
            InputStream imagenStream = getClass().getResourceAsStream(
                "/interfaz/sara/imagenes/usuario.png"
            );
            if (imagenStream != null) {
                Image imagen = new Image(imagenStream);
                perfilImageView.setImage(imagen);
            } else {
                // Si no hay imagen en recursos, usar placeholder
                imagenStream = getClass().getResourceAsStream(
                    "/interfaz/sara/imagenes/perfil_placeholder.png"
                );
                if (imagenStream != null) {
                    Image imagen = new Image(imagenStream);
                    perfilImageView.setImage(imagen);
                } else {
                    perfilImageView.setImage(null);
                }
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
     * @param vistaActiva El nombre de la vista activa: "usuarios", "salas", "reservas", "reportes", "notificaciones", "perfil"
     */
    public void establecerVistaActiva(String vistaActiva) {
        // Resetear todos los botones
        botonUsuarios.getStyleClass().remove("navbar-button-active");
        botonSalas.getStyleClass().remove("navbar-button-active");
        botonReservas.getStyleClass().remove("navbar-button-active");
        botonReportes.getStyleClass().remove("navbar-button-active");
        botonNotificaciones.getStyleClass().remove("navbar-button-active");
        botonPerfil.getStyleClass().remove("navbar-button-active");
        botonPerfil.getStyleClass().remove("active");
        
        if (!botonUsuarios.getStyleClass().contains("navbar-button")) {
            botonUsuarios.getStyleClass().add("navbar-button");
        }
        if (!botonSalas.getStyleClass().contains("navbar-button")) {
            botonSalas.getStyleClass().add("navbar-button");
        }
        if (!botonReservas.getStyleClass().contains("navbar-button")) {
            botonReservas.getStyleClass().add("navbar-button");
        }
        if (!botonReportes.getStyleClass().contains("navbar-button")) {
            botonReportes.getStyleClass().add("navbar-button");
        }
        if (!botonNotificaciones.getStyleClass().contains("navbar-button")) {
            botonNotificaciones.getStyleClass().add("navbar-button");
        }
        if (!botonPerfil.getStyleClass().contains("navbar-button-profile")) {
            botonPerfil.getStyleClass().add("navbar-button-profile");
        }
        
        // Activar el botón correspondiente
        switch (vistaActiva) {
            case "usuarios":
                botonUsuarios.getStyleClass().remove("navbar-button");
                botonUsuarios.getStyleClass().add("navbar-button-active");
                break;
            case "salas":
                botonSalas.getStyleClass().remove("navbar-button");
                botonSalas.getStyleClass().add("navbar-button-active");
                break;
            case "reservas":
                botonReservas.getStyleClass().remove("navbar-button");
                botonReservas.getStyleClass().add("navbar-button-active");
                break;
            case "reportes":
                botonReportes.getStyleClass().remove("navbar-button");
                botonReportes.getStyleClass().add("navbar-button-active");
                break;
            case "notificaciones":
                botonNotificaciones.getStyleClass().remove("navbar-button");
                botonNotificaciones.getStyleClass().add("navbar-button-active");
                break;
            case "perfil":
                botonPerfil.getStyleClass().remove("navbar-button-profile");
                if (!botonPerfil.getStyleClass().contains("navbar-button-profile")) {
                    botonPerfil.getStyleClass().add("navbar-button-profile");
                }
                botonPerfil.getStyleClass().add("active");
                break;
        }
    }

    // ========== Métodos de navegación ==========
    
    /**
     * Navega a la vista de Usuarios
     */
    @FXML
    private void manejarUsuarios() {
        GestorNavegacion gestorNavegacion = GestorNavegacion.obtenerInstancia();
        gestorNavegacion.navegarAVistaUsuariosAdmin();
    }
    
    /**
     * Navega a la vista de Salas
     */
    @FXML
    private void manejarSalas() {
        GestorNavegacion gestorNavegacion = GestorNavegacion.obtenerInstancia();
        gestorNavegacion.navegarAVistaSalasAdmin();
    }
    
    /**
     * Navega a la vista de Reservas
     */
    @FXML
    private void manejarReservas() {
        GestorNavegacion gestorNavegacion = GestorNavegacion.obtenerInstancia();
        gestorNavegacion.navegarAVistaReservasAdmin();
    }
    
    /**
     * Navega a la vista de Reportes
     */
    @FXML
    private void manejarReportes() {
        GestorNavegacion gestorNavegacion = GestorNavegacion.obtenerInstancia();
        gestorNavegacion.navegarAVistaReportesAdmin();
    }
    
    /**
     * Navega a la vista de Notificaciones
     */
    @FXML
    private void manejarNotificaciones() {
        GestorNavegacion gestorNavegacion = GestorNavegacion.obtenerInstancia();
        gestorNavegacion.navegarAVistaNotificacionesAdmin();
    }
    
    /**
     * Navega a la vista de Perfil
     */
    @FXML
    private void manejarPerfil() {
        GestorNavegacion gestorNavegacion = GestorNavegacion.obtenerInstancia();
        gestorNavegacion.navegarAVistaPerfilAdmin();
    }
    
    /**
     * Cierra la sesión del administrador y navega al login
     */
    @FXML
    private void manejarCerrarSesion() {
        SesionUsuario sesion = SesionUsuario.obtenerInstancia();
        sesion.cerrarSesion();
        
        GestorNavegacion gestorNavegacion = GestorNavegacion.obtenerInstancia();
        gestorNavegacion.navegarALogin();
    }
}

