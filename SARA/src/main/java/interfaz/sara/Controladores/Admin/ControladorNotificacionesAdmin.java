package interfaz.sara.Controladores.Admin;

import interfaz.sara.ConexionBD.ConexionBD;
import interfaz.sara.Utilidades.GestorNavegacion;
import interfaz.sara.Utilidades.SesionUsuario;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Controlador para la vista de notificaciones del administrador (VistaNotificacionesAdmin.fxml)
 * Gestiona la visualización de notificaciones del administrador
 */
public class ControladorNotificacionesAdmin {

    // ========== Componentes FXML ==========
    
    @FXML
    private VBox sidebarInclude;
    
    @FXML
    private VBox contenedorNotificaciones;

    // ========== Variables de estado ==========
    
    /** Lista de notificaciones */
    private List<NotificacionInfo> listaNotificaciones;
    
    /** Formateador de fecha y hora */
    private DateTimeFormatter formateadorFecha = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // ========== Clase interna para notificaciones ==========
    
    /**
     * Clase que representa la información de una notificación
     */
    private static class NotificacionInfo {
        private Long id;
        private String tipo;
        private String titulo;
        private String mensaje;
        private LocalDateTime fechaCreacion;
        private boolean leida;
        private LocalDateTime fechaLectura;
        
        public NotificacionInfo(Long id, String tipo, String titulo, String mensaje, 
                               LocalDateTime fechaCreacion, boolean leida, LocalDateTime fechaLectura) {
            this.id = id;
            this.tipo = tipo;
            this.titulo = titulo;
            this.mensaje = mensaje;
            this.fechaCreacion = fechaCreacion;
            this.leida = leida;
            this.fechaLectura = fechaLectura;
        }
        
        public Long getId() { return id; }
        public String getTipo() { return tipo; }
        public String getTitulo() { return titulo; }
        public String getMensaje() { return mensaje; }
        public LocalDateTime getFechaCreacion() { return fechaCreacion; }
        public boolean isLeida() { return leida; }
        public LocalDateTime getFechaLectura() { return fechaLectura; }
    }

    // ========== Métodos de inicialización ==========
    
    /**
     * Inicializa el controlador después de que se carga el FXML
     */
    @FXML
    private void initialize() {
        // Verificar que el usuario es admin
        SesionUsuario sesion = SesionUsuario.obtenerInstancia();
        if (!sesion.estaAutenticado() || !sesion.esAdmin()) {
            // Redirigir inmediatamente sin mostrar alerta para evitar cruce de pantallas
            Platform.runLater(() -> {
                GestorNavegacion gestor = GestorNavegacion.obtenerInstancia();
                if (!sesion.estaAutenticado()) {
                    gestor.navegarALogin();
                } else {
                    // Si está autenticado pero no es admin, redirigir a vista de usuario
                    gestor.navegarAVistaPrincipalUsuario();
                }
            });
            return;
        }
        
        // Inicializar lista
        listaNotificaciones = new ArrayList<>();
        
        // Cargar notificaciones desde la base de datos
        cargarNotificaciones();
        
        // Configurar el sidebar
        Platform.runLater(() -> {
            configurarSidebar();
        });
    }
    
