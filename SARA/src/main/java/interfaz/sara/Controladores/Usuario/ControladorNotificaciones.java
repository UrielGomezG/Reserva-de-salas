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
     * Carga las notificaciones del usuario: solo reservas aceptadas o rechazadas
     */
    private void cargarNotificaciones() {
        listaNotificaciones.clear();
        
        // Cargar reservas aceptadas y rechazadas
        cargarReservasAceptadasRechazadas();
        
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
        
        // Obtener reservas con estado CONFIRMED o CANCELLED_ADMIN
        String sql = "SELECT r.id, r.room_id, ro.name as nombre_sala, r.user_id, " +
                    "r.start_at, r.end_at, r.reason, r.cancellation_reason, " +
                    "rs.code as codigo_estado, rs.label as estado_reserva, r.created_at " +
                    "FROM reservations r " +
                    "INNER JOIN rooms ro ON r.room_id = ro.id " +
                    "INNER JOIN reservation_status rs ON r.status_id = rs.id " +
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
                        String estadoReserva = resultado.getString("estado_reserva");
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
                        
                        LocalDateTime fechaFin = null;
                        if (resultado.getTimestamp("end_at") != null) {
                            fechaFin = resultado.getTimestamp("end_at").toLocalDateTime();
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
                            -id, tipo, titulo, mensaje, fechaCreacion, false, null
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
        
        // Si no está leída, marcarla como leída al hacer clic
        if (!notificacion.isLeida()) {
            tarjeta.setOnMouseClicked(e -> marcarComoLeida(notificacion));
            tarjeta.setStyle("-fx-cursor: hand;");
        }
        
        return tarjeta;
    }
    
    /**
     * Marca una notificación como leída
     * Si es una reserva (ID negativo), simplemente recarga para que no vuelva a aparecer como "no leída"
     * 
     * @param notificacion La notificación a marcar como leída
     */
    private void marcarComoLeida(NotificacionInfo notificacion) {
        Long id = notificacion.getId();
        
        // Si es una reserva (ID negativo), podemos navegar a Mis Reservas o simplemente marcarla como vista
        // Por ahora, solo recargamos para actualizar la visualización
        if (id < 0) {
            // Opcionalmente, navegar a Mis Reservas
            // GestorNavegacion gestor = GestorNavegacion.obtenerInstancia();
            // gestor.navegarAVistaPrincipalUsuario();
            
            // Recargar para actualizar el estado visual
            cargarNotificaciones();
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

