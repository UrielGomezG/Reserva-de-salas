package interfaz.sara.Controladores.Usuario;

import interfaz.sara.ConexionBD.ConexionBD;
import interfaz.sara.Modelo.Reserva;
import interfaz.sara.Modelo.Sala;
import interfaz.sara.Utilidades.SesionUsuario;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Controlador para la vista de reporte de incidentes (VistaReporte.fxml)
 * Gestiona el formulario para reportar incidentes en las salas
 */
public class ControladorReporte {

    // ========== Componentes FXML ==========
    
    @FXML
    private VBox navbarInclude;
    
    @FXML
    private ComboBox<Sala> comboSala;
    
    @FXML
    private DatePicker datePickerFecha;
    
    @FXML
    private ComboBox<Reserva> comboReservacion;
    
    @FXML
    private TextArea textAreaDescripcion;

    // ========== Variables de estado ==========
    
    /** Sala seleccionada */
    private Sala salaSeleccionada;
    
    /** Fecha seleccionada */
    private LocalDate fechaSeleccionada;
    
    /** Reservación seleccionada (opcional) */
    private Reserva reservacionSeleccionada;
    
    /** Tipo de incidente seleccionado (nombre mostrado) */
    private String tipoIncidenteSeleccionado;
    
    /** ID del tipo de incidente seleccionado */
    private Integer tipoIncidenteIdSeleccionado;
    
    /** Tarjeta de incidente actualmente seleccionada */
    private VBox tarjetaIncidenteSeleccionada;
    
    /** Lista de salas disponibles */
    private ObservableList<Sala> listaSalas;
    
    /** Lista de reservaciones del usuario */
    private ObservableList<Reserva> listaReservaciones;
    
    /** Mapeo de nombres de tipos de incidente a IDs en la base de datos */
    private static final java.util.Map<String, Integer> MAPEO_TIPOS_INCIDENTE = new java.util.HashMap<>();
    
    static {
        MAPEO_TIPOS_INCIDENTE.put("Mueble Roto", 1); // Daño en mobiliario
        MAPEO_TIPOS_INCIDENTE.put("Electrónico Roto", 2); // Problemas de equipo
        MAPEO_TIPOS_INCIDENTE.put("Sucio", 3); // Limpieza
        MAPEO_TIPOS_INCIDENTE.put("Otro", 4); // Otro
    }

    // ========== Métodos de inicialización ==========
    
    /**
     * Inicializa el controlador después de que se carga el FXML
     */
    @FXML
    private void initialize() {
        // Inicializar listas
        listaSalas = FXCollections.observableArrayList();
        listaReservaciones = FXCollections.observableArrayList();
        
        // Configurar ComboBox de salas
        configurarComboSala();
        
        // Configurar ComboBox de reservaciones
        configurarComboReservacion();
        
        // Cargar datos desde la base de datos
        cargarSalas();
        cargarReservaciones();
        
        // Establecer fecha por defecto (hoy) en el DatePicker
        fechaSeleccionada = LocalDate.now();
        datePickerFecha.setValue(fechaSeleccionada);
        
        // Actualizar el navbar para marcar "Reporte" como activo
        actualizarNavbarActivo();
    }
    
    /**
     * Configura el ComboBox de salas para mostrar el nombre correctamente
     */
    private void configurarComboSala() {
        comboSala.setItems(listaSalas);
        
        // Configurar cómo se muestra cada sala en el ComboBox
        comboSala.setCellFactory(param -> new javafx.scene.control.ListCell<Sala>() {
            @Override
            protected void updateItem(Sala sala, boolean empty) {
                super.updateItem(sala, empty);
                if (empty || sala == null) {
                    setText(null);
                } else {
                    setText(sala.getNombre() + " - " + sala.getTipoSala());
                }
            }
        });
        
        // Configurar cómo se muestra la sala seleccionada
        comboSala.setButtonCell(new javafx.scene.control.ListCell<Sala>() {
            @Override
            protected void updateItem(Sala sala, boolean empty) {
                super.updateItem(sala, empty);
                if (empty || sala == null) {
                    setText(null);
                } else {
                    setText(sala.getNombre() + " - " + sala.getTipoSala());
                }
            }
        });
    }
    
