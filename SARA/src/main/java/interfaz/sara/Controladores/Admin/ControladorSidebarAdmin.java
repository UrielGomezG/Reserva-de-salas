package interfaz.sara.Controladores.Admin;

import interfaz.sara.Utilidades.GestorNavegacionAdmin;
import interfaz.sara.Utilidades.SesionUsuario;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;

/**
 * Controlador para el componente Sidebar del administrador
 * Gestiona la navegación entre las diferentes secciones del panel de administración
 */
public class ControladorSidebarAdmin {

    // ========== Componentes FXML ==========
    
    @FXML
    private VBox sidebarInclude;
    
    @FXML
    private Button botonUsuarios;
    
    @FXML
    private Button botonSalas;
    
    @FXML
    private Button botonReservas;
    
    @FXML
    private Button botonReportes;
    
    @FXML
    private Button botonPerfil;
    
    @FXML
    private Button botonNotificaciones;
    
    @FXML
    private Button botonCerrarSesion;

    // ========== Métodos de inicialización ==========
    
    /**
     * Inicializa el controlador después de que se carga el FXML
     */
    @FXML
    private void initialize() {
        // La inicialización se hace automáticamente por FXML
    }
    
    /**
     * Establece qué botón del sidebar está activo
     * 
     * @param vistaActiva El nombre de la vista activa: "usuarios", "salas", "reservas", "reportes", "perfil", "notificaciones"
     */
    public void establecerVistaActiva(String vistaActiva) {
        // Resetear todos los botones - remover estado activo
        botonUsuarios.getStyleClass().remove("sidebar-button-active");
        botonSalas.getStyleClass().remove("sidebar-button-active");
        botonReservas.getStyleClass().remove("sidebar-button-active");
        botonReportes.getStyleClass().remove("sidebar-button-active");
        botonPerfil.getStyleClass().remove("sidebar-button-active");
        botonNotificaciones.getStyleClass().remove("sidebar-button-active");
        
        // Asegurarse de que todos los botones tengan el estilo base
        if (!botonUsuarios.getStyleClass().contains("sidebar-button")) {
            botonUsuarios.getStyleClass().add("sidebar-button");
        }
        if (!botonSalas.getStyleClass().contains("sidebar-button")) {
            botonSalas.getStyleClass().add("sidebar-button");
        }
        if (!botonReservas.getStyleClass().contains("sidebar-button")) {
            botonReservas.getStyleClass().add("sidebar-button");
        }
        if (!botonReportes.getStyleClass().contains("sidebar-button")) {
            botonReportes.getStyleClass().add("sidebar-button");
        }
        if (!botonPerfil.getStyleClass().contains("sidebar-button")) {
            botonPerfil.getStyleClass().add("sidebar-button");
        }
        if (!botonNotificaciones.getStyleClass().contains("sidebar-button")) {
            botonNotificaciones.getStyleClass().add("sidebar-button");
        }
        
        // Remover estilo base del botón que será activo y agregar estilo activo
        switch (vistaActiva) {
            case "usuarios":
                botonUsuarios.getStyleClass().remove("sidebar-button");
                botonUsuarios.getStyleClass().add("sidebar-button-active");
                break;
            case "salas":
                botonSalas.getStyleClass().remove("sidebar-button");
                botonSalas.getStyleClass().add("sidebar-button-active");
                break;
            case "reservas":
                botonReservas.getStyleClass().remove("sidebar-button");
                botonReservas.getStyleClass().add("sidebar-button-active");
                break;
            case "reportes":
                botonReportes.getStyleClass().remove("sidebar-button");
                botonReportes.getStyleClass().add("sidebar-button-active");
                break;
            case "perfil":
                botonPerfil.getStyleClass().remove("sidebar-button");
                botonPerfil.getStyleClass().add("sidebar-button-active");
                break;
            case "notificaciones":
                botonNotificaciones.getStyleClass().remove("sidebar-button");
                botonNotificaciones.getStyleClass().add("sidebar-button-active");
                break;
        }
    }

    // ========== Métodos de navegación ==========
    
    /**
     * Navega a la vista de Usuarios
     */
    @FXML
    private void manejarUsuarios() {
        GestorNavegacionAdmin gestorNavegacion = GestorNavegacionAdmin.obtenerInstancia();
        gestorNavegacion.navegarAVistaUsuariosAdmin();
    }
    
    /**
     * Navega a la vista de Salas
     */
    @FXML
    private void manejarSalas() {
        GestorNavegacionAdmin gestorNavegacion = GestorNavegacionAdmin.obtenerInstancia();
        gestorNavegacion.navegarAVistaSalasAdmin();
    }
    
    /**
     * Navega a la vista de Reservas
     */
    @FXML
    private void manejarReservas() {
        GestorNavegacionAdmin gestorNavegacion = GestorNavegacionAdmin.obtenerInstancia();
        gestorNavegacion.navegarAVistaReservasAdmin();
    }
    
    /**
     * Navega a la vista de Reportes
     */
    @FXML
    private void manejarReportes() {
        GestorNavegacionAdmin gestorNavegacion = GestorNavegacionAdmin.obtenerInstancia();
        gestorNavegacion.navegarAVistaReportesAdmin();
    }
    
    /**
     * Navega a la vista de Perfil
     */
    @FXML
    private void manejarPerfil() {
        GestorNavegacionAdmin gestorNavegacion = GestorNavegacionAdmin.obtenerInstancia();
        gestorNavegacion.navegarAVistaPerfilAdmin();
    }
    
    /**
     * Navega a la vista de Notificaciones
     */
    @FXML
    private void manejarNotificaciones() {
        GestorNavegacionAdmin gestorNavegacion = GestorNavegacionAdmin.obtenerInstancia();
        gestorNavegacion.navegarAVistaNotificacionesAdmin();
    }
    
    /**
     * Cierra la sesión del administrador y navega al login
     */
    @FXML
    private void manejarCerrarSesion() {
        SesionUsuario sesion = SesionUsuario.obtenerInstancia();
        sesion.cerrarSesion();
        
        GestorNavegacionAdmin gestorNavegacion = GestorNavegacionAdmin.obtenerInstancia();
        gestorNavegacion.navegarALogin();
    }
}

