package interfaz.sara.Controladores.Usuario;

import interfaz.sara.ConexionBD.ConexionBD;
import interfaz.sara.Modelo.Sala;
import interfaz.sara.Utilidades.GestorNavegacionUsuario;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Controlador para la vista de nueva reserva (VistaNuevaReserva.fxml)
 * Gestiona el calendario y la visualización de disponibilidad de salas
 */
public class ControladorNuevaReserva {

    // ========== Componentes FXML ==========
    
    @FXML
    private Label labelMesActual;
    
    @FXML
    private GridPane gridCalendario;
    
    @FXML
    private Label labelFechaSeleccionada;
    
    @FXML
    private VBox contenedorDisponibilidad;
    
    @FXML
    private VBox navbarInclude;
    
    @FXML
    private Button botonVolver;

    // ========== Variables de estado ==========
    
    /** Fecha actual del calendario */
    private LocalDate fechaActual;
    
    /** Fecha seleccionada por el usuario */
    private LocalDate fechaSeleccionada;
    
    /** Sala seleccionada */
    private Sala salaSeleccionada;
    
    /** Hora seleccionada (formato HH:mm) */
    private String horaSeleccionada;
    
    /** Estado actual: 0 = mostrar salas, 1 = mostrar calendario */
    private int estadoActual = 0;
    
    /** Formateador para la fecha seleccionada */
    private static final DateTimeFormatter formateadorFecha = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    
    /** Nombres de los meses en español */
    private static final String[] NOMBRES_MESES = {
        "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
        "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
    };

    // ========== Métodos de inicialización ==========
    
    /**
     * Inicializa el controlador después de que se carga el FXML
     * Configura el calendario con el mes actual
     */
    @FXML
    private void initialize() {
        // Inicializar estado
        estadoActual = 0; // Empezar mostrando salas
        salaSeleccionada = null;
        horaSeleccionada = null;
        fechaSeleccionada = null;
        
        // Establecer la fecha actual del calendario (asegurar que esté dentro de los límites)
        fechaActual = LocalDate.now();
        LocalDate limiteInferior = LocalDate.now().minusWeeks(2);
        LocalDate limiteSuperior = LocalDate.now().plusWeeks(4);
        
        if (fechaActual.isBefore(limiteInferior)) {
            fechaActual = limiteInferior;
        } else if (fechaActual.isAfter(limiteSuperior)) {
            fechaActual = limiteSuperior;
        }
        
        // Actualizar el navbar para marcar "Nueva Reserva" como activo
        actualizarNavbarActivo();
        
        // Mostrar primero las salas disponibles
        mostrarVistaSalas();
    }
    
    /**
     * Actualiza el navbar para marcar "Nueva Reserva" como vista activa
     */
    private void actualizarNavbarActivo() {
        Platform.runLater(() -> {
            if (navbarInclude != null) {
                // Buscar los botones del navbar usando lookup
                Button btnMisReservas = (Button) navbarInclude.lookup("#botonMisReservas");
                Button btnNuevaReserva = (Button) navbarInclude.lookup("#botonNuevaReserva");
                Button btnPerfil = (Button) navbarInclude.lookup("#botonPerfil");
                
                if (btnMisReservas != null && btnNuevaReserva != null && btnPerfil != null) {
                    // Resetear todos los botones
                    btnMisReservas.getStyleClass().remove("navbar-button-active");
                    btnNuevaReserva.getStyleClass().remove("navbar-button-active");
                    btnPerfil.getStyleClass().remove("navbar-button-active");
                    
                    if (!btnMisReservas.getStyleClass().contains("navbar-button")) {
                        btnMisReservas.getStyleClass().add("navbar-button");
                    }
                    if (!btnPerfil.getStyleClass().contains("navbar-button")) {
                        btnPerfil.getStyleClass().add("navbar-button");
                    }
                    
                    // Activar el botón de Nueva Reserva
                    btnNuevaReserva.getStyleClass().remove("navbar-button");
                    if (!btnNuevaReserva.getStyleClass().contains("navbar-button-active")) {
                        btnNuevaReserva.getStyleClass().add("navbar-button-active");
                    }
                }
            }
        });
    }

