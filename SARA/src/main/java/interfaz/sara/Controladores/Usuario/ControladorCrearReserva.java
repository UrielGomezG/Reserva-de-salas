package interfaz.sara.Controladores.Usuario;

import interfaz.sara.ConexionBD.ConexionBD;
import interfaz.sara.Modelo.Sala;
import interfaz.sara.Utilidades.GestorNavegacion;
import interfaz.sara.Utilidades.SesionUsuario;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Controlador para la vista de crear reserva (VistaCrearReserva.fxml)
 * Gestiona el formulario para crear una nueva reserva
 */
public class ControladorCrearReserva {

    // ========== Componentes FXML ==========
    
    @FXML
    private ComboBox<Sala> comboSala;
    
    @FXML
    private DatePicker datePickerFecha;
    
    @FXML
    private ComboBox<String> comboHoraInicio;
    
    @FXML
    private ComboBox<String> comboHoraFin;
    
    @FXML
    private TextField campoMotivo;
    
    @FXML
    private TextField campoCantidadPersonas;
    
    @FXML
    private VBox mensajeSinDisponibilidad;
    
    @FXML
    private Label mensajeError;

    // ========== Variables de estado ==========
    
    /** Lista de salas disponibles */
    private ObservableList<Sala> listaSalas;
    
    /** Lista de horarios disponibles (8:00 a 20:00) */
    private ObservableList<String> listaHorarios;
    
    /** Fecha y sala seleccionadas para cargar disponibilidad */
    private LocalDate fechaSeleccionada;
    private Long salaIdSeleccionada;
    
    /** Horarios ocupados para la sala y fecha seleccionadas */
    private List<String> horariosOcupados;

    // ========== Constantes ==========
    
    /** Formateador para horarios */
    private static final DateTimeFormatter formateadorHora = DateTimeFormatter.ofPattern("HH:mm");
    
    /** Hora mínima para reservas */
    private static final int HORA_MINIMA = 8;
    
    /** Hora máxima para reservas */
    private static final int HORA_MAXIMA = 20;

    // ========== Métodos de inicialización ==========
    
