package interfaz.sara.Utilidades;

import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Gestor de navegación específico para las vistas de administrador
 * Maneja todas las redirecciones dentro del panel de administración
 */
public class GestorNavegacionAdmin {
    
    /** Instancia única del gestor (Singleton) */
    private static GestorNavegacionAdmin instancia;
    
    /** Referencia al gestor de navegación base para métodos comunes */
    private GestorNavegacion gestorBase;
    
    // ========== Variables de estado temporal ==========
    
    /** ID del usuario seleccionado para ver detalles (temporal) */
    private static Long usuarioIdSeleccionado = null;
    
    /** ID de la sala seleccionada para ver detalles (temporal) */
    private static Long salaIdSeleccionado = null;
    
    /** ID de la reserva seleccionada para ver detalles (temporal) */
    private static Long reservaIdSeleccionado = null;
    
    // ========== Constructor privado (Singleton) ==========
    
    /**
     * Constructor privado para prevenir la creación de instancias externas
     */
    private GestorNavegacionAdmin() {
        gestorBase = GestorNavegacion.obtenerInstancia();
    }
    
    // ========== Método para obtener la instancia singleton ==========
    
    /**
     * Obtiene la instancia única de GestorNavegacionAdmin (patrón Singleton)
     * 
     * @return La instancia única de GestorNavegacionAdmin
     */
    public static GestorNavegacionAdmin obtenerInstancia() {
        if (instancia == null) {
            synchronized (GestorNavegacionAdmin.class) {
                if (instancia == null) {
                    instancia = new GestorNavegacionAdmin();
                }
            }
        }
        return instancia;
    }
    
    // ========== Métodos de configuración ==========
    
    /**
     * Establece el escenario principal de la aplicación
     * Delega al gestor base
     * 
     * @param escenario El escenario principal
     */
    public void establecerEscenarioPrincipal(Stage escenario) {
        gestorBase.establecerEscenarioPrincipal(escenario);
    }
    
    // ========== Métodos de gestión de IDs temporales ==========
    
    /**
     * Establece el ID del usuario para ver sus detalles
     * 
     * @param usuarioId ID del usuario
     */
    public void establecerUsuarioIdSeleccionado(Long usuarioId) {
        usuarioIdSeleccionado = usuarioId;
    }
    
    /**
     * Obtiene el ID del usuario seleccionado para ver detalles
     * 
     * @return ID del usuario o null si no hay ninguno seleccionado
     */
    public Long obtenerUsuarioIdSeleccionado() {
        return usuarioIdSeleccionado;
    }
    
    /**
     * Establece el ID de la sala para ver sus detalles
     * 
     * @param salaId ID de la sala
     */
    public void establecerSalaIdSeleccionado(Long salaId) {
        salaIdSeleccionado = salaId;
    }
    
    /**
     * Obtiene el ID de la sala seleccionada para ver detalles
     * 
     * @return ID de la sala o null si no hay ninguna seleccionada
     */
    public Long obtenerSalaIdSeleccionado() {
        return salaIdSeleccionado;
    }
    
    /**
     * Establece el ID de la reserva para ver sus detalles
     * 
     * @param reservaId ID de la reserva
     */
    public void establecerReservaIdSeleccionado(Long reservaId) {
        reservaIdSeleccionado = reservaId;
    }
    
    /**
     * Obtiene el ID de la reserva seleccionada para ver detalles
     * 
     * @return ID de la reserva o null si no hay ninguna seleccionada
     */
    public Long obtenerReservaIdSeleccionado() {
        return reservaIdSeleccionado;
    }
    
    // ========== Métodos de navegación de administrador ==========
    
    /**
     * Navega a la vista de usuarios del administrador
     * Carga VistaUsuariosAdmin.fxml y aplica los estilos CSS correspondientes
     */
    public void navegarAVistaUsuariosAdmin() {
        cambiarAVista("/interfaz/sara/Vistas/Admin/VistaUsuariosAdmin.fxml", "/interfaz/sara/CSS/Admin/admin.css");
    }
    
    /**
     * Navega a la vista de detalle de usuario del administrador
     * Carga VistaDetalleUsuarioAdmin.fxml y aplica los estilos CSS correspondientes
     * 
     * @param usuarioId ID del usuario a mostrar
     */
    public void navegarAVistaDetalleUsuarioAdmin(Long usuarioId) {
        establecerUsuarioIdSeleccionado(usuarioId);
        cambiarAVista("/interfaz/sara/Vistas/Admin/VistaDetalleUsuarioAdmin.fxml", "/interfaz/sara/CSS/Admin/admin.css");
    }
    
    /**
     * Navega a la vista de editar usuario del administrador
     * Carga VistaEditarUsuarioAdmin.fxml y aplica los estilos CSS correspondientes
     * 
     * @param usuarioId ID del usuario a editar
     */
    public void navegarAVistaEditarUsuarioAdmin(Long usuarioId) {
        establecerUsuarioIdSeleccionado(usuarioId);
        cambiarAVista("/interfaz/sara/Vistas/Admin/VistaEditarUsuarioAdmin.fxml", "/interfaz/sara/CSS/Admin/admin.css");
    }
    