    // ========== Métodos de manejo de eventos - Navegación del calendario ==========
    
    /**
     * Maneja el clic en la flecha izquierda para ir al mes anterior
     * Limita la navegación a 2 semanas pasadas desde hoy
     * Solo funciona cuando se está en vista de calendario
     */
    @FXML
    private void manejarMesAnterior() {
        if (estadoActual != 1) {
            return; // Solo funciona en vista de calendario
        }
        
        LocalDate limiteInferior = LocalDate.now().minusWeeks(2);
        LocalDate nuevaFecha = fechaActual.minusMonths(1);
        
        if (nuevaFecha.isBefore(limiteInferior)) {
            fechaActual = limiteInferior;
        } else {
            fechaActual = nuevaFecha;
        }
        generarCalendario();
    }
    
    /**
     * Maneja el clic en la flecha derecha para ir al mes siguiente
     * Limita la navegación a 4 semanas futuras desde hoy
     * Solo funciona cuando se está en vista de calendario
     */
    @FXML
    private void manejarMesSiguiente() {
        if (estadoActual != 1) {
            return; // Solo funciona en vista de calendario
        }
        
        LocalDate limiteSuperior = LocalDate.now().plusWeeks(4);
        LocalDate nuevaFecha = fechaActual.plusMonths(1);
        
        if (nuevaFecha.isAfter(limiteSuperior)) {
            fechaActual = limiteSuperior;
        } else {
            fechaActual = nuevaFecha;
        }
        generarCalendario();
    }
    
    /**
     * Maneja el botón para volver a la vista de salas
     */
    @FXML
    private void manejarVolverASalas() {
        estadoActual = 0;
        salaSeleccionada = null;
        horaSeleccionada = null;
        mostrarVistaSalas();
    }
    