    /**
     * Configura el ComboBox de reservaciones para mostrar información relevante
     */
    private void configurarComboReservacion() {
        comboReservacion.setItems(listaReservaciones);
        
        // Configurar cómo se muestra cada reservación en el ComboBox
        comboReservacion.setCellFactory(param -> new javafx.scene.control.ListCell<Reserva>() {
            @Override
            protected void updateItem(Reserva reserva, boolean empty) {
                super.updateItem(reserva, empty);
                if (empty || reserva == null) {
                    setText(null);
                } else {
                    java.time.format.DateTimeFormatter formatter = 
                        java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
                    String fechaInicio = reserva.getFechaInicio() != null ? 
                        reserva.getFechaInicio().format(formatter) : "N/A";
                    setText(reserva.getNombreSala() + " - " + fechaInicio);
                }
            }
        });
        
        // Configurar cómo se muestra la reservación seleccionada
        comboReservacion.setButtonCell(new javafx.scene.control.ListCell<Reserva>() {
            @Override
            protected void updateItem(Reserva reserva, boolean empty) {
                super.updateItem(reserva, empty);
                if (empty || reserva == null) {
                    setText(null);
                } else {
                    java.time.format.DateTimeFormatter formatter = 
                        java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
                    String fechaInicio = reserva.getFechaInicio() != null ? 
                        reserva.getFechaInicio().format(formatter) : "N/A";
                    setText(reserva.getNombreSala() + " - " + fechaInicio);
                }
            }
        });
    }
    
    /**
     * Actualiza el navbar para marcar "Reporte" como vista activa
     */
    private void actualizarNavbarActivo() {
        Platform.runLater(() -> {
            if (navbarInclude != null) {
                Button btnMisReservas = (Button) navbarInclude.lookup("#botonMisReservas");
                Button btnNuevaReserva = (Button) navbarInclude.lookup("#botonNuevaReserva");
                Button btnPerfil = (Button) navbarInclude.lookup("#botonPerfil");
                Button btnReporte = (Button) navbarInclude.lookup("#botonReporte");
                
                if (btnMisReservas != null && btnNuevaReserva != null && 
                    btnPerfil != null && btnReporte != null) {
                    // Resetear todos los botones
                    btnMisReservas.getStyleClass().remove("navbar-button-active");
                    btnNuevaReserva.getStyleClass().remove("navbar-button-active");
                    btnPerfil.getStyleClass().remove("navbar-button-active");
                    btnReporte.getStyleClass().remove("navbar-button-active");
                    
                    if (!btnMisReservas.getStyleClass().contains("navbar-button")) {
                        btnMisReservas.getStyleClass().add("navbar-button");
                    }
                    if (!btnNuevaReserva.getStyleClass().contains("navbar-button")) {
                        btnNuevaReserva.getStyleClass().add("navbar-button");
                    }
                    if (!btnPerfil.getStyleClass().contains("navbar-button")) {
                        btnPerfil.getStyleClass().add("navbar-button");
                    }
                    
                    // Activar el botón de Reporte
                    btnReporte.getStyleClass().remove("navbar-button");
                    if (!btnReporte.getStyleClass().contains("navbar-button-active")) {
                        btnReporte.getStyleClass().add("navbar-button-active");
                    }
                }
            }
        });
    }

    // ========== Métodos de manejo de eventos ==========
    
    /**
     * Maneja el cambio de sala seleccionada en el ComboBox
     */
    @FXML
    private void manejarCambioSala() {
        salaSeleccionada = comboSala.getSelectionModel().getSelectedItem();
    }
    
    /**
     * Carga las salas habilitadas desde la base de datos
     */
    private void cargarSalas() {
        try {
            List<Sala> salas = obtenerSalasHabilitadas();
            listaSalas.clear();
            listaSalas.addAll(salas);
        } catch (SQLException e) {
            System.err.println("Error al cargar salas: " + e.getMessage());
            mostrarAlerta("Error", "Error al cargar salas", 
                         "No se pudieron cargar las salas. Por favor, intente más tarde.", 
                         Alert.AlertType.ERROR);
        }
    }
    