    /**
     * Configura el sidebar para resaltar el botón de notificaciones como activo
     */
    private void configurarSidebar() {
        try {
            if (sidebarInclude != null) {
                Button btnUsuarios = (Button) sidebarInclude.lookup("#botonUsuarios");
                Button btnSalas = (Button) sidebarInclude.lookup("#botonSalas");
                Button btnReservas = (Button) sidebarInclude.lookup("#botonReservas");
                Button btnReportes = (Button) sidebarInclude.lookup("#botonReportes");
                Button btnPerfil = (Button) sidebarInclude.lookup("#botonPerfil");
                Button btnNotificaciones = (Button) sidebarInclude.lookup("#botonNotificaciones");
                
                if (btnNotificaciones != null) {
                    // Resetear todos
                    resetearBotonesSidebar(btnUsuarios, btnSalas, btnReservas, btnReportes, btnPerfil);
                    
                    // Activar notificaciones
                    btnNotificaciones.getStyleClass().remove("sidebar-button");
                    if (!btnNotificaciones.getStyleClass().contains("sidebar-button-active")) {
                        btnNotificaciones.getStyleClass().add("sidebar-button-active");
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
     * Carga las notificaciones del admin: reservas pendientes e incidentes recientes
     */
    private void cargarNotificaciones() {
        listaNotificaciones.clear();
        
        // Cargar reservas pendientes
        cargarReservasPendientes();
        
        // Cargar incidentes recientes
        cargarIncidentesRecientes();
        
        // Ordenar por fecha de creación (más recientes primero)
        listaNotificaciones.sort((a, b) -> {
            if (a.getFechaCreacion() == null && b.getFechaCreacion() == null) return 0;
            if (a.getFechaCreacion() == null) return 1;
            if (b.getFechaCreacion() == null) return -1;
            return b.getFechaCreacion().compareTo(a.getFechaCreacion());
        });
        
        // Actualizar visualización
        actualizarVisualizacionNotificaciones();
    }
    
    /**
     * Carga las reservas pendientes como notificaciones
     */
    private void cargarReservasPendientes() {
        ConexionBD conexionBD = ConexionBD.obtenerInstancia();
        
        // Obtener reservas con estado PENDING
        String sql = "SELECT r.id, r.room_id, ro.name as nombre_sala, r.user_id, " +
                    "u.username as nombre_usuario, r.start_at, r.end_at, r.reason, r.created_at " +
                    "FROM reservations r " +
                    "INNER JOIN rooms ro ON r.room_id = ro.id " +
                    "INNER JOIN users u ON r.user_id = u.id " +
                    "INNER JOIN reservation_status rs ON r.status_id = rs.id " +
                    "WHERE rs.code = 'PENDING' " +
                    "ORDER BY r.created_at DESC";
        
        try {
            Connection conexion = conexionBD.obtenerConexion();
            try (PreparedStatement statement = conexion.prepareStatement(sql)) {
                try (ResultSet resultado = statement.executeQuery()) {
                    while (resultado.next()) {
                        Long id = resultado.getLong("id");
                        String nombreSala = resultado.getString("nombre_sala");
                        String nombreUsuario = resultado.getString("nombre_usuario");
                        String motivo = resultado.getString("reason");
                        
                        LocalDateTime fechaCreacion = null;
                        if (resultado.getTimestamp("created_at") != null) {
                            fechaCreacion = resultado.getTimestamp("created_at").toLocalDateTime();
                        }
                        
                        LocalDateTime fechaInicio = null;
                        if (resultado.getTimestamp("start_at") != null) {
                            fechaInicio = resultado.getTimestamp("start_at").toLocalDateTime();
                        }
                        
                        // Crear mensaje de notificación
                        String titulo = "Nueva Reserva Pendiente";
                        String mensaje = String.format("El usuario %s ha solicitado reservar la sala %s. ", nombreUsuario, nombreSala);
                        if (fechaInicio != null) {
                            mensaje += String.format("Fecha: %s. ", fechaInicio.format(formateadorFecha));
                        }
                        if (motivo != null && !motivo.trim().isEmpty()) {
                            mensaje += String.format("Motivo: %s", motivo);
                        }
                        
                        // Usar ID negativo para distinguir de notificaciones normales
                        NotificacionInfo notificacion = new NotificacionInfo(
                            -id, "Reserva Pendiente", titulo, mensaje, fechaCreacion, false, null
                        );
                        
                        listaNotificaciones.add(notificacion);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al cargar reservas pendientes: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Carga los incidentes recientes como notificaciones
     */
    private void cargarIncidentesRecientes() {
        ConexionBD conexionBD = ConexionBD.obtenerInstancia();
        
        // Obtener incidentes recientes (últimos 30 días)
        String sql = "SELECT i.id, i.room_id, ro.name as nombre_sala, i.reported_by_user_id, " +
                    "u.username as nombre_usuario, it.name as tipo_incidente, " +
                    "i.title, i.description, i.created_at " +
                    "FROM incidents i " +
                    "INNER JOIN rooms ro ON i.room_id = ro.id " +
                    "INNER JOIN users u ON i.reported_by_user_id = u.id " +
                    "INNER JOIN incident_types it ON i.incident_type_id = it.id " +
                    "WHERE i.created_at >= DATE_SUB(NOW(), INTERVAL 30 DAY) " +
                    "ORDER BY i.created_at DESC " +
                    "LIMIT 50";
        
        try {
            Connection conexion = conexionBD.obtenerConexion();
            try (PreparedStatement statement = conexion.prepareStatement(sql)) {
                try (ResultSet resultado = statement.executeQuery()) {
                    while (resultado.next()) {
                        Long id = resultado.getLong("id");
                        String nombreSala = resultado.getString("nombre_sala");
                        String nombreUsuario = resultado.getString("nombre_usuario");
                        String tipoIncidente = resultado.getString("tipo_incidente");
                        String titulo = resultado.getString("title");
                        String descripcion = resultado.getString("description");
                        
                        LocalDateTime fechaCreacion = null;
                        if (resultado.getTimestamp("created_at") != null) {
                            fechaCreacion = resultado.getTimestamp("created_at").toLocalDateTime();
                        }
                        
                        // Crear mensaje de notificación
                        String tituloNotificacion = "Nuevo Reporte de Incidente";
                        String mensaje = String.format("El usuario %s ha reportado un incidente en la sala %s. ", nombreUsuario, nombreSala);
                        mensaje += String.format("Tipo: %s. ", tipoIncidente);
                        if (titulo != null && !titulo.trim().isEmpty()) {
                            mensaje += String.format("%s. ", titulo);
                        }
                        if (descripcion != null && !descripcion.trim().isEmpty()) {
                            mensaje += descripcion.length() > 100 ? descripcion.substring(0, 100) + "..." : descripcion;
                        }
                        
                        // Usar ID con offset para distinguir de reservas
                        NotificacionInfo notificacion = new NotificacionInfo(
                            id + 1000000, "Incidente", tituloNotificacion, mensaje, fechaCreacion, false, null
                        );
                        
                        listaNotificaciones.add(notificacion);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al cargar incidentes recientes: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Actualiza la visualización de las notificaciones
     */
    private void actualizarVisualizacionNotificaciones() {
        contenedorNotificaciones.getChildren().clear();
        
        if (listaNotificaciones.isEmpty()) {
            Label mensajeVacio = new Label("No hay notificaciones disponibles.");
            mensajeVacio.getStyleClass().add("mensaje-vacio");
            contenedorNotificaciones.getChildren().add(mensajeVacio);
            return;
        }
        
        for (NotificacionInfo notificacion : listaNotificaciones) {
            VBox tarjetaNotificacion = crearTarjetaNotificacion(notificacion);
            contenedorNotificaciones.getChildren().add(tarjetaNotificacion);
        }
    }
    
    /**
     * Crea una tarjeta visual para una notificación
     * 
     * @param notificacion La notificación a mostrar
     * @return VBox con la tarjeta de la notificación
     */
    private VBox crearTarjetaNotificacion(NotificacionInfo notificacion) {
        VBox tarjeta = new VBox();
        tarjeta.getStyleClass().add("tarjeta-notificacion-admin");
        tarjeta.setSpacing(0);
        tarjeta.setPadding(new Insets(0));
        tarjeta.setPrefWidth(Region.USE_COMPUTED_SIZE);
        tarjeta.setMaxWidth(Double.MAX_VALUE);
        
        // Contenedor de información
        VBox infoContainer = new VBox(12);
        infoContainer.setPadding(new Insets(20));
        infoContainer.setSpacing(12);
        infoContainer.setPrefWidth(Region.USE_COMPUTED_SIZE);
        infoContainer.setStyle("-fx-background-color: #ffffff;");
        
        // Encabezado con tipo y fecha
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        
        Label tipoNotificacion = new Label(notificacion.getTipo() != null ? notificacion.getTipo() : "Notificación");
        tipoNotificacion.getStyleClass().add("tarjeta-notificacion-tipo");
        
        HBox spacer = new HBox();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
        
        Label fecha = new Label(
            notificacion.getFechaCreacion() != null ? 
            notificacion.getFechaCreacion().format(formateadorFecha) : "Fecha no disponible"
        );
        fecha.getStyleClass().add("tarjeta-notificacion-fecha");
        
        // Badge de leída/no leída
        Label badgeEstado = new Label(notificacion.isLeida() ? "✓ Leída" : "● No leída");
        badgeEstado.getStyleClass().add(
            notificacion.isLeida() ? "badge-notificacion-leida" : "badge-notificacion-no-leida"
        );
        
        header.getChildren().addAll(tipoNotificacion, spacer, fecha, badgeEstado);
        
        // Separador visual
        javafx.scene.shape.Line separador = new javafx.scene.shape.Line();
        separador.setStartX(0);
        separador.setEndX(800);
        separador.setStroke(javafx.scene.paint.Color.web("#e2e8f0"));
        separador.setStrokeWidth(1);
        
        // Título
        Label titulo = new Label(notificacion.getTitulo() != null ? notificacion.getTitulo() : "Sin título");
        titulo.getStyleClass().add("tarjeta-notificacion-titulo");
        
        // Mensaje
        Label mensaje = new Label(notificacion.getMensaje() != null ? notificacion.getMensaje() : "Sin mensaje");
        mensaje.getStyleClass().add("tarjeta-notificacion-mensaje");
        mensaje.setWrapText(true);
        
        // Agregar todos los elementos a la tarjeta
        infoContainer.getChildren().addAll(header, separador, titulo, mensaje);
        
        tarjeta.getChildren().add(infoContainer);
        
        // Si no está leída, marcarla como leída al hacer clic
        if (!notificacion.isLeida()) {
            tarjeta.setOnMouseClicked(e -> marcarComoLeida(notificacion));
            tarjeta.setStyle("-fx-cursor: hand;");
        }
        
        return tarjeta;
    }
    
    /**
     * Marca una notificación como leída
     * Si es una reserva, navega a la vista de detalle de reserva
     * Si es un incidente, puede navegar a la vista de reportes
     * 
     * @param notificacion La notificación a marcar como leída
     */
    private void marcarComoLeida(NotificacionInfo notificacion) {
        Long id = notificacion.getId();
        
        // Si es una reserva (ID negativo), navegar a detalle de reserva
        if (id < 0) {
            Long reservaId = -id;
            GestorNavegacion gestor = GestorNavegacion.obtenerInstancia();
            gestor.navegarAVistaDetalleReservaAdmin(reservaId);
            return;
        }
        
        // Si es un incidente (ID con offset), navegar a reportes
        if (id > 1000000) {
            // Navegar a la vista de reportes (el admin puede filtrar por incidente si es necesario)
            GestorNavegacion gestor = GestorNavegacion.obtenerInstancia();
            gestor.navegarAVistaReportesAdmin();
            return;
        }
        
        // Para notificaciones normales de la tabla notifications (si existen)
        ConexionBD conexionBD = ConexionBD.obtenerInstancia();
        String sql = "UPDATE notifications SET delivered = 1, sent_at = NOW() WHERE id = ?";
        
        try {
            Connection conexion = conexionBD.obtenerConexion();
            try (PreparedStatement statement = conexion.prepareStatement(sql)) {
                statement.setLong(1, id);
                
                int filasAfectadas = statement.executeUpdate();
                
                if (filasAfectadas > 0) {
                    // Recargar notificaciones para actualizar el estado
                    cargarNotificaciones();
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al marcar notificación como leída: " + e.getMessage());
        }
    }
    
    /**
     * Muestra un mensaje de error
     * 
     * @param mensaje El mensaje a mostrar
     */
    private void mostrarMensajeError(String mensaje) {
        Label mensajeError = new Label(mensaje);
        mensajeError.getStyleClass().add("mensaje-error");
        contenedorNotificaciones.getChildren().clear();
        contenedorNotificaciones.getChildren().add(mensajeError);
    }
}

