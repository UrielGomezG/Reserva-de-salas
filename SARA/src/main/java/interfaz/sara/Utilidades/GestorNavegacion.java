package interfaz.sara.Utilidades;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Gestor de navegación entre vistas de la aplicación SARA
 * Centraliza el cambio de escenas y la carga de recursos
 */
public class GestorNavegacion {
    
    /** Instancia única del gestor (Singleton) */
    private static GestorNavegacion instancia;
    
    /** Referencia al escenario (ventana) principal de la aplicación */
    private Stage escenarioPrincipal;
    
    /** Referencia a la escena actual */
    private Scene escenaActual;
    
    // ========== Constructor privado (Singleton) ==========
    
    /**
     * Constructor privado para prevenir la creación de instancias externas
     */
    private GestorNavegacion() {
    }
    
    // ========== Método para obtener la instancia singleton ==========
    
    /**
     * Obtiene la instancia única de GestorNavegacion (patrón Singleton)
     * 
     * @return La instancia única de GestorNavegacion
     */
    public static GestorNavegacion obtenerInstancia() {
        if (instancia == null) {
            synchronized (GestorNavegacion.class) {
                if (instancia == null) {
                    instancia = new GestorNavegacion();
                }
            }
        }
        return instancia;
    }
    
    // ========== Métodos de configuración ==========
    
    /**
     * Establece el escenario principal de la aplicación
     * Debe ser llamado una vez al iniciar la aplicación
     * 
     * @param escenario El escenario principal
     */
    public void establecerEscenarioPrincipal(Stage escenario) {
        this.escenarioPrincipal = escenario;
        if (escenario.getScene() != null) {
            this.escenaActual = escenario.getScene();
        }
    }
    
    // ========== Métodos de navegación ==========
    
    /**
     * Navega a la vista de login
     * Carga VistaLogin.fxml y aplica los estilos CSS correspondientes
     */
    public void navegarALogin() {
        cambiarAVista("/interfaz/sara/Vistas/VistaLogin.fxml", "/interfaz/sara/CSS/login.css");
    }
    
    /**
     * Navega a la vista de registro
     * Carga VistaRegistro.fxml y aplica los estilos CSS correspondientes
     */
    public void navegarARegistro() {
        cambiarAVista("/interfaz/sara/Vistas/VistaRegistro.fxml", "/interfaz/sara/CSS/login.css");
    }
    
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
    
    // ========== Métodos de navegación Admin ==========
    
    /**
     * Navega a la vista de usuarios del administrador
     * Carga VistaUsuariosAdmin.fxml y aplica los estilos CSS correspondientes
     */
    public void navegarAVistaUsuariosAdmin() {
        cambiarAVista("/interfaz/sara/Vistas/Admin/VistaUsuariosAdmin.fxml", "/interfaz/sara/CSS/Admin/admin.css");
    }
    
    /**
     * ID del usuario seleccionado para ver detalles (temporal)
     */
    private static Long usuarioIdSeleccionado = null;
    
    /**
     * ID de la sala seleccionada para ver detalles (temporal)
     */
    private static Long salaIdSeleccionado = null;
    
    /**
     * ID de la reserva seleccionada para ver detalles (temporal)
     */
    private static Long reservaIdSeleccionado = null;
    
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
     * Método genérico para cambiar a una vista específica
     * Carga el archivo FXML y aplica los estilos CSS indicados
     * 
     * @param rutaFXML Ruta del archivo FXML relativa a los recursos
     * @param rutaCSS Ruta del archivo CSS relativa a los recursos (puede ser null)
     */
    public void cambiarAVista(String rutaFXML, String rutaCSS) {
        if (escenarioPrincipal == null) {
            System.err.println("Error: El escenario principal no ha sido establecido");
            return;
        }
        
        try {
            // Guardar el estado de la ventana antes de cambiar
            boolean estabaMaximizada = escenarioPrincipal.isMaximized();
            double anchoAnterior = escenarioPrincipal.getWidth();
            double altoAnterior = escenarioPrincipal.getHeight();
            
            // Cargar el archivo FXML
            FXMLLoader loader = new FXMLLoader(
                GestorNavegacion.class.getResource(rutaFXML)
            );
            
            Parent root = loader.load();
            
            // Crear o actualizar la escena
            Scene escena;
            if (escenaActual == null) {
                escena = new Scene(root);
            } else {
                // Reutilizar la escena existente si es posible, o crear una nueva con el tamaño anterior
                if (anchoAnterior > 0 && altoAnterior > 0) {
                    escena = new Scene(root, anchoAnterior, altoAnterior);
                } else {
                    escena = new Scene(root);
                }
            }
            
            // Aplicar estilos CSS si se proporcionó una ruta
            if (rutaCSS != null && !rutaCSS.isEmpty()) {
                String css = GestorNavegacion.class.getResource(rutaCSS).toExternalForm();
                escena.getStylesheets().clear(); // Limpiar estilos previos
                escena.getStylesheets().add(css);
            }
            
            // Actualizar la escena en el escenario
            escenarioPrincipal.setScene(escena);
            escenaActual = escena;
            
            // Restaurar el estado maximizado si estaba maximizada
            if (estabaMaximizada) {
                escenarioPrincipal.setMaximized(true);
            }
            
            // Asegurar que la ventana esté visible
            escenarioPrincipal.show();
            
        } catch (IOException e) {
            System.err.println("Error al cargar la vista: " + rutaFXML);
            System.err.println("Mensaje: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Obtiene la referencia al escenario principal
     * 
     * @return El escenario principal de la aplicación
     */
    public Stage obtenerEscenarioPrincipal() {
        return escenarioPrincipal;
    }
    
    /**
     * Obtiene la referencia a la escena actual
     * 
     * @return La escena actualmente activa
     */
    public Scene obtenerEscenaActual() {
        return escenaActual;
    }
}

