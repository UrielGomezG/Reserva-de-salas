package interfaz.sara.Controladores.Usuario;

import interfaz.sara.ConexionBD.ConexionBD;
import interfaz.sara.Modelo.Sala;
import interfaz.sara.Utilidades.GestorNavegacionUsuario;
import interfaz.sara.Utilidades.SesionUsuario;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
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
    private Label labelNombreSala;
    
    @FXML
    private Label labelTipoSala;
    
    @FXML
    private Label labelCapacidadSala;
    
    @FXML
    private Label labelUbicacionSala;
    
    @FXML
    private Label labelFecha;
    
    @FXML
    private Label labelHoraInicio;
    
    @FXML
    private ComboBox<String> comboHoraFin;
    
    @FXML
    private TextField campoMotivo;
    
    @FXML
    private Spinner<Integer> spinnerCantidadPersonas;
    
    @FXML
    private VBox mensajeSinDisponibilidad;
    
    @FXML
    private Label mensajeError;

    // ========== Variables de estado ==========
    
    /** Lista de salas disponibles */
    private ObservableList<Sala> listaSalas;
    
    /** Lista de horarios disponibles (8:00 a 20:00) */
    private ObservableList<String> listaHorarios;
    
    /** Sala seleccionada */
    private Sala salaSeleccionada;
    
    /** Fecha y hora de inicio seleccionadas */
    private LocalDate fechaSeleccionada;
    private String horaInicioSeleccionada;
    
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
        
        // Configurar combo box de hora fin
        comboHoraFin.setItems(listaHorarios);
        
        // Configurar Spinner de cantidad de personas (mínimo 1, máximo temporal 1000 hasta seleccionar sala)
        // El Spinner no es editable, solo se puede cambiar con los botones
        SpinnerValueFactory.IntegerSpinnerValueFactory valueFactory = 
            new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 1000, 1);
        spinnerCantidadPersonas.setValueFactory(valueFactory);
        spinnerCantidadPersonas.setEditable(false);
        
        // Cargar salas desde la base de datos
        cargarSalas();
        
        // Verificar si hay datos prellenados del gestor de navegación
        GestorNavegacionUsuario gestor = GestorNavegacionUsuario.obtenerInstancia();
        Long salaId = gestor.obtenerSalaIdSeleccionada();
        String hora = gestor.obtenerHoraSeleccionada();
        LocalDate fecha = gestor.obtenerFechaSeleccionada();
        
        if (salaId != null && hora != null && fecha != null) {
            // Prellenar los campos con los datos recibidos
            prellenarDatos(salaId, hora, fecha);
            // Limpiar los datos temporales
            gestor.limpiarDatosReserva();
        }
        
        // Ocultar mensajes inicialmente
        mensajeSinDisponibilidad.setVisible(false);
        mensajeSinDisponibilidad.setManaged(false);
        mensajeError.setVisible(false);
        mensajeError.setManaged(false);
    }
    
    /**
     * Prellena los campos del formulario con los datos recibidos
     * 
     * @param salaId ID de la sala seleccionada
     * @param hora Hora seleccionada (formato HH:mm)
     * @param fecha Fecha seleccionada
     */
    private void prellenarDatos(Long salaId, String hora, LocalDate fecha) {
        // Buscar y seleccionar la sala
        for (Sala sala : listaSalas) {
            if (sala.getId().equals(salaId)) {
                salaSeleccionada = sala;
                fechaSeleccionada = fecha;
                horaInicioSeleccionada = hora;
                
                // Actualizar la información en los Labels
                actualizarInformacionSala();
                actualizarInformacionFechaHora();
                
                // Actualizar el límite del Spinner
                actualizarLimiteSpinner();
                
                // Establecer la hora de fin (1 hora después)
                try {
                    LocalTime horaInicio = LocalTime.parse(hora, formateadorHora);
                    LocalTime horaFin = horaInicio.plusHours(1);
                    String horaFinStr = horaFin.format(formateadorHora);
                    comboHoraFin.getSelectionModel().select(horaFinStr);
                } catch (Exception e) {
                    System.err.println("Error al calcular hora de fin: " + e.getMessage());
                }
                
                // Actualizar disponibilidad de horarios
                actualizarDisponibilidadHorarios();
                break;
            }
        }
    }
    
    /**
     * Actualiza los Labels con la información de la sala seleccionada
     */
    private void actualizarInformacionSala() {
        if (salaSeleccionada != null) {
            labelNombreSala.setText(salaSeleccionada.getNombre());
            labelTipoSala.setText(salaSeleccionada.getTipoSala());
            labelCapacidadSala.setText(String.valueOf(salaSeleccionada.getCapacidad()));
            labelUbicacionSala.setText(salaSeleccionada.getUbicacion() != null ? salaSeleccionada.getUbicacion() : "Sin ubicación");
        } else {
            labelNombreSala.setText("-");
            labelTipoSala.setText("-");
            labelCapacidadSala.setText("-");
            labelUbicacionSala.setText("-");
        }
    }
    
    /**
     * Actualiza los Labels con la fecha y hora de inicio
     */
    private void actualizarInformacionFechaHora() {
        if (fechaSeleccionada != null) {
            labelFecha.setText(fechaSeleccionada.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        } else {
            labelFecha.setText("-");
        }
        
        if (horaInicioSeleccionada != null) {
            labelHoraInicio.setText(horaInicioSeleccionada);
        } else {
            labelHoraInicio.setText("-");
        }
    }
    
    /**
     * Actualiza el límite del Spinner según la capacidad de la sala
     */
    private void actualizarLimiteSpinner() {
        if (salaSeleccionada != null) {
            SpinnerValueFactory.IntegerSpinnerValueFactory valueFactory = 
                (SpinnerValueFactory.IntegerSpinnerValueFactory) spinnerCantidadPersonas.getValueFactory();
            int capacidad = salaSeleccionada.getCapacidad();
            int valorActual = valueFactory.getValue();
            
            valueFactory.setMax(capacidad);
            
            if (valorActual > capacidad) {
                valueFactory.setValue(capacidad);
            }
        }
    }

    // ========== Métodos de manejo de eventos ==========
    
    
    /**
     * Maneja el cambio de hora de fin
     * Valida que sea posterior a la hora de inicio
     */
    @FXML
    private void manejarCambioHoraFin() {
        String horaFin = comboHoraFin.getSelectionModel().getSelectedItem();
        
        if (horaInicioSeleccionada != null && horaFin != null) {
            if (!validarHoras(horaInicioSeleccionada, horaFin)) {
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
        GestorNavegacionUsuario gestorNavegacion = GestorNavegacionUsuario.obtenerInstancia();
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
            // La navegación se maneja dentro de crearReserva() después de mostrar la alerta
        }
    }

    // ========== Métodos de validación ==========
    
    /**
     * Valida que todos los campos del formulario estén completos y sean válidos
     * 
     * @return true si el formulario es válido, false en caso contrario
     */
    private boolean validarFormulario() {
        // Validar que haya una sala seleccionada
        if (salaSeleccionada == null) {
            mostrarError("Error: No hay sala seleccionada.");
            return false;
        }
        
        // Validar fecha
        if (fechaSeleccionada == null) {
            mostrarError("Error: No hay fecha seleccionada.");
            return false;
        }
        
        // Validar que la fecha no sea pasada
        if (fechaSeleccionada.isBefore(LocalDate.now())) {
            mostrarError("No se pueden crear reservas para fechas pasadas.");
            return false;
        }
        
        // Validar hora de inicio
        if (horaInicioSeleccionada == null || horaInicioSeleccionada.isEmpty()) {
            mostrarError("Error: No hay hora de inicio seleccionada.");
            return false;
        }
        
        String horaFin = comboHoraFin.getSelectionModel().getSelectedItem();
        if (horaFin == null || horaFin.isEmpty()) {
            mostrarError("Por favor, seleccione una hora de fin.");
            comboHoraFin.requestFocus();
            return false;
        }
        
        // Validar que la hora de fin sea posterior a la de inicio
        if (!validarHoras(horaInicioSeleccionada, horaFin)) {
            mostrarError("La hora de fin debe ser posterior a la hora de inicio.");
            comboHoraFin.requestFocus();
            return false;
        }
        
        // Validar duración mínima de 1 hora
        try {
            LocalTime inicio = LocalTime.parse(horaInicioSeleccionada, formateadorHora);
            LocalTime fin = LocalTime.parse(horaFin, formateadorHora);
            long horas = java.time.Duration.between(inicio, fin).toHours();
            if (horas < 1) {
                mostrarError("La duración mínima de la reserva debe ser de 1 hora.");
                comboHoraFin.requestFocus();
                return false;
            }
        } catch (Exception e) {
            mostrarError("Error al validar la duración de la reserva.");
            return false;
        }
        
        // Validar cantidad de personas (el Spinner ya valida el rango, solo verificamos que haya sala seleccionada)
        int cantidadPersonas = spinnerCantidadPersonas.getValue();
        if (cantidadPersonas <= 0) {
            mostrarError("La cantidad de personas debe ser mayor a cero.");
            spinnerCantidadPersonas.requestFocus();
            return false;
        }
        
        // El Spinner ya limita el máximo a la capacidad de la sala, pero verificamos por seguridad
        if (cantidadPersonas > salaSeleccionada.getCapacidad()) {
            mostrarError("La cantidad de personas (" + cantidadPersonas + 
                       ") excede la capacidad de la sala (" + salaSeleccionada.getCapacidad() + ").");
            spinnerCantidadPersonas.requestFocus();
            return false;
        }
        
        // Validar motivo (opcional pero con límite de longitud si se proporciona)
        String motivo = campoMotivo.getText().trim();
        if (!motivo.isEmpty() && motivo.length() > 500) {
            mostrarError("El motivo no puede exceder 500 caracteres.");
            campoMotivo.requestFocus();
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
        if (salaSeleccionada == null || fechaSeleccionada == null) {
            limpiarHorarios();
            return;
        }
        
        try {
            // Obtener horarios ocupados
            horariosOcupados = obtenerHorariosOcupados(salaSeleccionada.getId(), fechaSeleccionada);
            
            // Filtrar horarios disponibles (solo posteriores a la hora de inicio)
            ObservableList<String> horariosFinDisponibles = FXCollections.observableArrayList();
            if (horaInicioSeleccionada != null) {
                try {
                    LocalTime inicio = LocalTime.parse(horaInicioSeleccionada, formateadorHora);
                    for (String horario : listaHorarios) {
                        LocalTime hora = LocalTime.parse(horario, formateadorHora);
                        if (hora.isAfter(inicio) && !horariosOcupados.contains(horario)) {
                            horariosFinDisponibles.add(horario);
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Error al parsear hora de inicio: " + e.getMessage());
                }
            }
            
            // Actualizar combo box de hora fin
            comboHoraFin.setItems(horariosFinDisponibles);
            
            // Limpiar selección si la hora fin actual no es válida
            String horaFinSeleccionada = comboHoraFin.getSelectionModel().getSelectedItem();
            if (horaFinSeleccionada != null && !horariosFinDisponibles.contains(horaFinSeleccionada)) {
                comboHoraFin.getSelectionModel().clearSelection();
            }
            
            // Mostrar mensaje si no hay disponibilidad
            if (horariosFinDisponibles.isEmpty()) {
                mensajeSinDisponibilidad.setVisible(true);
                mensajeSinDisponibilidad.setManaged(true);
            } else {
                mensajeSinDisponibilidad.setVisible(false);
                mensajeSinDisponibilidad.setManaged(false);
            }
            
            // Actualizar horas de fin disponibles
            actualizarHorasFinDisponibles();
            
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
    /**
     * Actualiza las horas de fin disponibles (solo posteriores a la hora de inicio)
     */
    private void actualizarHorasFinDisponibles() {
        if (horaInicioSeleccionada == null) {
            return;
        }
        
        try {
            LocalTime inicio = LocalTime.parse(horaInicioSeleccionada, formateadorHora);
            
            ObservableList<String> horasFinDisponibles = FXCollections.observableArrayList();
            for (String horario : listaHorarios) {
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
        comboHoraFin.getSelectionModel().clearSelection();
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
            String horaFinStr = comboHoraFin.getSelectionModel().getSelectedItem();
            String motivo = campoMotivo.getText().trim();
            int cantidadPersonas = spinnerCantidadPersonas.getValue();
            
            // Construir fechas completas
            LocalTime horaInicio = LocalTime.parse(horaInicioSeleccionada, formateadorHora);
            LocalTime horaFin = LocalTime.parse(horaFinStr, formateadorHora);
            
            LocalDateTime fechaHoraInicio = LocalDateTime.of(fechaSeleccionada, horaInicio);
            LocalDateTime fechaHoraFin = LocalDateTime.of(fechaSeleccionada, horaFin);
            
            // Verificar que no haya conflictos
            if (existeConflicto(salaSeleccionada.getId(), fechaHoraInicio, fechaHoraFin)) {
                mostrarError("Ya existe una reserva para este horario en la sala seleccionada.");
                return false;
            }
            
            // Insertar la reserva en la base de datos
            ConexionBD conexionBD = ConexionBD.obtenerInstancia();
            Connection conexion = conexionBD.obtenerConexion();
            
            String sql = "INSERT INTO reservations (room_id, user_id, start_at, end_at, people_count, reason, status_id) " +
                        "VALUES (?, ?, ?, ?, ?, ?, 1)"; // status_id = 1 (PENDING)
            
            try (PreparedStatement statement = conexion.prepareStatement(sql)) {
                statement.setLong(1, salaSeleccionada.getId());
                statement.setLong(2, sesion.getUsuarioId());
                statement.setTimestamp(3, java.sql.Timestamp.valueOf(fechaHoraInicio));
                statement.setTimestamp(4, java.sql.Timestamp.valueOf(fechaHoraFin));
                statement.setInt(5, cantidadPersonas);
                statement.setString(6, motivo.isEmpty() ? null : motivo);
                
                int filasAfectadas = statement.executeUpdate();
                
                if (filasAfectadas > 0) {
                    mostrarAlertaConfirmacion("Éxito", "Reserva confirmada", 
                                             "Su reserva ha sido creada exitosamente.\n\n" +
                                             "Sala: " + salaSeleccionada.getNombre() + "\n" +
                                             "Fecha: " + fechaSeleccionada.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")) + "\n" +
                                             "Horario: " + horaInicioSeleccionada + " - " + horaFinStr);
                    
                    // Navegar de vuelta al calendario después de que el usuario cierre la alerta
                    Platform.runLater(() -> {
                        GestorNavegacionUsuario gestorNavegacion = GestorNavegacionUsuario.obtenerInstancia();
                        gestorNavegacion.navegarAVistaNuevaReserva();
                    });
                    
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
    
    /**
     * Muestra una alerta de confirmación al usuario
     */
    private void mostrarAlertaConfirmacion(String titulo, String encabezado, String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setTitle(titulo);
        alerta.setHeaderText(encabezado);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}

