package interfaz.sara.Controladores.Admin;

import interfaz.sara.ConexionBD.ConexionBD;
import interfaz.sara.Utilidades.GestorNavegacion;
import interfaz.sara.Utilidades.GestorNavegacionAdmin;
import interfaz.sara.Utilidades.SesionUsuario;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.Duration;

/**
 * Controlador para la vista de detalle y autorización de reserva del administrador
 * Permite al admin autorizar o cancelar reservas
 */
public class ControladorDetalleReservaAdmin {

    // ========== Componentes FXML ==========
    
    @FXML
    private VBox sidebarInclude;
    
    @FXML
    private Label lblNombreSala;
    
    @FXML
    private Label lblEstado;
    
    @FXML
    private Label lblEstadoBadge;
    
    @FXML
    private Label lblSolicitadoPor;
    
    @FXML
    private Label lblFechaInicio;
    
    @FXML
    private Label lblFechaFin;
    
    @FXML
    private Label lblCantidadPersonas;
    
    @FXML
    private Label lblMotivo;
    
    @FXML
    private VBox contenedorCancelacion;
    
    @FXML
    private Label lblMotivoCancelacion;
    
    @FXML
    private Button btnAutorizar;
    
    @FXML
    private Button btnCancelar;
    
    @FXML
    private VBox contenedorCancelacionAdmin;
    
    @FXML
    private TextArea campoMotivoCancelacion;
    
    @FXML
    private Label lblMensajeEstado;
    
    // ========== Variables de estado ==========
    
    private Long reservaId;
    
    /** Formateador de fecha y hora */
    private DateTimeFormatter formateadorFecha = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // ========== Métodos de inicialización ==========
    
    @FXML
    private void initialize() {
        // Verificar que el usuario es admin
        SesionUsuario sesion = SesionUsuario.obtenerInstancia();
        if (!sesion.estaAutenticado() || !sesion.esAdmin()) {
            // Redirigir inmediatamente sin mostrar alerta para evitar cruce de pantallas
            Platform.runLater(() -> {
                if (!sesion.estaAutenticado()) {
                    GestorNavegacion gestor = GestorNavegacion.obtenerInstancia();
                    gestor.navegarALogin();
                } else {
                    // Si está autenticado pero no es admin, redirigir a vista de usuario
                    interfaz.sara.Utilidades.GestorNavegacionUsuario gestorUsuario = interfaz.sara.Utilidades.GestorNavegacionUsuario.obtenerInstancia();
                    gestorUsuario.navegarAVistaPrincipalUsuario();
                }
            });
            return;
        }
        
        GestorNavegacionAdmin gestorNavegacion = GestorNavegacionAdmin.obtenerInstancia();
        reservaId = gestorNavegacion.obtenerReservaIdSeleccionado();
        
        if (reservaId == null) {
            mostrarAlerta("Error", "Reserva no seleccionada",
                         "No se ha seleccionado una reserva para ver sus detalles.",
                         Alert.AlertType.ERROR);
            return;
        }
        
        cargarDatosReserva();
        Platform.runLater(this::configurarSidebar);
    }
    