    /**
     * Carga las reservaciones del usuario desde la base de datos
     */
    private void cargarReservaciones() {
        try {
            List<Reserva> reservaciones = obtenerReservacionesDeBD();
            listaReservaciones.clear();
            listaReservaciones.addAll(reservaciones);
        } catch (SQLException e) {
            System.err.println("Error al cargar reservaciones: " + e.getMessage());
            // No mostrar alerta, ya que es opcional
        }
    }
    
    /**
     * Obtiene las reservaciones del usuario desde la base de datos
     * 
     * @return Lista de reservaciones del usuario
     * @throws SQLException Si ocurre un error al consultar la base de datos
     */
    private List<Reserva> obtenerReservacionesDeBD() throws SQLException {
        List<Reserva> reservaciones = new ArrayList<>();
        
        SesionUsuario sesion = SesionUsuario.obtenerInstancia();
        if (!sesion.estaAutenticado()) {
            return reservaciones;
        }
        
        Long usuarioIdActual = sesion.getUsuarioId();
        
        ConexionBD conexionBD = ConexionBD.obtenerInstancia();
        Connection conexion = conexionBD.obtenerConexion();
        
        String sql = "SELECT r.id, r.room_id, ro.name as nombre_sala, r.user_id, " +
                    "r.start_at, r.end_at, r.people_count, r.reason, " +
                    "r.status_id, rs.code as codigo_estado, rs.label as estado_reserva, " +
                    "r.cancelled_by_user_id, r.cancellation_reason, r.created_at " +
                    "FROM reservations r " +
                    "INNER JOIN rooms ro ON r.room_id = ro.id " +
                    "INNER JOIN reservation_status rs ON r.status_id = rs.id " +
                    "WHERE r.user_id = ? " +
                    "ORDER BY r.start_at DESC";
        
        try (PreparedStatement statement = conexion.prepareStatement(sql)) {
            statement.setLong(1, usuarioIdActual);
            
            try (ResultSet resultado = statement.executeQuery()) {
                while (resultado.next()) {
                    Reserva reserva = new Reserva();
                    reserva.setId(resultado.getLong("id"));
                    reserva.setRoomId(resultado.getLong("room_id"));
                    reserva.setNombreSala(resultado.getString("nombre_sala"));
                    reserva.setUserId(resultado.getLong("user_id"));
                    
                    // Convertir Timestamp a LocalDateTime
                    if (resultado.getTimestamp("start_at") != null) {
                        reserva.setFechaInicio(resultado.getTimestamp("start_at").toLocalDateTime());
                    }
                    if (resultado.getTimestamp("end_at") != null) {
                        reserva.setFechaFin(resultado.getTimestamp("end_at").toLocalDateTime());
                    }
                    if (resultado.getTimestamp("created_at") != null) {
                        reserva.setFechaCreacion(resultado.getTimestamp("created_at").toLocalDateTime());
                    }
                    
                    reserva.setCantidadPersonas(resultado.getInt("people_count"));
                    reserva.setMotivo(resultado.getString("reason"));
                    reserva.setStatusId(resultado.getInt("status_id"));
                    reserva.setCodigoEstado(resultado.getString("codigo_estado"));
                    reserva.setEstadoReserva(resultado.getString("estado_reserva"));
                    
                    if (resultado.getLong("cancelled_by_user_id") > 0) {
                        reserva.setCanceladoPorUsuarioId(resultado.getLong("cancelled_by_user_id"));
                    }
                    reserva.setMotivoCancelacion(resultado.getString("cancellation_reason"));
                    
                    reservaciones.add(reserva);
                }
            }
        }
        
        return reservaciones;
    }
    
    /**
     * Maneja el cambio de fecha seleccionada en el DatePicker
     */
    @FXML
    private void manejarCambioFecha() {
        fechaSeleccionada = datePickerFecha.getValue();
    }
    
    /**
     * Maneja el cambio de reservación seleccionada en el ComboBox
     */
    @FXML
    private void manejarCambioReservacion() {
        reservacionSeleccionada = comboReservacion.getSelectionModel().getSelectedItem();
    }
    