    /**
     * Navega a la vista de reservas del administrador
     * Carga VistaReservasAdmin.fxml y aplica los estilos CSS correspondientes
     */
    public void navegarAVistaReservasAdmin() {
        cambiarAVista("/interfaz/sara/Vistas/Admin/VistaReservasAdmin.fxml", "/interfaz/sara/CSS/Admin/admin.css");
    }
    
    /**
     * Navega a la vista de reportes del administrador
     * Carga VistaReportesAdmin.fxml y aplica los estilos CSS correspondientes
     */
    public void navegarAVistaReportesAdmin() {
        cambiarAVista("/interfaz/sara/Vistas/Admin/VistaReportesAdmin.fxml", "/interfaz/sara/CSS/Admin/admin.css");
    }
    
    /**
     * Navega a la vista de perfil del administrador
     * Carga VistaPerfilAdmin.fxml y aplica los estilos CSS correspondientes
     */
    public void navegarAVistaPerfilAdmin() {
        cambiarAVista("/interfaz/sara/Vistas/Admin/VistaPerfilAdmin.fxml", "/interfaz/sara/CSS/Admin/admin.css");
    }
    
    /**
     * Navega a la vista de notificaciones del administrador
     * Carga VistaNotificacionesAdmin.fxml y aplica los estilos CSS correspondientes
     */
    public void navegarAVistaNotificacionesAdmin() {
        cambiarAVista("/interfaz/sara/Vistas/Admin/VistaNotificacionesAdmin.fxml", "/interfaz/sara/CSS/Admin/admin.css");
    }
    
    /**
     * Navega a la vista de detalle de reserva del administrador
     * Carga VistaDetalleReservaAdmin.fxml y aplica los estilos CSS correspondientes
     * 
     * @param reservaId ID de la reserva a mostrar
     */
    public void navegarAVistaDetalleReservaAdmin(Long reservaId) {
        establecerReservaIdSeleccionado(reservaId);
        cambiarAVista("/interfaz/sara/Vistas/Admin/VistaDetalleReservaAdmin.fxml", "/interfaz/sara/CSS/Admin/admin.css");
    }
    
    /**
     * Navega a la vista de salas del administrador
     * Carga VistaSalasAdmin.fxml y aplica los estilos CSS correspondientes
     */
    public void navegarAVistaSalasAdmin() {
        cambiarAVista("/interfaz/sara/Vistas/Admin/VistaSalasAdmin.fxml", "/interfaz/sara/CSS/Admin/admin.css");
    }
    
    /**
     * Navega a la vista de registrar nueva sala del administrador
     * Carga VistaRegistrarSalaAdmin.fxml y aplica los estilos CSS correspondientes
     */
    public void navegarAVistaRegistrarSalaAdmin() {
        cambiarAVista("/interfaz/sara/Vistas/Admin/VistaRegistrarSalaAdmin.fxml", "/interfaz/sara/CSS/Admin/admin.css");
    }
    
    /**
     * Navega a la vista de detalle de sala del administrador
     * Carga VistaDetalleSalaAdmin.fxml y aplica los estilos CSS correspondientes
     * 
     * @param salaId ID de la sala a mostrar en detalle
     */
    public void navegarAVistaDetalleSalaAdmin(Long salaId) {
        establecerSalaIdSeleccionado(salaId);
        cambiarAVista("/interfaz/sara/Vistas/Admin/VistaDetalleSalaAdmin.fxml", "/interfaz/sara/CSS/Admin/admin.css");
    }
    
    /**
     * Navega a la vista de editar sala del administrador
     * Carga VistaEditarSalaAdmin.fxml y aplica los estilos CSS correspondientes
     * 
     * @param salaId ID de la sala a editar
     */
    public void navegarAVistaEditarSalaAdmin(Long salaId) {
        establecerSalaIdSeleccionado(salaId);
        cambiarAVista("/interfaz/sara/Vistas/Admin/VistaEditarSalaAdmin.fxml", "/interfaz/sara/CSS/Admin/admin.css");
    }
    
    /**
     * Navega al login (para cerrar sesión)
     */
    public void navegarALogin() {
        gestorBase.navegarALogin();
    }
    
    // ========== Métodos de utilidad ==========
    
    /**
     * Método genérico para cambiar a una vista específica de administrador
     * Carga el archivo FXML y aplica los estilos CSS indicados
     * 
     * @param rutaFXML Ruta del archivo FXML relativa a los recursos
     * @param rutaCSS Ruta del archivo CSS relativa a los recursos (puede ser null)
     */
    private void cambiarAVista(String rutaFXML, String rutaCSS) {
        gestorBase.cambiarAVista(rutaFXML, rutaCSS);
    }
    
    /**
     * Obtiene la referencia al escenario principal
     * 
     * @return El escenario principal de la aplicación
     */
    public Stage obtenerEscenarioPrincipal() {
        return gestorBase.obtenerEscenarioPrincipal();
    }
    
    /**
     * Obtiene la referencia a la escena actual
     * 
     * @return La escena actualmente activa
     */
    public Scene obtenerEscenaActual() {
        return gestorBase.obtenerEscenaActual();
    }
}