    /**
     * Maneja la selección de un horario específico
     * Muestra el calendario con fechas disponibles para esa hora
     * 
     * @param horario El horario seleccionado (formato HH:mm)
     * @param salaId El ID de la sala seleccionada
     */
    private void seleccionarHorario(String horario, Long salaId) {
        // Buscar la sala seleccionada
        try {
            List<Sala> salas = obtenerSalasHabilitadas();
            for (Sala sala : salas) {
                if (sala.getId().equals(salaId)) {
                    salaSeleccionada = sala;
                    horaSeleccionada = horario;
                    break;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener sala: " + e.getMessage());
            return;
        }
        
        if (salaSeleccionada == null) {
            return;
        }
        
        // Cambiar a vista de calendario
        estadoActual = 1;
        mostrarVistaCalendario();
    }

    // ========== Métodos de generación del calendario ==========
    
    /**
     * Genera y muestra el calendario del mes actual
     */
    private void generarCalendario() {
        // Limpiar el grid del calendario
        gridCalendario.getChildren().clear();
        
        // Actualizar el label del mes
        String nombreMes = NOMBRES_MESES[fechaActual.getMonthValue() - 1];
        labelMesActual.setText(nombreMes + " " + fechaActual.getYear());
        
        // Obtener el primer día del mes y el día de la semana
        LocalDate primerDiaMes = fechaActual.withDayOfMonth(1);
        int diaSemanaInicio = primerDiaMes.getDayOfWeek().getValue() % 7; // 0=Domingo, 1=Lunes, etc.
        
        // Obtener el número de días en el mes
        int diasEnMes = fechaActual.lengthOfMonth();
        
        // Llenar el calendario
        int dia = 1;
        for (int fila = 0; fila < 6; fila++) {
            for (int columna = 0; columna < 7; columna++) {
                if (fila == 0 && columna < diaSemanaInicio) {
                    // Celda vacía antes del primer día del mes
                    continue;
                }
                
                if (dia <= diasEnMes) {
                    Label labelDia = crearLabelDia(dia, fila, columna);
                    gridCalendario.add(labelDia, columna, fila);
                    dia++;
                }
            }
        }
    }
    
    /**
     * Crea un Label para un día específico del calendario
     * Solo muestra como disponible si la hora seleccionada está libre en esa fecha
     * 
     * @param dia El número del día
     * @param fila La fila en el grid
     * @param columna La columna en el grid
     * @return Label configurado para el día
     */
    private Label crearLabelDia(int dia, int fila, int columna) {
        Label label = new Label(String.valueOf(dia));
        label.getStyleClass().add("calendar-day");
        label.setAlignment(Pos.CENTER);
        label.setMinSize(40, 40);
        label.setPrefSize(40, 40);
        label.setMaxSize(40, 40);
        
        // Crear la fecha correspondiente
        LocalDate fecha = fechaActual.withDayOfMonth(dia);
        LocalDate hoy = LocalDate.now();
        
        // Verificar límites de navegación
        LocalDate limiteInferior = hoy.minusWeeks(2);
        LocalDate limiteSuperior = hoy.plusWeeks(4);
        
        // Deshabilitar días pasados
        if (fecha.isBefore(hoy)) {
            label.getStyleClass().add("calendar-day-disabled");
            label.setDisable(true);
            return label;
        }
        
        // Verificar límites de navegación
        if (fecha.isBefore(limiteInferior) || fecha.isAfter(limiteSuperior)) {
            label.getStyleClass().add("calendar-day-disabled");
            label.setDisable(true);
            return label;
        }
        
        // Verificar el estado de la reserva para esta hora y fecha
        Integer estadoReserva = null; // null = disponible, 1 = pendiente, 2 = confirmada
        if (salaSeleccionada != null && horaSeleccionada != null) {
            estadoReserva = obtenerEstadoReserva(salaSeleccionada.getId(), fecha, horaSeleccionada);
        }
        
        // Si no hay sala/hora seleccionada, no se puede seleccionar fecha
        if (salaSeleccionada == null || horaSeleccionada == null) {
            label.getStyleClass().add("calendar-day-disabled");
            label.setDisable(true);
        } else if (estadoReserva != null) {
            // Hay una reserva para esta hora
            if (estadoReserva == 1) {
                // Reserva pendiente - mostrar en azul
                label.getStyleClass().add("calendar-day-pending");
                label.setDisable(true);
            } else if (estadoReserva == 2) {
                // Reserva confirmada - mostrar en rojo
                label.getStyleClass().add("calendar-day-occupied");
                label.setDisable(true);
            }
        } else {
            // Fecha disponible y seleccionable - mostrar en verde
            label.getStyleClass().add("calendar-day-available");
            
            // Estilo especial para el día actual (sobrescribe el verde pero mantiene la disponibilidad)
            if (fecha.equals(hoy)) {
                label.getStyleClass().add("calendar-day-today");
            }
            
            // Agregar evento de clic
            label.setOnMouseClicked(e -> seleccionarFecha(fecha));
        }
        
        return label;
    }
    
    /**
     * Selecciona una fecha y redirige a crear reserva
     * 
     * @param fecha La fecha seleccionada
     */
    private void seleccionarFecha(LocalDate fecha) {
        if (salaSeleccionada == null || horaSeleccionada == null) {
            return;
        }
        
        fechaSeleccionada = fecha;
        
        // Redirigir a crear reserva con los datos seleccionados
        GestorNavegacionUsuario gestor = GestorNavegacionUsuario.obtenerInstancia();
        gestor.navegarAVistaCrearReserva(salaSeleccionada.getId(), horaSeleccionada, fechaSeleccionada);
    }

    // ========== Métodos de visualización ==========
    
    /**
     * Actualiza la etiqueta que muestra la fecha seleccionada
     */
    private void actualizarEtiquetaFecha() {
        if (fechaSeleccionada != null) {
            labelFechaSeleccionada.setText(fechaSeleccionada.format(formateadorFecha));
        } else {
            labelFechaSeleccionada.setText("Seleccione una fecha");
        }
    }
    
    /**
     * Muestra la vista de salas (estado inicial)
     */
    private void mostrarVistaSalas() {
        // Ocultar calendario
        gridCalendario.setVisible(false);
        gridCalendario.setManaged(false);
        labelMesActual.setVisible(false);
        labelMesActual.setManaged(false);
        
        // Ocultar botón volver
        if (botonVolver != null) {
            botonVolver.setVisible(false);
            botonVolver.setManaged(false);
        }
        
        // Limpiar y mostrar salas
        contenedorDisponibilidad.getChildren().clear();
        labelFechaSeleccionada.setText("Seleccione una sala y hora");
        
        try {
            List<Sala> salas = obtenerSalasHabilitadas();
            
            if (salas.isEmpty()) {
                Label mensajeVacio = new Label("No hay salas disponibles");
                mensajeVacio.getStyleClass().add("empty-message");
                contenedorDisponibilidad.getChildren().add(mensajeVacio);
                return;
            }
            
            // Para cada sala, mostrar sus horarios disponibles
            for (Sala sala : salas) {
                VBox tarjetaSala = crearTarjetaSalaConHorarios(sala);
                contenedorDisponibilidad.getChildren().add(tarjetaSala);
            }
            
        } catch (SQLException e) {
            System.err.println("Error al cargar salas: " + e.getMessage());
            e.printStackTrace();
            Label mensajeError = new Label("Error al cargar las salas");
            mensajeError.getStyleClass().add("empty-message");
            contenedorDisponibilidad.getChildren().add(mensajeError);
        }
    }
    
    /**
     * Muestra la vista de calendario (después de seleccionar hora)
     */
    private void mostrarVistaCalendario() {
        // Mostrar calendario
        gridCalendario.setVisible(true);
        gridCalendario.setManaged(true);
        labelMesActual.setVisible(true);
        labelMesActual.setManaged(true);
        
        // Mostrar botón volver
        if (botonVolver != null) {
            botonVolver.setVisible(true);
            botonVolver.setManaged(true);
        }
        
        // Actualizar etiqueta
        if (salaSeleccionada != null && horaSeleccionada != null) {
            labelFechaSeleccionada.setText("Sala: " + salaSeleccionada.getNombre() + " - Hora: " + horaSeleccionada + " - Seleccione una fecha");
        }
        
        // Generar calendario
        generarCalendario();
        
        // Limpiar contenedor de disponibilidad (ya no se usa en este estado)
        contenedorDisponibilidad.getChildren().clear();
    }
    
    /**
     * Crea una tarjeta que muestra una sala con sus horarios disponibles
     * 
     * @param sala La sala a mostrar
     * @return VBox con la tarjeta de la sala
     */
    private VBox crearTarjetaSalaConHorarios(Sala sala) {
        VBox tarjeta = new VBox(10);
        tarjeta.getStyleClass().add("room-availability-card");
        tarjeta.setPadding(new Insets(15));
        
        // Encabezado de la sala
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        
        Label nombreSala = new Label(sala.getNombre());
        nombreSala.getStyleClass().add("room-name");
        
        Label tipoSala = new Label(sala.getTipoSala());
        tipoSala.getStyleClass().add("room-type");
        
        Label capacidad = new Label("Capacidad: " + sala.getCapacidad());
        capacidad.getStyleClass().add("room-capacity");
        
        HBox spacer = new HBox();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
        
        header.getChildren().addAll(nombreSala, tipoSala, spacer, capacidad);
        
        // Horarios disponibles (8:00 a 20:00)
        VBox horariosBox = new VBox(5);
        horariosBox.getStyleClass().add("time-slots-container");
        
        // Crear bloques de horarios (todos disponibles inicialmente)
        HBox horariosRow = new HBox(10);
        for (int hora = 8; hora < 21; hora++) {
            String horario = String.format("%02d:00", hora);
            
            Label bloqueHorario = crearBloqueHorario(horario, false, sala.getId());
            horariosRow.getChildren().add(bloqueHorario);
            
            // Nueva fila cada 6 horarios
            if ((hora - 7) % 6 == 0 && hora < 20) {
                horariosBox.getChildren().add(horariosRow);
                horariosRow = new HBox(10);
            }
        }
        if (!horariosRow.getChildren().isEmpty()) {
            horariosBox.getChildren().add(horariosRow);
        }
        
        tarjeta.getChildren().addAll(header, horariosBox);
        return tarjeta;
    }
    
    /**
     * Crea un bloque visual para un horario específico
     * 
     * @param horario El horario (formato HH:mm)
     * @param ocupado true si el horario está ocupado, false si está disponible (no se usa en el nuevo flujo)
     * @param salaId El ID de la sala
     * @return Label configurado como bloque de horario
     */
    private Label crearBloqueHorario(String horario, boolean ocupado, Long salaId) {
        Label bloque = new Label(horario);
        bloque.setAlignment(Pos.CENTER);
        bloque.setMinSize(80, 35);
        bloque.setPrefSize(80, 35);
        bloque.setMaxSize(80, 35);
        
        bloque.getStyleClass().add("time-slot-available");
        bloque.setOnMouseClicked(e -> seleccionarHorario(horario, salaId));
        
        return bloque;
    }

    // ========== Métodos de acceso a base de datos ==========
    
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
     * Verifica si una hora específica está disponible en una fecha para una sala
     * 
     * @param salaId El ID de la sala
     * @param fecha La fecha a consultar
     * @param hora La hora a verificar (formato HH:mm)
     * @return true si la hora está disponible, false si está ocupada
     */
    /**
     * Obtiene el estado de la reserva para una hora específica
     * 
     * @param salaId El ID de la sala
     * @param fecha La fecha a verificar
     * @param hora La hora a verificar (formato HH:mm)
     * @return null si está disponible, 1 si hay reserva pendiente, 2 si hay reserva confirmada
     */
    private Integer obtenerEstadoReserva(Long salaId, LocalDate fecha, String hora) {
        ConexionBD conexionBD = ConexionBD.obtenerInstancia();
        
        try {
            Connection conexion = conexionBD.obtenerConexion();
            
            // Parsear la hora
            java.time.LocalTime horaLocal = java.time.LocalTime.parse(hora, DateTimeFormatter.ofPattern("HH:mm"));
            java.time.LocalDateTime fechaHoraInicio = LocalDateTime.of(fecha, horaLocal);
            java.time.LocalDateTime fechaHoraFin = fechaHoraInicio.plusHours(1);
            
            // Consultar si hay reservas que ocupen esta hora
            // Retornar el status_id de la reserva si existe
            // Prioridad: si hay una confirmada, mostrar confirmada; si solo hay pendientes, mostrar pendiente
            String sql = "SELECT status_id " +
                        "FROM reservations " +
                        "WHERE room_id = ? " +
                        "AND status_id IN (1, 2) " + // PENDING o CONFIRMED
                        "AND DATE(start_at) = ? " +
                        "AND start_at < ? " + // La reserva comienza antes del fin de esta hora
                        "AND end_at >= ? " + // La reserva termina en o después del inicio de esta hora
                        "ORDER BY status_id DESC " + // Priorizar CONFIRMED (2) sobre PENDING (1)
                        "LIMIT 1";
            
            try (PreparedStatement statement = conexion.prepareStatement(sql)) {
                statement.setLong(1, salaId);
                statement.setDate(2, java.sql.Date.valueOf(fecha));
                statement.setTimestamp(3, java.sql.Timestamp.valueOf(fechaHoraFin));
                statement.setTimestamp(4, java.sql.Timestamp.valueOf(fechaHoraInicio));
                
                try (ResultSet resultado = statement.executeQuery()) {
                    if (resultado.next()) {
                        return resultado.getInt("status_id");
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al verificar estado de reserva: " + e.getMessage());
            e.printStackTrace();
            return null; // En caso de error, asumir disponible
        }
        
        return null; // No hay reserva, está disponible
    }
}