    /**
     * Maneja la selección de tipo de incidente
     * Permite seleccionar y deseleccionar el tipo de incidente
     */
    @FXML
    private void manejarSeleccionarIncidente(MouseEvent event) {
        VBox card = (VBox) event.getSource();
        Label label = (Label) card.getChildren().get(0);
        String tipo = label.getText();
        
        // Si la tarjeta ya está seleccionada, deseleccionarla
        if (card == tarjetaIncidenteSeleccionada && tipoIncidenteSeleccionado != null) {
            card.getStyleClass().remove("incident-card-selected");
            tipoIncidenteSeleccionado = null;
            tipoIncidenteIdSeleccionado = null;
            tarjetaIncidenteSeleccionada = null;
            return;
        }
        
        // Deseleccionar la tarjeta anterior si existe
        if (tarjetaIncidenteSeleccionada != null) {
            tarjetaIncidenteSeleccionada.getStyleClass().remove("incident-card-selected");
        }
        
        // Seleccionar la nueva tarjeta y obtener el ID correspondiente
        tipoIncidenteSeleccionado = tipo;
        tipoIncidenteIdSeleccionado = MAPEO_TIPOS_INCIDENTE.get(tipo);
        tarjetaIncidenteSeleccionada = card;
        card.getStyleClass().add("incident-card-selected");
    }
    
    /**
     * Maneja la entrega del reporte
     * Valida los datos y guarda el incidente en la base de datos
     */
    @FXML
    private void manejarEntregarReporte() {
        // Validar campos
        if (!validarFormulario()) {
            return;
        }
        
        try {
            // Crear el incidente en la base de datos
            crearIncidente();
            
            // Mostrar mensaje de éxito
            mostrarAlerta("Éxito", "Reporte enviado", 
                         "El incidente ha sido reportado exitosamente.", 
                         Alert.AlertType.INFORMATION);
            
            // Limpiar formulario
            limpiarFormulario();
            
        } catch (SQLException e) {
            System.err.println("Error al crear incidente: " + e.getMessage());
            mostrarAlerta("Error", "Error al enviar reporte", 
                         "No se pudo enviar el reporte. Por favor, intente más tarde.", 
                         Alert.AlertType.ERROR);
        }
    }

    // ========== Métodos de lógica de negocio ==========
    
    /**
     * Valida que todos los campos del formulario estén completos
     * 
     * @return true si el formulario es válido, false en caso contrario
     */
    private boolean validarFormulario() {
        if (salaSeleccionada == null) {
            mostrarAlerta("Validación", "Campo incompleto", 
                         "Por favor, selecciona una sala.", 
                         Alert.AlertType.WARNING);
            return false;
        }
        
        if (tipoIncidenteSeleccionado == null || tipoIncidenteSeleccionado.isEmpty()) {
            mostrarAlerta("Validación", "Campo incompleto", 
                         "Por favor, selecciona un tipo de incidente.", 
                         Alert.AlertType.WARNING);
            return false;
        }
        
        if (textAreaDescripcion.getText() == null || 
            textAreaDescripcion.getText().trim().isEmpty()) {
            mostrarAlerta("Validación", "Campo incompleto", 
                         "Por favor, describe el problema en detalle.", 
                         Alert.AlertType.WARNING);
            return false;
        }
        
        return true;
    }
    
