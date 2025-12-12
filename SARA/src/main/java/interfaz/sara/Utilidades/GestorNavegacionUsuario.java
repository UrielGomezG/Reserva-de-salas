package interfaz.sara.Utilidades;

import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Gestor de navegación específico para las vistas de usuario
 * Maneja todas las redirecciones dentro del panel de usuario
 */
public class GestorNavegacionUsuario {
    
    /** Instancia única del gestor (Singleton) */
    private static GestorNavegacionUsuario instancia;
    
    /** Referencia al gestor de navegación base para métodos comunes */
    private GestorNavegacion gestorBase;
    
    // ========== Variables de estado temporal para pasar datos entre vistas ==========
    
    /** ID de la sala seleccionada para crear reserva */
    private static Long salaIdSeleccionada = null;
    
    /** Hora seleccionada para crear reserva (formato HH:mm) */
    private static String horaSeleccionada = null;
    
    /** Fecha seleccionada para crear reserva */
    private static java.time.LocalDate fechaSeleccionada = null;
    
    // ========== Constructor privado (Singleton) ==========
    
    /**
     * Constructor privado para prevenir la creación de instancias externas
     */
    private GestorNavegacionUsuario() {
        gestorBase = GestorNavegacion.obtenerInstancia();
    }
    
    // ========== Método para obtener la instancia singleton ==========
    
    /**
     * Obtiene la instancia única de GestorNavegacionUsuario (patrón Singleton)
     * 
     * @return La instancia única de GestorNavegacionUsuario
     */
    public static GestorNavegacionUsuario obtenerInstancia() {
        if (instancia == null) {
            synchronized (GestorNavegacionUsuario.class) {
                if (instancia == null) {
                    instancia = new GestorNavegacionUsuario();
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
    
    // ========== Métodos de navegación de usuario ==========
    
    /**
     * Navega a la vista principal del usuario
     * Carga VistaPrincipalUsuario.fxml y aplica los estilos CSS correspondientes
     */
    public void navegarAVistaPrincipalUsuario() {
        cambiarAVista("/interfaz/sara/Vistas/Usuario/VistaPrincipalUsuario.fxml", "/interfaz/sara/CSS/Usuario/usuario.css");
    }
    
    /**
     * Navega a la vista de nueva reserva
     * Carga VistaNuevaReserva.fxml y aplica los estilos CSS correspondientes
     */
    public void navegarAVistaNuevaReserva() {
        cambiarAVista("/interfaz/sara/Vistas/Usuario/VistaNuevaReserva.fxml", "/interfaz/sara/CSS/Usuario/usuario.css");
    }
    
    /**
     * Navega a la vista de crear reserva
     * Carga VistaCrearReserva.fxml y aplica los estilos CSS correspondientes
     */
    public void navegarAVistaCrearReserva() {
        cambiarAVista("/interfaz/sara/Vistas/Usuario/VistaCrearReserva.fxml", "/interfaz/sara/CSS/Usuario/usuario.css");
    }
    
    /**
     * Navega a la vista de crear reserva con datos prellenados
     * 
     * @param salaId ID de la sala seleccionada
     * @param hora Hora seleccionada (formato HH:mm)
     * @param fecha Fecha seleccionada
     */
    public void navegarAVistaCrearReserva(Long salaId, String hora, java.time.LocalDate fecha) {
        establecerDatosReserva(salaId, hora, fecha);
        navegarAVistaCrearReserva();
    }
    
    // ========== Métodos de gestión de datos temporales ==========
    
    /**
     * Establece los datos de reserva para pasar a la vista de crear reserva
     * 
     * @param salaId ID de la sala
     * @param hora Hora seleccionada (formato HH:mm)
     * @param fecha Fecha seleccionada
     */
    public void establecerDatosReserva(Long salaId, String hora, java.time.LocalDate fecha) {
        salaIdSeleccionada = salaId;
        horaSeleccionada = hora;
        fechaSeleccionada = fecha;
    }
    
    /**
     * Obtiene el ID de la sala seleccionada
     * 
     * @return ID de la sala o null
     */
    public Long obtenerSalaIdSeleccionada() {
        return salaIdSeleccionada;
    }
    
    /**
     * Obtiene la hora seleccionada
     * 
     * @return Hora en formato HH:mm o null
     */
    public String obtenerHoraSeleccionada() {
        return horaSeleccionada;
    }
    
    /**
     * Obtiene la fecha seleccionada
     * 
     * @return Fecha o null
     */
    public java.time.LocalDate obtenerFechaSeleccionada() {
        return fechaSeleccionada;
    }
    
    /**
     * Limpia los datos de reserva temporales
     */
    public void limpiarDatosReserva() {
        salaIdSeleccionada = null;
        horaSeleccionada = null;
        fechaSeleccionada = null;
    }
    
    /**
     * Navega a la vista de reporte
     * Carga VistaReporte.fxml y aplica los estilos CSS correspondientes
     */
    public void navegarAVistaReporte() {
        cambiarAVista("/interfaz/sara/Vistas/Usuario/VistaReporte.fxml", "/interfaz/sara/CSS/Usuario/usuario.css");
    }
    
    /**
     * Navega a la vista de perfil del usuario
     * Carga VistaPerfil.fxml y aplica los estilos CSS correspondientes
     */
    public void navegarAVistaPerfil() {
        cambiarAVista("/interfaz/sara/Vistas/Usuario/VistaPerfil.fxml", "/interfaz/sara/CSS/Usuario/usuario.css");
    }
    
    /**
     * Navega a la vista de notificaciones del usuario
     * Carga VistaNotificaciones.fxml y aplica los estilos CSS correspondientes
     */
    public void navegarAVistaNotificaciones() {
        cambiarAVista("/interfaz/sara/Vistas/Usuario/VistaNotificaciones.fxml", "/interfaz/sara/CSS/Usuario/usuario.css");
    }
    
    /**
     * Navega al login (para cerrar sesión)
     */
    public void navegarALogin() {
        gestorBase.navegarALogin();
    }
    
    // ========== Métodos de utilidad ==========
    
    /**
     * Método genérico para cambiar a una vista específica de usuario
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