    /**
     * Configura el sidebar para resaltar el botón de reservas como activo
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
                
                if (btnReservas != null) {
                    resetearBotonesSidebar(btnUsuarios, btnSalas, btnReportes, btnPerfil, btnNotificaciones);
                    btnReservas.getStyleClass().remove("sidebar-button");
                    if (!btnReservas.getStyleClass().contains("sidebar-button-active")) {
                        btnReservas.getStyleClass().add("sidebar-button-active");
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
     * Carga los datos de la reserva desde la base de datos
     */
    private void cargarDatosReserva() {
        ConexionBD conexionBD = ConexionBD.obtenerInstancia();
        
        String sql = "SELECT r.id, r.room_id, ro.name as nombre_sala, r.user_id, " +
                    "u.username as nombre_usuario, " +
                    "r.start_at, r.end_at, r.people_count, r.reason, " +
                    "r.status_id, rs.code as codigo_estado, rs.label as estado_reserva, " +
                    "r.cancelled_by_user_id, r.cancellation_reason, r.created_at " +
                    "FROM reservations r " +
                    "INNER JOIN rooms ro ON r.room_id = ro.id " +
                    "INNER JOIN users u ON r.user_id = u.id " +
                    "INNER JOIN reservation_status rs ON r.status_id = rs.id " +
                    "WHERE r.id = ?";
        
        try {
            Connection conexion = conexionBD.obtenerConexion();
            try (PreparedStatement statement = conexion.prepareStatement(sql)) {
                statement.setLong(1, reservaId);
                
                try (ResultSet resultado = statement.executeQuery()) {
                    if (resultado.next()) {
                        // Llenar los campos
                        lblNombreSala.setText(resultado.getString("nombre_sala"));
                        lblSolicitadoPor.setText(resultado.getString("nombre_usuario"));
                        
                        if (resultado.getTimestamp("start_at") != null) {
                            lblFechaInicio.setText(resultado.getTimestamp("start_at").toLocalDateTime().format(formateadorFecha));
                        }
                        
                        if (resultado.getTimestamp("end_at") != null) {
                            lblFechaFin.setText(resultado.getTimestamp("end_at").toLocalDateTime().format(formateadorFecha));
                        }
                        
                        lblCantidadPersonas.setText(String.valueOf(resultado.getInt("people_count")));
                        lblMotivo.setText(resultado.getString("reason") != null ? resultado.getString("reason") : "Sin motivo");
                        
                        String estadoReserva = resultado.getString("estado_reserva");
                        String codigoEstado = resultado.getString("codigo_estado");
                        
                        lblEstado.setText(estadoReserva);
                        lblEstadoBadge.setText(estadoReserva);
                        lblEstadoBadge.getStyleClass().clear();
                        lblEstadoBadge.getStyleClass().add("tarjeta-reserva-estado-" + obtenerClaseEstado(codigoEstado));
                        
                        // Mostrar motivo de cancelación si existe
                        String motivoCancelacion = resultado.getString("cancellation_reason");
                        if (motivoCancelacion != null && !motivoCancelacion.isEmpty()) {
                            contenedorCancelacion.setVisible(true);
                            contenedorCancelacion.setManaged(true);
                            lblMotivoCancelacion.setText(motivoCancelacion);
                        }
                        
                        // Configurar visibilidad de botones según el estado
                        configurarBotonesSegunEstado(codigoEstado);
                    } else {
                        mostrarAlerta("Error", "Reserva no encontrada",
                                     "No se encontró la reserva en la base de datos.",
                                     Alert.AlertType.ERROR);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al cargar datos de la reserva: " + e.getMessage());
            e.printStackTrace();
            mostrarAlerta("Error", "Error al cargar datos",
                         "No se pudieron cargar los datos de la reserva. Por favor, intente más tarde.",
                         Alert.AlertType.ERROR);
        }
    }
    
    /**
     * Configura la visibilidad de los botones según el estado de la reserva
     */
    private void configurarBotonesSegunEstado(String codigoEstado) {
        if (codigoEstado == null) {
            return;
        }
        
        switch (codigoEstado) {
            case "PENDING":
                // Pendiente: puede autorizar o cancelar
                btnAutorizar.setVisible(true);
                btnAutorizar.setManaged(true);
                btnCancelar.setVisible(true);
                btnCancelar.setManaged(true);
                break;
            case "CONFIRMED":
                // Confirmada: solo puede cancelar (con motivo)
                btnAutorizar.setVisible(false);
                btnAutorizar.setManaged(false);
                btnCancelar.setVisible(true);
                btnCancelar.setManaged(true);
                // Asegurar que el contenedor de cancelación esté oculto inicialmente
                contenedorCancelacionAdmin.setVisible(false);
                contenedorCancelacionAdmin.setManaged(false);
                break;
            case "CANCELLED_USER":
            case "CANCELLED_ADMIN":
            case "COMPLETED":
                // Cancelada o completada: no se puede modificar
                btnAutorizar.setVisible(false);
                btnAutorizar.setManaged(false);
                btnCancelar.setVisible(false);
                btnCancelar.setManaged(false);
                contenedorCancelacionAdmin.setVisible(false);
                contenedorCancelacionAdmin.setManaged(false);
                break;
        }
    }
    
    /**
     * Obtiene la clase CSS para el estado de la reserva
     */
    private String obtenerClaseEstado(String codigoEstado) {
        if (codigoEstado == null) return "unknown";
        
        switch (codigoEstado) {
            case "PENDING":
                return "pending";
            case "CONFIRMED":
                return "confirmed";
            case "CANCELLED_USER":
            case "CANCELLED_ADMIN":
                return "cancelled";
            case "COMPLETED":
                return "completed";
            default:
                return "unknown";
        }
    }

    // ========== Métodos de manejo de eventos ==========
    
    /**
     * Maneja el evento de volver a la lista de reservas
     */
    @FXML
    private void manejarVolver() {
        GestorNavegacionAdmin gestorNavegacion = GestorNavegacionAdmin.obtenerInstancia();
        gestorNavegacion.navegarAVistaReservasAdmin();
    }
    
    /**
     * Maneja el evento de autorizar la reserva
     */
    @FXML
    private void manejarAutorizar() {
        // Confirmar autorización
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar Autorización");
        confirmacion.setHeaderText("¿Autorizar esta reserva?");
        confirmacion.setContentText("La reserva será confirmada y el usuario será notificado.");
        
        if (confirmacion.showAndWait().orElse(javafx.scene.control.ButtonType.CANCEL) == javafx.scene.control.ButtonType.OK) {
            autorizarReserva();
        }
    }
    
    /**
     * Autoriza la reserva cambiando su estado a CONFIRMED
     * Si la confirmación es antes de las 2 horas del inicio, programa un recordatorio
     */
    private void autorizarReserva() {
        ConexionBD conexionBD = ConexionBD.obtenerInstancia();
        
        try {
            Connection conexion = conexionBD.obtenerConexion();
            
            // Primero obtener los datos de la reserva para verificar la fecha de inicio
            String sqlSelect = "SELECT r.user_id, r.start_at, ro.name as nombre_sala " +
                              "FROM reservations r " +
                              "INNER JOIN rooms ro ON r.room_id = ro.id " +
                              "WHERE r.id = ?";
            
            Long userId = null;
            LocalDateTime fechaInicio = null;
            String nombreSala = null;
            
            try (PreparedStatement selectStmt = conexion.prepareStatement(sqlSelect)) {
                selectStmt.setLong(1, reservaId);
                try (ResultSet resultado = selectStmt.executeQuery()) {
                    if (resultado.next()) {
                        userId = resultado.getLong("user_id");
                        if (resultado.getTimestamp("start_at") != null) {
                            fechaInicio = resultado.getTimestamp("start_at").toLocalDateTime();
                        }
                        nombreSala = resultado.getString("nombre_sala");
                    }
                }
            }
            
            if (userId == null || fechaInicio == null) {
                mostrarMensajeEstado("Error: No se pudieron obtener los datos de la reserva.", true);
                return;
            }
            
            // Actualizar el estado de la reserva a CONFIRMED
            String sqlUpdate = "UPDATE reservations SET status_id = (SELECT id FROM reservation_status WHERE code = 'CONFIRMED' LIMIT 1) WHERE id = ?";
            
            try (PreparedStatement updateStmt = conexion.prepareStatement(sqlUpdate)) {
                updateStmt.setLong(1, reservaId);
                
                int filasAfectadas = updateStmt.executeUpdate();
                
                if (filasAfectadas > 0) {
                    // Verificar si la confirmación fue antes de las 2 horas del inicio
                    LocalDateTime ahora = LocalDateTime.now();
                    Duration tiempoHastaInicio = Duration.between(ahora, fechaInicio);
                    long horasHastaInicio = tiempoHastaInicio.toHours();
                    
                    // Si hay más de 2 horas hasta el inicio, programar recordatorio
                    if (horasHastaInicio > 2) {
                        programarRecordatorio(userId, reservaId, fechaInicio, nombreSala, conexion);
                    }
                    
                    mostrarMensajeEstado("Reserva autorizada exitosamente.", false);
                    // Recargar datos y actualizar botones
                    Platform.runLater(() -> {
                        try {
                            Thread.sleep(1500);
                            Platform.runLater(() -> {
                                cargarDatosReserva();
                                ocultarMensajeEstado();
                            });
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            cargarDatosReserva();
                            ocultarMensajeEstado();
                        }
                    });
                } else {
                    mostrarMensajeEstado("Error al autorizar la reserva.", true);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al autorizar reserva: " + e.getMessage());
            e.printStackTrace();
            mostrarMensajeEstado("Error al autorizar la reserva: " + e.getMessage(), true);
        }
    }
    
    /**
     * Programa un recordatorio para 2 horas antes del inicio de la reservación
     * 
     * @param userId ID del usuario propietario de la reserva
     * @param reservaId ID de la reserva
     * @param fechaInicio Fecha y hora de inicio de la reserva
     * @param nombreSala Nombre de la sala
     * @param conexion Conexión a la base de datos
     */
    private void programarRecordatorio(Long userId, Long reservaId, LocalDateTime fechaInicio, 
                                      String nombreSala, Connection conexion) throws SQLException {
        // Calcular la fecha del recordatorio (2 horas antes del inicio)
        LocalDateTime fechaRecordatorio = fechaInicio.minusHours(2);
        
        // Obtener el ID del tipo de notificación RESERVATION_REMINDER
        int tipoNotificacionId = obtenerTipoNotificacionId("RESERVATION_REMINDER", conexion);
        if (tipoNotificacionId == 0) {
            // Si no existe, crearlo
            tipoNotificacionId = crearTipoNotificacion("RESERVATION_REMINDER", "Recordatorio de reserva", conexion);
        }
        
        // Crear mensaje del recordatorio
        String mensaje = String.format("Recordatorio: Tu reserva de la sala %s comienza en 2 horas. " +
                                       "Fecha: %s", 
                                       nombreSala, 
                                       fechaInicio.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        
        // Insertar notificación programada
        String sql = "INSERT INTO notifications (user_id, type_id, reservation_id, message, sent_at, delivered, created_at) " +
                     "VALUES (?, ?, ?, ?, ?, 0, NOW())";
        
        try (PreparedStatement statement = conexion.prepareStatement(sql)) {
            statement.setLong(1, userId);
            statement.setInt(2, tipoNotificacionId);
            statement.setLong(3, reservaId);
            statement.setString(4, mensaje);
            statement.setTimestamp(5, java.sql.Timestamp.valueOf(fechaRecordatorio));
            
            statement.executeUpdate();
        }
    }
    
    /**
     * Obtiene el ID del tipo de notificación
     * 
     * @param codigo Código del tipo de notificación
     * @param conexion Conexión a la base de datos
     * @return ID del tipo de notificación, o 0 si no existe
     */
    private int obtenerTipoNotificacionId(String codigo, Connection conexion) throws SQLException {
        String sql = "SELECT id FROM notification_types WHERE code = ? LIMIT 1";
        try (PreparedStatement statement = conexion.prepareStatement(sql)) {
            statement.setString(1, codigo);
            try (ResultSet resultado = statement.executeQuery()) {
                if (resultado.next()) {
                    return resultado.getInt("id");
                }
            }
        }
        return 0;
    }
    
    /**
     * Crea un nuevo tipo de notificación si no existe
     * 
     * @param codigo Código del tipo de notificación
     * @param etiqueta Etiqueta del tipo de notificación
     * @param conexion Conexión a la base de datos
     * @return ID del tipo de notificación creado
     */
    private int crearTipoNotificacion(String codigo, String etiqueta, Connection conexion) throws SQLException {
        String sql = "INSERT INTO notification_types (code, label) VALUES (?, ?)";
        try (PreparedStatement statement = conexion.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, codigo);
            statement.setString(2, etiqueta);
            statement.executeUpdate();
            
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                }
            }
        }
        return 0;
    }
    
    /**
     * Maneja el evento de cancelar la reserva
     */
    @FXML
    private void manejarCancelar() {
        // Mostrar campo para motivo de cancelación
        contenedorCancelacionAdmin.setVisible(true);
        contenedorCancelacionAdmin.setManaged(true);
        campoMotivoCancelacion.clear();
    }
    
    /**
     * Cancela la acción de cancelación
     */
    @FXML
    private void manejarCancelarCancelacion() {
        contenedorCancelacionAdmin.setVisible(false);
        contenedorCancelacionAdmin.setManaged(false);
        campoMotivoCancelacion.clear();
    }
    
    /**
     * Maneja la confirmación de cancelación de la reserva
     */
    @FXML
    private void manejarConfirmarCancelacion() {
        String motivoCancelacion = campoMotivoCancelacion.getText().trim();
        
        // Confirmar cancelación
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar Cancelación");
        confirmacion.setHeaderText("¿Cancelar esta reserva?");
        confirmacion.setContentText("La reserva será cancelada por el administrador." + 
                                   (motivoCancelacion.isEmpty() ? "" : "\nMotivo: " + motivoCancelacion));
        
        if (confirmacion.showAndWait().orElse(javafx.scene.control.ButtonType.CANCEL) == javafx.scene.control.ButtonType.OK) {
            cancelarReserva(motivoCancelacion);
        }
    }
    
    /**
     * Cancela la reserva cambiando su estado a CANCELLED_ADMIN
     */
    private void cancelarReserva(String motivoCancelacion) {
        ConexionBD conexionBD = ConexionBD.obtenerInstancia();
        
        String sqlUpdate = "UPDATE reservations SET " +
                          "status_id = (SELECT id FROM reservation_status WHERE code = 'CANCELLED_ADMIN' LIMIT 1), " +
                          "cancellation_reason = ? " +
                          "WHERE id = ?";
        
        try {
            Connection conexion = conexionBD.obtenerConexion();
            try (PreparedStatement statement = conexion.prepareStatement(sqlUpdate)) {
                if (motivoCancelacion != null && !motivoCancelacion.isEmpty()) {
                    statement.setString(1, motivoCancelacion);
                } else {
                    statement.setString(1, "Cancelada por el administrador");
                }
                statement.setLong(2, reservaId);
                
                int filasAfectadas = statement.executeUpdate();
                
                if (filasAfectadas > 0) {
                    mostrarMensajeEstado("Reserva cancelada exitosamente.", false);
                    contenedorCancelacionAdmin.setVisible(false);
                    contenedorCancelacionAdmin.setManaged(false);
                    campoMotivoCancelacion.clear();
                    
                    // Recargar datos y actualizar botones
                    Platform.runLater(() -> {
                        try {
                            Thread.sleep(1500);
                            Platform.runLater(() -> {
                                cargarDatosReserva();
                                ocultarMensajeEstado();
                            });
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            cargarDatosReserva();
                            ocultarMensajeEstado();
                        }
                    });
                } else {
                    mostrarMensajeEstado("Error al cancelar la reserva.", true);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al cancelar reserva: " + e.getMessage());
            e.printStackTrace();
            mostrarMensajeEstado("Error al cancelar la reserva: " + e.getMessage(), true);
        }
    }
    
    /**
     * Muestra un mensaje de estado
     */
    private void mostrarMensajeEstado(String mensaje, boolean esError) {
        lblMensajeEstado.setText(mensaje);
        lblMensajeEstado.getStyleClass().clear();
        lblMensajeEstado.getStyleClass().add("mensaje-estado-admin");
        if (esError) {
            lblMensajeEstado.getStyleClass().add("mensaje-estado-error");
        } else {
            lblMensajeEstado.getStyleClass().add("mensaje-estado-exito");
        }
        lblMensajeEstado.setVisible(true);
        lblMensajeEstado.setManaged(true);
    }
    
    /**
     * Oculta el mensaje de estado
     */
    private void ocultarMensajeEstado() {
        lblMensajeEstado.setVisible(false);
        lblMensajeEstado.setManaged(false);
    }
    
    /**
     * Muestra una alerta al usuario
     */
    private void mostrarAlerta(String titulo, String encabezado, String mensaje, Alert.AlertType tipo) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(encabezado);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}

