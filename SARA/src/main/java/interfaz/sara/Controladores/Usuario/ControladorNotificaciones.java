package interfaz.sara.Controladores.Usuario;

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
 * Controlador para la vista de notificaciones del usuario (VistaNotificaciones.fxml)
 * Gestiona la visualización de notificaciones del usuario
 */
public class ControladorNotificaciones {

    // ========== Componentes FXML ==========
    
    @FXML
    private VBox navbarInclude;
    
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
    }

    // ========== Métodos de inicialización ==========
    
    /**
     * Inicializa el controlador después de que se carga el FXML
     */
    @FXML
    private void initialize() {
        // Verificar autenticación
        SesionUsuario sesion = SesionUsuario.obtenerInstancia();
        if (!sesion.estaAutenticado()) {
            Platform.runLater(() -> {
                GestorNavegacion gestor = GestorNavegacion.obtenerInstancia();
                gestor.navegarALogin();
            });
            return;
        }
        
        // Inicializar lista
        listaNotificaciones = new ArrayList<>();
        
        // Cargar notificaciones desde la base de datos
        cargarNotificaciones();
        
        // Configurar el navbar
        Platform.runLater(() -> {
            configurarNavbar();
        });
    }
    
    /**
     * Configura el navbar para resaltar el botón de notificaciones como activo
     */
    private void configurarNavbar() {
        try {
            if (navbarInclude != null) {
                Button btnMisReservas = (Button) navbarInclude.lookup("#botonMisReservas");
                Button btnNuevaReserva = (Button) navbarInclude.lookup("#botonNuevaReserva");
                Button btnReporte = (Button) navbarInclude.lookup("#botonReporte");
                Button btnNotificaciones = (Button) navbarInclude.lookup("#botonNotificaciones");
                Button btnPerfil = (Button) navbarInclude.lookup("#botonPerfil");
                
                if (btnNotificaciones != null) {
                    // Resetear todos
                    if (btnMisReservas != null) {
                        btnMisReservas.getStyleClass().remove("navbar-button-active");
                        if (!btnMisReservas.getStyleClass().contains("navbar-button")) {
                            btnMisReservas.getStyleClass().add("navbar-button");
                        }
                    }
                    if (btnNuevaReserva != null) {
                        btnNuevaReserva.getStyleClass().remove("navbar-button-active");
                        if (!btnNuevaReserva.getStyleClass().contains("navbar-button")) {
                            btnNuevaReserva.getStyleClass().add("navbar-button");
                        }
                    }
                    if (btnReporte != null) {
                        btnReporte.getStyleClass().remove("navbar-button-active");
                        if (!btnReporte.getStyleClass().contains("navbar-button")) {
                            btnReporte.getStyleClass().add("navbar-button");
                        }
                    }
                    if (btnPerfil != null) {
                        btnPerfil.getStyleClass().remove("navbar-button-active");
                        btnPerfil.getStyleClass().remove("active");
                        if (!btnPerfil.getStyleClass().contains("navbar-button-profile")) {
                            btnPerfil.getStyleClass().add("navbar-button-profile");
                        }
                    }
                    
                    // Activar notificaciones
                    btnNotificaciones.getStyleClass().remove("navbar-button");
                    if (!btnNotificaciones.getStyleClass().contains("navbar-button-active")) {
                        btnNotificaciones.getStyleClass().add("navbar-button-active");
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error al configurar navbar: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ========== Métodos de carga de datos ==========
    
    /**
     * Carga las notificaciones del usuario: reservas aceptadas, rechazadas y recordatorios
     */
    private void cargarNotificaciones() {
        listaNotificaciones.clear();
        
        // Cargar reservas aceptadas y rechazadas
        cargarReservasAceptadasRechazadas();
        
        // Cargar recordatorios de reservaciones
        cargarRecordatoriosReservaciones();
        
        // Cargar notificaciones de cancelación de reservas
        cargarNotificacionesCancelacion();
        
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
     * Carga las reservas aceptadas (CONFIRMED) o rechazadas (CANCELLED_ADMIN) del usuario
     */
    private void cargarReservasAceptadasRechazadas() {
        ConexionBD conexionBD = ConexionBD.obtenerInstancia();
        SesionUsuario sesion = SesionUsuario.obtenerInstancia();
        
        if (!sesion.estaAutenticado()) {
            return;
        }
        
        Long usuarioId = sesion.getUsuarioId();
        
        // Crear tabla si no existe
        crearTablaSiNoExiste();
        
        // Obtener reservas con estado CONFIRMED o CANCELLED_ADMIN
        // Verificar si ya fueron leídas
        String sql = "SELECT r.id, r.room_id, ro.name as nombre_sala, r.user_id, " +
                    "r.start_at, r.end_at, r.reason, r.cancellation_reason, " +
                    "rs.code as codigo_estado, rs.label as estado_reserva, r.created_at, " +
                    "CASE WHEN nr.id IS NOT NULL THEN 1 ELSE 0 END as leida, " +
                    "nr.read_at as fecha_lectura " +
                    "FROM reservations r " +
                    "INNER JOIN rooms ro ON r.room_id = ro.id " +
                    "INNER JOIN reservation_status rs ON r.status_id = rs.id " +
                    "LEFT JOIN notification_reads nr ON nr.user_id = r.user_id " +
                    "    AND nr.notification_type = 'RESERVATION' " +
                    "    AND nr.entity_id = r.id " +
                    "WHERE r.user_id = ? " +
                    "AND (rs.code = 'CONFIRMED' OR rs.code = 'CANCELLED_ADMIN') " +
                    "ORDER BY r.created_at DESC";
        
        try {
            Connection conexion = conexionBD.obtenerConexion();
            try (PreparedStatement statement = conexion.prepareStatement(sql)) {
                statement.setLong(1, usuarioId);
                
                try (ResultSet resultado = statement.executeQuery()) {
                    while (resultado.next()) {
                        Long id = resultado.getLong("id");
                        String nombreSala = resultado.getString("nombre_sala");
                        String codigoEstado = resultado.getString("codigo_estado");
                        String motivo = resultado.getString("reason");
                        String motivoCancelacion = resultado.getString("cancellation_reason");
                        
                        LocalDateTime fechaCreacion = null;
                        if (resultado.getTimestamp("created_at") != null) {
                            fechaCreacion = resultado.getTimestamp("created_at").toLocalDateTime();
                        }
                        
                        LocalDateTime fechaInicio = null;
                        if (resultado.getTimestamp("start_at") != null) {
                            fechaInicio = resultado.getTimestamp("start_at").toLocalDateTime();
                        }
                        
                        // Verificar si está leída
                        boolean leida = resultado.getInt("leida") == 1;
                        LocalDateTime fechaLectura = null;
                        if (resultado.getTimestamp("fecha_lectura") != null) {
                            fechaLectura = resultado.getTimestamp("fecha_lectura").toLocalDateTime();
                        }
                        
                        // Crear mensaje de notificación según el estado
                        String titulo;
                        String mensaje;
                        String tipo;
                        
                        if ("CONFIRMED".equals(codigoEstado)) {
                            tipo = "Reserva Aceptada";
                            titulo = "¡Reserva Aceptada!";
                            mensaje = String.format("Tu reserva de la sala %s ha sido aceptada. ", nombreSala);
                            if (fechaInicio != null) {
                                mensaje += String.format("Fecha: %s. ", fechaInicio.format(formateadorFecha));
                            }
                            if (motivo != null && !motivo.trim().isEmpty()) {
                                mensaje += String.format("Motivo: %s", motivo);
                            }
                        } else { // CANCELLED_ADMIN
                            tipo = "Reserva Rechazada";
                            titulo = "Reserva Rechazada";
                            mensaje = String.format("Tu reserva de la sala %s ha sido rechazada. ", nombreSala);
                            if (fechaInicio != null) {
                                mensaje += String.format("Fecha solicitada: %s. ", fechaInicio.format(formateadorFecha));
                            }
                            if (motivoCancelacion != null && !motivoCancelacion.trim().isEmpty()) {
                                mensaje += String.format("Motivo: %s", motivoCancelacion);
                            } else {
                                mensaje += "Contacta al administrador para más información.";
                            }
                        }
                        
                        // Usar ID negativo para distinguir de notificaciones normales
                        NotificacionInfo notificacion = new NotificacionInfo(
                            -id, tipo, titulo, mensaje, fechaCreacion, leida, fechaLectura
                        );
                        
                        listaNotificaciones.add(notificacion);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al cargar reservas aceptadas/rechazadas: " + e.getMessage());
            e.printStackTrace();
            mostrarMensajeError("Error al cargar las notificaciones. Por favor, intente más tarde.");
        }
    }
    
    /**
     * Carga los recordatorios de reservaciones programados que ya deben mostrarse
     */
    private void cargarRecordatoriosReservaciones() {
        ConexionBD conexionBD = ConexionBD.obtenerInstancia();
        SesionUsuario sesion = SesionUsuario.obtenerInstancia();
        
        if (!sesion.estaAutenticado()) {
            return;
        }
        
        Long usuarioId = sesion.getUsuarioId();
        
        // Crear tabla si no existe
        crearTablaSiNoExiste();
        
        // Obtener recordatorios que ya deben mostrarse (sent_at <= NOW() y delivered = 0)
        String sql = "SELECT n.id, n.reservation_id, n.message, n.sent_at, n.created_at, " +
                    "r.start_at, ro.name as nombre_sala, " +
                    "CASE WHEN nr.id IS NOT NULL THEN 1 ELSE 0 END as leida, " +
                    "nr.read_at as fecha_lectura " +
                    "FROM notifications n " +
                    "INNER JOIN notification_types nt ON n.type_id = nt.id " +
                    "LEFT JOIN reservations r ON n.reservation_id = r.id " +
                    "LEFT JOIN rooms ro ON r.room_id = ro.id " +
                    "LEFT JOIN notification_reads nr ON nr.user_id = n.user_id " +
                    "    AND nr.notification_type = 'NOTIFICATION' " +
                    "    AND nr.entity_id = n.id " +
                    "WHERE n.user_id = ? " +
                    "AND nt.code = 'RESERVATION_REMINDER' " +
                    "AND n.delivered = 0 " +
                    "AND n.sent_at <= NOW() " +
                    "ORDER BY n.sent_at DESC";
        
        try {
            Connection conexion = conexionBD.obtenerConexion();
            try (PreparedStatement statement = conexion.prepareStatement(sql)) {
                statement.setLong(1, usuarioId);
                
                try (ResultSet resultado = statement.executeQuery()) {
                    while (resultado.next()) {
                        Long id = resultado.getLong("id");
                        String mensaje = resultado.getString("message");
                        String nombreSala = resultado.getString("nombre_sala");
                        
                        LocalDateTime fechaCreacion = null;
                        if (resultado.getTimestamp("created_at") != null) {
                            fechaCreacion = resultado.getTimestamp("created_at").toLocalDateTime();
                        }
                        
                        LocalDateTime fechaEnvio = null;
                        if (resultado.getTimestamp("sent_at") != null) {
                            fechaEnvio = resultado.getTimestamp("sent_at").toLocalDateTime();
                        }
                        
                        // Verificar si está leída
                        boolean leida = resultado.getInt("leida") == 1;
                        LocalDateTime fechaLectura = null;
                        if (resultado.getTimestamp("fecha_lectura") != null) {
                            fechaLectura = resultado.getTimestamp("fecha_lectura").toLocalDateTime();
                        }
                        
                        // Marcar como entregada
                        marcarRecordatorioComoEntregado(id, conexion);
                        
                        // Crear notificación de recordatorio
                        String titulo = "Recordatorio de Reserva";
                        String tipo = "Recordatorio";
                        
                        // Usar el ID positivo de la notificación
                        NotificacionInfo notificacion = new NotificacionInfo(
                            id, tipo, titulo, mensaje, 
                            fechaEnvio != null ? fechaEnvio : fechaCreacion, 
                            leida, fechaLectura
                        );
                        
                        listaNotificaciones.add(notificacion);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al cargar recordatorios: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Marca un recordatorio como entregado
     * 
     * @param notificacionId ID de la notificación
     * @param conexion Conexión a la base de datos
     */
    private void marcarRecordatorioComoEntregado(Long notificacionId, Connection conexion) {
        try {
            String sql = "UPDATE notifications SET delivered = 1 WHERE id = ?";
            try (PreparedStatement statement = conexion.prepareStatement(sql)) {
                statement.setLong(1, notificacionId);
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            System.err.println("Error al marcar recordatorio como entregado: " + e.getMessage());
        }
    }
    
    /**
     * Carga las notificaciones de cancelación de reservas (RESERVATION_CANCELLED)
     */
    private void cargarNotificacionesCancelacion() {
        ConexionBD conexionBD = ConexionBD.obtenerInstancia();
        SesionUsuario sesion = SesionUsuario.obtenerInstancia();
        
        if (!sesion.estaAutenticado()) {
            return;
        }
        
        Long usuarioId = sesion.getUsuarioId();
        
        // Crear tabla si no existe
        crearTablaSiNoExiste();
        
        // Obtener notificaciones de cancelación
        String sql = "SELECT n.id, n.message, n.created_at, n.sent_at, " +
                    "CASE WHEN nr.id IS NOT NULL THEN 1 ELSE 0 END as leida, " +
                    "nr.read_at as fecha_lectura " +
                    "FROM notifications n " +
                    "INNER JOIN notification_types nt ON n.type_id = nt.id " +
                    "LEFT JOIN notification_reads nr ON nr.user_id = n.user_id " +
                    "    AND nr.notification_type = 'NOTIFICATION' " +
                    "    AND nr.entity_id = n.id " +
                    "WHERE n.user_id = ? " +
                    "AND nt.code = 'RESERVATION_CANCELLED' " +
                    "ORDER BY n.created_at DESC";
        
        try {
            Connection conexion = conexionBD.obtenerConexion();
            try (PreparedStatement statement = conexion.prepareStatement(sql)) {
                statement.setLong(1, usuarioId);
                
                try (ResultSet resultado = statement.executeQuery()) {
                    while (resultado.next()) {
                        Long id = resultado.getLong("id");
                        String mensaje = resultado.getString("message");
                        
                        LocalDateTime fechaCreacion = null;
                        if (resultado.getTimestamp("created_at") != null) {
                            fechaCreacion = resultado.getTimestamp("created_at").toLocalDateTime();
                        }
                        
                        LocalDateTime fechaEnvio = null;
                        if (resultado.getTimestamp("sent_at") != null) {
                            fechaEnvio = resultado.getTimestamp("sent_at").toLocalDateTime();
                        }
                        
                        // Verificar si está leída
                        boolean leida = resultado.getInt("leida") == 1;
                        LocalDateTime fechaLectura = null;
                        if (resultado.getTimestamp("fecha_lectura") != null) {
                            fechaLectura = resultado.getTimestamp("fecha_lectura").toLocalDateTime();
                        }
                        
                        // Crear notificación de cancelación
                        String titulo = "Reserva Cancelada";
                        String tipo = "Reserva Cancelada";
                        
                        // Usar el ID positivo de la notificación
                        NotificacionInfo notificacion = new NotificacionInfo(
                            id, tipo, titulo, mensaje, 
                            fechaEnvio != null ? fechaEnvio : fechaCreacion, 
                            leida, fechaLectura
                        );
                        
                        listaNotificaciones.add(notificacion);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al cargar notificaciones de cancelación: " + e.getMessage());
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
        tarjeta.getStyleClass().add("tarjeta-notificacion");
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
        
        // Permitir hacer clic en todas las notificaciones para marcarlas como leídas
        tarjeta.setOnMouseClicked(e -> marcarComoLeida(notificacion));
        if (!notificacion.isLeida()) {
            tarjeta.setStyle("-fx-cursor: hand;");
        }
        
        return tarjeta;
    }
    
    /**
     * Marca una notificación como leída en la base de datos
     * 
     * @param notificacion La notificación a marcar como leída
     */
    private void marcarComoLeida(NotificacionInfo notificacion) {
        // Si ya está leída, no hacer nada
        if (notificacion.isLeida()) {
            return;
        }
        
        Long id = notificacion.getId();
        SesionUsuario sesion = SesionUsuario.obtenerInstancia();
        
        if (!sesion.estaAutenticado()) {
            return;
        }
        
        Long usuarioId = sesion.getUsuarioId();
        
        // Si es una reserva (ID negativo)
        if (id < 0) {
            Long reservaId = -id;
            ConexionBD conexionBD = ConexionBD.obtenerInstancia();
            
            // Insertar o actualizar registro de lectura
            String sql = "INSERT INTO notification_reads (user_id, notification_type, entity_id, read_at) " +
                        "VALUES (?, 'RESERVATION', ?, NOW()) " +
                        "ON DUPLICATE KEY UPDATE read_at = NOW()";
            
            try {
                Connection conexion = conexionBD.obtenerConexion();
                try (PreparedStatement statement = conexion.prepareStatement(sql)) {
                    statement.setLong(1, usuarioId);
                    statement.setLong(2, reservaId);
                    
                    statement.executeUpdate();
                    
                    // Recargar notificaciones para actualizar el estado visual
                    cargarNotificaciones();
                }
            } catch (SQLException e) {
                System.err.println("Error al marcar notificación como leída: " + e.getMessage());
                e.printStackTrace();
            }
            return;
        }
        
        // Para notificaciones de la tabla notifications (recordatorios, etc.)
        ConexionBD conexionBD = ConexionBD.obtenerInstancia();
        
        // Insertar o actualizar registro de lectura
        String sql = "INSERT INTO notification_reads (user_id, notification_type, entity_id, read_at) " +
                    "VALUES (?, 'NOTIFICATION', ?, NOW()) " +
                    "ON DUPLICATE KEY UPDATE read_at = NOW()";
        
        try {
            Connection conexion = conexionBD.obtenerConexion();
            try (PreparedStatement statement = conexion.prepareStatement(sql)) {
                statement.setLong(1, usuarioId);
                statement.setLong(2, id);
                
                statement.executeUpdate();
                
                // Recargar notificaciones para actualizar el estado visual
                cargarNotificaciones();
            }
        } catch (SQLException e) {
            System.err.println("Error al marcar notificación como leída: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Crea la tabla notification_reads si no existe
     */
    private void crearTablaSiNoExiste() {
        ConexionBD conexionBD = ConexionBD.obtenerInstancia();
        String sql = "CREATE TABLE IF NOT EXISTS notification_reads (" +
                    "id bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT, " +
                    "user_id bigint(20) UNSIGNED NOT NULL, " +
                    "notification_type VARCHAR(50) NOT NULL, " +
                    "entity_id bigint(20) UNSIGNED NOT NULL, " +
                    "read_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                    "created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                    "PRIMARY KEY (id), " +
                    "UNIQUE KEY unique_user_notification (user_id, notification_type, entity_id), " +
                    "KEY idx_user_read_at (user_id, read_at), " +
                    "CONSTRAINT fk_notification_reads_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE ON UPDATE CASCADE" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";
        
        try {
            Connection conexion = conexionBD.obtenerConexion();
            try (PreparedStatement statement = conexion.prepareStatement(sql)) {
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            // La tabla ya existe o hay un error, continuar
            System.err.println("Nota: Error al crear tabla notification_reads (puede que ya exista): " + e.getMessage());
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