    /**
     * Inicializa el controlador después de que se carga el FXML
     * Configura los componentes y carga las salas disponibles
     */
    @FXML
    private void initialize() {
        // Inicializar listas
        listaSalas = FXCollections.observableArrayList();
        listaHorarios = FXCollections.observableArrayList();
        
        // Generar lista de horarios (8:00 a 20:00)
        generarListaHorarios();
        
        // Configurar combo boxes
        comboSala.setItems(listaSalas);
        
        // Configurar el ComboBox de salas para mostrar el nombre
        comboSala.setCellFactory(param -> new javafx.scene.control.ListCell<Sala>() {
            @Override
            protected void updateItem(Sala sala, boolean empty) {
                super.updateItem(sala, empty);
                if (empty || sala == null) {
                    setText(null);
                } else {
                    setText(sala.getNombre() + " - " + sala.getTipoSala() + " (Cap: " + sala.getCapacidad() + ")");
                }
            }
        });
        
        comboSala.setButtonCell(new javafx.scene.control.ListCell<Sala>() {
            @Override
            protected void updateItem(Sala sala, boolean empty) {
                super.updateItem(sala, empty);
                if (empty || sala == null) {
                    setText(null);
                } else {
                    setText(sala.getNombre() + " - " + sala.getTipoSala() + " (Cap: " + sala.getCapacidad() + ")");
                }
            }
        });
        
        comboHoraInicio.setItems(listaHorarios);
        comboHoraFin.setItems(listaHorarios);
        
        // Configurar DatePicker para no permitir fechas pasadas
        datePickerFecha.setDayCellFactory(picker -> new javafx.scene.control.DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isBefore(LocalDate.now()));
            }
        });
        
        // Cargar salas desde la base de datos
        cargarSalas();
        
        // Ocultar mensajes inicialmente
        mensajeSinDisponibilidad.setVisible(false);
        mensajeSinDisponibilidad.setManaged(false);
        mensajeError.setVisible(false);
        mensajeError.setManaged(false);
    }

    // ========== Métodos de manejo de eventos ==========
    
    /**
     * Maneja el cambio de sala seleccionada
     * Actualiza los horarios disponibles según la sala
     */
    @FXML
    private void manejarCambioSala() {
        Sala salaSeleccionada = comboSala.getSelectionModel().getSelectedItem();
        if (salaSeleccionada != null) {
            salaIdSeleccionada = salaSeleccionada.getId();
            actualizarDisponibilidadHorarios();
        } else {
            salaIdSeleccionada = null;
            limpiarHorarios();
        }
    }
    
    /**
     * Maneja el cambio de fecha seleccionada
     * Actualiza los horarios disponibles según la fecha
     */
    @FXML
    private void manejarCambioFecha() {
        fechaSeleccionada = datePickerFecha.getValue();
        if (fechaSeleccionada != null) {
            actualizarDisponibilidadHorarios();
        } else {
            limpiarHorarios();
        }
    }
    
    /**
     * Maneja el cambio de hora de inicio
     * Actualiza las horas de fin disponibles (solo posteriores a la hora de inicio)
     */
    @FXML
    private void manejarCambioHoraInicio() {
        String horaInicio = comboHoraInicio.getSelectionModel().getSelectedItem();
        if (horaInicio != null) {
            actualizarHorasFinDisponibles(horaInicio);
        }
    }
    
    /**
     * Maneja el cambio de hora de fin
     * Valida que sea posterior a la hora de inicio
     */
    @FXML
    private void manejarCambioHoraFin() {
        String horaInicio = comboHoraInicio.getSelectionModel().getSelectedItem();
        String horaFin = comboHoraFin.getSelectionModel().getSelectedItem();
        
        if (horaInicio != null && horaFin != null) {
            if (!validarHoras(horaInicio, horaFin)) {
                mostrarError("La hora de fin debe ser posterior a la hora de inicio.");
                comboHoraFin.getSelectionModel().clearSelection();
            } else {
                ocultarError();
            }
        }
    }
    
    /**
     * Maneja el clic en el botón "Cancelar"
     * Regresa a la vista del calendario
     */
    @FXML
    private void manejarCancelar() {
        GestorNavegacion gestorNavegacion = GestorNavegacion.obtenerInstancia();
        gestorNavegacion.navegarAVistaNuevaReserva();
    }
    
    /**
     * Maneja el clic en el botón "Confirmar Reserva"
     * Valida los datos y crea la reserva en la base de datos
     */
    @FXML
    private void manejarConfirmarReserva() {
        ocultarError();
        
        // Validar que todos los campos estén completos
        if (!validarFormulario()) {
            return;
        }
        
        // Crear la reserva
        if (crearReserva()) {
            // Reserva creada exitosamente - volver al calendario
            GestorNavegacion gestorNavegacion = GestorNavegacion.obtenerInstancia();
            gestorNavegacion.navegarAVistaNuevaReserva();
        }
    }

    // ========== Métodos de validación ==========
    
    /**
     * Valida que todos los campos del formulario estén completos y sean válidos
     * 
     * @return true si el formulario es válido, false en caso contrario
     */
    private boolean validarFormulario() {
        if (comboSala.getSelectionModel().getSelectedItem() == null) {
            mostrarError("Por favor, seleccione una sala.");
            return false;
        }
        
        if (datePickerFecha.getValue() == null) {
            mostrarError("Por favor, seleccione una fecha.");
            return false;
        }
        
        // Validar que la fecha no sea pasada
        if (datePickerFecha.getValue().isBefore(LocalDate.now())) {
            mostrarError("No se pueden crear reservas para fechas pasadas.");
            return false;
        }
        
        String horaInicio = comboHoraInicio.getSelectionModel().getSelectedItem();
        if (horaInicio == null || horaInicio.isEmpty()) {
            mostrarError("Por favor, seleccione una hora de inicio.");
            return false;
        }
        
        String horaFin = comboHoraFin.getSelectionModel().getSelectedItem();
        if (horaFin == null || horaFin.isEmpty()) {
            mostrarError("Por favor, seleccione una hora de fin.");
            return false;
        }
        
        // Validar que la hora de fin sea posterior a la de inicio
        if (!validarHoras(horaInicio, horaFin)) {
            mostrarError("La hora de fin debe ser posterior a la hora de inicio.");
            return false;
        }
        
        // Validar cantidad de personas
        String cantidadPersonasStr = campoCantidadPersonas.getText().trim();
        if (cantidadPersonasStr.isEmpty()) {
            mostrarError("Por favor, ingrese la cantidad de personas.");
            return false;
        }
        
        try {
            int cantidadPersonas = Integer.parseInt(cantidadPersonasStr);
            if (cantidadPersonas <= 0) {
                mostrarError("La cantidad de personas debe ser mayor a cero.");
                return false;
            }
            
            // Validar que no exceda la capacidad de la sala
            Sala salaSeleccionada = comboSala.getSelectionModel().getSelectedItem();
            if (cantidadPersonas > salaSeleccionada.getCapacidad()) {
                mostrarError("La cantidad de personas (" + cantidadPersonas + 
                           ") excede la capacidad de la sala (" + salaSeleccionada.getCapacidad() + ").");
                return false;
            }
        } catch (NumberFormatException e) {
            mostrarError("Por favor, ingrese un número válido para la cantidad de personas.");
            return false;
        }
        
        return true;
    }
    
    /**
     * Valida que la hora de fin sea posterior a la hora de inicio
     * 
     * @param horaInicio La hora de inicio (formato HH:mm)
     * @param horaFin La hora de fin (formato HH:mm)
     * @return true si es válido, false en caso contrario
     */
    private boolean validarHoras(String horaInicio, String horaFin) {
        try {
            LocalTime inicio = LocalTime.parse(horaInicio, formateadorHora);
            LocalTime fin = LocalTime.parse(horaFin, formateadorHora);
            return fin.isAfter(inicio);
        } catch (Exception e) {
            return false;
        }
    }

    // ========== Métodos de carga de datos ==========
    
    /**
     * Genera la lista de horarios disponibles (8:00 a 20:00)
     */
    private void generarListaHorarios() {
        listaHorarios.clear();
        for (int hora = HORA_MINIMA; hora <= HORA_MAXIMA; hora++) {
            listaHorarios.add(String.format("%02d:00", hora));
        }
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
            e.printStackTrace();
            mostrarError("Error al cargar las salas disponibles.");
        }
    }
    
    /**
     * Obtiene todas las salas habilitadas desde la base de datos
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
     * Actualiza la disponibilidad de horarios según la sala y fecha seleccionadas
     */
    private void actualizarDisponibilidadHorarios() {
        if (salaIdSeleccionada == null || fechaSeleccionada == null) {
            limpiarHorarios();
            return;
        }
        
        try {
            // Obtener horarios ocupados
            horariosOcupados = obtenerHorariosOcupados(salaIdSeleccionada, fechaSeleccionada);
            
            // Filtrar horarios disponibles
            ObservableList<String> horariosDisponibles = FXCollections.observableArrayList();
            for (String horario : listaHorarios) {
                if (!horariosOcupados.contains(horario)) {
                    horariosDisponibles.add(horario);
                }
            }
            
            // Actualizar combo boxes
            comboHoraInicio.setItems(horariosDisponibles);
            comboHoraFin.setItems(horariosDisponibles);
            
            // Limpiar selecciones
            comboHoraInicio.getSelectionModel().clearSelection();
            comboHoraFin.getSelectionModel().clearSelection();
            
            // Mostrar mensaje si no hay disponibilidad
            if (horariosDisponibles.isEmpty()) {
                mensajeSinDisponibilidad.setVisible(true);
                mensajeSinDisponibilidad.setManaged(true);
            } else {
                mensajeSinDisponibilidad.setVisible(false);
                mensajeSinDisponibilidad.setManaged(false);
            }
            
        } catch (SQLException e) {
            System.err.println("Error al actualizar disponibilidad: " + e.getMessage());
            e.printStackTrace();
            mostrarError("Error al cargar la disponibilidad de horarios.");
        }
    }
    
    /**
     * Actualiza las horas de fin disponibles (solo posteriores a la hora de inicio)
     * 
     * @param horaInicio La hora de inicio seleccionada
     */
    private void actualizarHorasFinDisponibles(String horaInicio) {
        try {
            LocalTime inicio = LocalTime.parse(horaInicio, formateadorHora);
            
            ObservableList<String> horasFinDisponibles = FXCollections.observableArrayList();
            for (String horario : comboHoraInicio.getItems()) {
                LocalTime hora = LocalTime.parse(horario, formateadorHora);
                if (hora.isAfter(inicio)) {
                    horasFinDisponibles.add(horario);
                }
            }
            
            comboHoraFin.setItems(horasFinDisponibles);
            
            // Si la hora de fin seleccionada no es válida, limpiar
            String horaFinSeleccionada = comboHoraFin.getSelectionModel().getSelectedItem();
            if (horaFinSeleccionada != null) {
                LocalTime fin = LocalTime.parse(horaFinSeleccionada, formateadorHora);
                if (!fin.isAfter(inicio)) {
                    comboHoraFin.getSelectionModel().clearSelection();
                }
            }
        } catch (Exception e) {
            System.err.println("Error al actualizar horas de fin: " + e.getMessage());
        }
    }
    
    /**
     * Limpia las selecciones de horarios
     */
    private void limpiarHorarios() {
        comboHoraInicio.getSelectionModel().clearSelection();
        comboHoraFin.getSelectionModel().clearSelection();
        comboHoraInicio.setItems(listaHorarios);
        comboHoraFin.setItems(listaHorarios);
        mensajeSinDisponibilidad.setVisible(false);
        mensajeSinDisponibilidad.setManaged(false);
    }
    
    /**
     * Obtiene los horarios ocupados para una sala en una fecha específica
     * 
     * @param salaId El ID de la sala
     * @param fecha La fecha a consultar
     * @return Lista de horarios ocupados (formato HH:mm)
     * @throws SQLException Si ocurre un error al consultar la base de datos
     */
    private List<String> obtenerHorariosOcupados(Long salaId, LocalDate fecha) throws SQLException {
        List<String> horariosOcupados = new ArrayList<>();
        ConexionBD conexionBD = ConexionBD.obtenerInstancia();
        Connection conexion = conexionBD.obtenerConexion();
        
        String sql = "SELECT start_at, end_at " +
                    "FROM reservations " +
                    "WHERE room_id = ? " +
                    "AND DATE(start_at) = ? " +
                    "AND status_id IN (1, 2) " + // PENDING o CONFIRMED
                    "ORDER BY start_at";
        
        try (PreparedStatement statement = conexion.prepareStatement(sql)) {
            statement.setLong(1, salaId);
            statement.setDate(2, java.sql.Date.valueOf(fecha));
            
            try (ResultSet resultado = statement.executeQuery()) {
                while (resultado.next()) {
                    if (resultado.getTimestamp("start_at") != null && 
                        resultado.getTimestamp("end_at") != null) {
                        
                        LocalDateTime inicio = resultado.getTimestamp("start_at").toLocalDateTime();
                        LocalDateTime fin = resultado.getTimestamp("end_at").toLocalDateTime();
                        
                        // Agregar cada hora ocupada
                        LocalDateTime horaActual = inicio;
                        while (horaActual.isBefore(fin)) {
                            String horario = horaActual.format(formateadorHora);
                            if (!horariosOcupados.contains(horario)) {
                                horariosOcupados.add(horario);
                            }
                            horaActual = horaActual.plusHours(1);
                        }
                    }
                }
            }
        }
        
        return horariosOcupados;
    }

    // ========== Métodos de creación de reserva ==========
    
    /**
     * Crea una nueva reserva en la base de datos
     * 
     * @return true si la reserva fue creada exitosamente, false en caso contrario
     */
    private boolean crearReserva() {
        SesionUsuario sesion = SesionUsuario.obtenerInstancia();
        if (!sesion.estaAutenticado()) {
            mostrarError("Error: No hay usuario autenticado.");
            return false;
        }
        
        try {
            // Obtener datos del formulario
            Sala sala = comboSala.getSelectionModel().getSelectedItem();
            LocalDate fecha = datePickerFecha.getValue();
            String horaInicioStr = comboHoraInicio.getSelectionModel().getSelectedItem();
            String horaFinStr = comboHoraFin.getSelectionModel().getSelectedItem();
            String motivo = campoMotivo.getText().trim();
            int cantidadPersonas = Integer.parseInt(campoCantidadPersonas.getText().trim());
            
            // Construir fechas completas
            LocalTime horaInicio = LocalTime.parse(horaInicioStr, formateadorHora);
            LocalTime horaFin = LocalTime.parse(horaFinStr, formateadorHora);
            
            LocalDateTime fechaHoraInicio = LocalDateTime.of(fecha, horaInicio);
            LocalDateTime fechaHoraFin = LocalDateTime.of(fecha, horaFin);
            
            // Verificar que no haya conflictos
            if (existeConflicto(sala.getId(), fechaHoraInicio, fechaHoraFin)) {
                mostrarError("Ya existe una reserva para este horario en la sala seleccionada.");
                return false;
            }
            
            // Insertar la reserva en la base de datos
            ConexionBD conexionBD = ConexionBD.obtenerInstancia();
            Connection conexion = conexionBD.obtenerConexion();
            
            String sql = "INSERT INTO reservations (room_id, user_id, start_at, end_at, people_count, reason, status_id) " +
                        "VALUES (?, ?, ?, ?, ?, ?, 1)"; // status_id = 1 (PENDING)
            
            try (PreparedStatement statement = conexion.prepareStatement(sql)) {
                statement.setLong(1, sala.getId());
                statement.setLong(2, sesion.getUsuarioId());
                statement.setTimestamp(3, java.sql.Timestamp.valueOf(fechaHoraInicio));
                statement.setTimestamp(4, java.sql.Timestamp.valueOf(fechaHoraFin));
                statement.setInt(5, cantidadPersonas);
                statement.setString(6, motivo.isEmpty() ? null : motivo);
                
                int filasAfectadas = statement.executeUpdate();
                
                if (filasAfectadas > 0) {
                    System.out.println("Reserva creada exitosamente");
                    return true;
                } else {
                    mostrarError("Error al crear la reserva. Por favor, intente nuevamente.");
                    return false;
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error al crear reserva: " + e.getMessage());
            e.printStackTrace();
            mostrarError("Error al crear la reserva. Por favor, intente nuevamente.");
            return false;
        } catch (Exception e) {
            System.err.println("Error inesperado: " + e.getMessage());
            e.printStackTrace();
            mostrarError("Error inesperado. Por favor, intente nuevamente.");
            return false;
        }
    }
    
    /**
     * Verifica si existe un conflicto de horarios para la reserva propuesta
     * 
     * @param salaId El ID de la sala
     * @param fechaHoraInicio La fecha y hora de inicio
     * @param fechaHoraFin La fecha y hora de fin
     * @return true si existe conflicto, false en caso contrario
     */
    private boolean existeConflicto(Long salaId, LocalDateTime fechaHoraInicio, LocalDateTime fechaHoraFin) {
        try {
            ConexionBD conexionBD = ConexionBD.obtenerInstancia();
            Connection conexion = conexionBD.obtenerConexion();
            
            String sql = "SELECT COUNT(*) as count " +
                        "FROM reservations " +
                        "WHERE room_id = ? " +
                        "AND status_id IN (1, 2) " + // PENDING o CONFIRMED
                        "AND ((start_at < ? AND end_at > ?) OR " +
                        "     (start_at < ? AND end_at > ?) OR " +
                        "     (start_at >= ? AND end_at <= ?))";
            
            try (PreparedStatement statement = conexion.prepareStatement(sql)) {
                statement.setLong(1, salaId);
                statement.setTimestamp(2, java.sql.Timestamp.valueOf(fechaHoraInicio));
                statement.setTimestamp(3, java.sql.Timestamp.valueOf(fechaHoraInicio));
                statement.setTimestamp(4, java.sql.Timestamp.valueOf(fechaHoraFin));
                statement.setTimestamp(5, java.sql.Timestamp.valueOf(fechaHoraFin));
                statement.setTimestamp(6, java.sql.Timestamp.valueOf(fechaHoraInicio));
                statement.setTimestamp(7, java.sql.Timestamp.valueOf(fechaHoraFin));
                
                try (ResultSet resultado = statement.executeQuery()) {
                    if (resultado.next()) {
                        return resultado.getInt("count") > 0;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al verificar conflicto: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }

    // ========== Métodos de interfaz de usuario ==========
    
    /**
     * Muestra un mensaje de error al usuario
     * 
     * @param mensaje El mensaje de error a mostrar
     */
    private void mostrarError(String mensaje) {
        mensajeError.setText(mensaje);
        mensajeError.setVisible(true);
        mensajeError.setManaged(true);
    }
    
    /**
     * Oculta el mensaje de error
     */
    private void ocultarError() {
        mensajeError.setVisible(false);
        mensajeError.setManaged(false);
        mensajeError.setText("");
    }
}