    /**
     * Crea un nuevo incidente en la base de datos
     * 
     * @throws SQLException Si ocurre un error al insertar en la base de datos
     */
    private void crearIncidente() throws SQLException {
        SesionUsuario sesion = SesionUsuario.obtenerInstancia();
        if (!sesion.estaAutenticado()) {
            throw new SQLException("Usuario no autenticado");
        }
        
        if (tipoIncidenteIdSeleccionado == null) {
            throw new SQLException("Tipo de incidente no seleccionado");
        }
        
        Long usuarioId = sesion.getUsuarioId();
        LocalDateTime fechaReporte = fechaSeleccionada.atStartOfDay();
        String descripcion = textAreaDescripcion.getText().trim();
        
        // Crear título basado en el tipo de incidente
        String titulo = tipoIncidenteSeleccionado + " - " + salaSeleccionada.getNombre();
        if (titulo.length() > 150) {
            titulo = titulo.substring(0, 147) + "...";
        }
        
        ConexionBD conexionBD = ConexionBD.obtenerInstancia();
        Connection conexion = conexionBD.obtenerConexion();
        
        // Si hay una reservación seleccionada, incluirla en la inserción
        // Nota: created_at tiene DEFAULT current_timestamp() en la BD, no es necesario establecerlo
        String sql;
        if (reservacionSeleccionada != null) {
            sql = "INSERT INTO incidents (room_id, reservation_id, reported_by_user_id, incident_type_id, title, description, incident_at) " +
                  "VALUES (?, ?, ?, ?, ?, ?, ?)";
        } else {
            sql = "INSERT INTO incidents (room_id, reported_by_user_id, incident_type_id, title, description, incident_at) " +
                  "VALUES (?, ?, ?, ?, ?, ?)";
        }
        
        try (PreparedStatement statement = conexion.prepareStatement(sql)) {
            int paramIndex = 1;
            statement.setLong(paramIndex++, salaSeleccionada.getId());
            
            if (reservacionSeleccionada != null) {
                statement.setLong(paramIndex++, reservacionSeleccionada.getId());
            }
            
            statement.setLong(paramIndex++, usuarioId);
            statement.setInt(paramIndex++, tipoIncidenteIdSeleccionado);
            statement.setString(paramIndex++, titulo);
            statement.setString(paramIndex++, descripcion.isEmpty() ? null : descripcion);
            statement.setTimestamp(paramIndex++, java.sql.Timestamp.valueOf(fechaReporte));
            
            int filasAfectadas = statement.executeUpdate();
            
            if (filasAfectadas == 0) {
                throw new SQLException("No se pudo crear el incidente");
            }
        }
    }
    
    /**
     * Obtiene la lista de salas habilitadas desde la base de datos
     * 
     * @return Lista de salas habilitadas
     * @throws SQLException Si ocurre un error al consultar la base de datos
     */
    private List<Sala> obtenerSalasHabilitadas() throws SQLException {
        List<Sala> salas = new ArrayList<>();
        
        ConexionBD conexionBD = ConexionBD.obtenerInstancia();
        Connection conexion = conexionBD.obtenerConexion();
        
        String sql = "SELECT r.id, r.room_code, r.name, r.capacity, r.room_type, " +
                    "COALESCE(l.name, 'Sin ubicación') as location, r.is_enabled " +
                    "FROM rooms r " +
                    "LEFT JOIN locations l ON r.location_id = l.id " +
                    "WHERE r.is_enabled = 1 AND r.deleted_at IS NULL " +
                    "ORDER BY r.name";
        
        try (PreparedStatement statement = conexion.prepareStatement(sql);
             ResultSet resultado = statement.executeQuery()) {
            
            while (resultado.next()) {
                Sala sala = new Sala();
                sala.setId(resultado.getLong("id"));
                sala.setCodigoSala(resultado.getInt("room_code"));
                sala.setNombre(resultado.getString("name"));
                sala.setCapacidad(resultado.getInt("capacity"));
                sala.setTipoSala(resultado.getString("room_type"));
                sala.setUbicacion(resultado.getString("location"));
                sala.setHabilitada(resultado.getBoolean("is_enabled"));
                
                salas.add(sala);
            }
        }
        
        return salas;
    }
    
    
    /**
     * Limpia el formulario después de enviar el reporte
     */
    private void limpiarFormulario() {
        salaSeleccionada = null;
        comboSala.getSelectionModel().clearSelection();
        
        fechaSeleccionada = LocalDate.now();
        datePickerFecha.setValue(fechaSeleccionada);
        
        reservacionSeleccionada = null;
        comboReservacion.getSelectionModel().clearSelection();
        
        tipoIncidenteSeleccionado = null;
        tipoIncidenteIdSeleccionado = null;
        if (tarjetaIncidenteSeleccionada != null) {
            tarjetaIncidenteSeleccionada.getStyleClass().remove("incident-card-selected");
            tarjetaIncidenteSeleccionada = null;
        }
        
        textAreaDescripcion.clear();
    }
    
    /**
     * Muestra una alerta al usuario
     * 
     * @param titulo Título de la alerta
     * @param encabezado Encabezado de la alerta
     * @param mensaje Mensaje de la alerta
     * @param tipo Tipo de alerta
     */
    private void mostrarAlerta(String titulo, String encabezado, String mensaje, Alert.AlertType tipo) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(encabezado);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}

