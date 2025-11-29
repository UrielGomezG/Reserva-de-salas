package interfaz.sara.Controladores.Usuario;

import interfaz.sara.ConexionBD.ConexionBD;
import interfaz.sara.Modelo.Sala;
import interfaz.sara.Utilidades.GestorNavegacion;
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

    // ========== Variables de estado ==========
    
    /** Fecha actual del calendario */
    private LocalDate fechaActual;
    
    /** Fecha seleccionada por el usuario */
    private LocalDate fechaSeleccionada;
    
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
        // Establecer la fecha actual
        fechaActual = LocalDate.now();
        fechaSeleccionada = null;
        
        // Actualizar el navbar para marcar "Nueva Reserva" como activo
        actualizarNavbarActivo();
        
        // Generar el calendario del mes actual
        generarCalendario();
        
        // Actualizar la etiqueta de fecha seleccionada
        actualizarEtiquetaFecha();
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
     */
    @FXML
    private void manejarMesAnterior() {
        fechaActual = fechaActual.minusMonths(1);
        generarCalendario();
    }
    
    /**
     * Maneja el clic en la flecha derecha para ir al mes siguiente
     */
    @FXML
    private void manejarMesSiguiente() {
        fechaActual = fechaActual.plusMonths(1);
        generarCalendario();
    }
    
    /**
     * Maneja el clic en el botón "Nueva Reserva"
     * Navega al formulario para crear una nueva reserva
     */
    @FXML
    private void manejarNuevaReserva() {
        GestorNavegacion gestorNavegacion = GestorNavegacion.obtenerInstancia();
        gestorNavegacion.navegarAVistaCrearReserva();
    }
    
    /**
     * Maneja la selección de un horario específico para crear una reserva
     * Navega al formulario de crear reserva con el horario pre-seleccionado
     * 
     * @param horario El horario seleccionado (formato HH:mm)
     * @param salaId El ID de la sala seleccionada
     */
    private void seleccionarHorario(String horario, Long salaId) {
        // TODO: Pasar parámetros a la vista de crear reserva
        GestorNavegacion gestorNavegacion = GestorNavegacion.obtenerInstancia();
        gestorNavegacion.navegarAVistaCrearReserva();
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
        
        // Estilo especial para el día actual
        if (fecha.equals(LocalDate.now())) {
            label.getStyleClass().add("calendar-day-today");
        }
        
        // Estilo especial para la fecha seleccionada
        if (fecha.equals(fechaSeleccionada)) {
            label.getStyleClass().add("calendar-day-selected");
        }
        
        // Agregar evento de clic
        label.setOnMouseClicked(e -> seleccionarFecha(fecha));
        
        return label;
    }
    
    /**
     * Selecciona una fecha y actualiza la visualización
     * 
     * @param fecha La fecha seleccionada
     */
    private void seleccionarFecha(LocalDate fecha) {
        fechaSeleccionada = fecha;
        generarCalendario(); // Regenerar para aplicar el estilo de selección
        actualizarEtiquetaFecha();
        cargarDisponibilidadSalas();
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
     * Carga y muestra la disponibilidad de las salas para la fecha seleccionada
     */
    private void cargarDisponibilidadSalas() {
        if (fechaSeleccionada == null) {
            return;
        }
        
        // Limpiar el contenedor
        contenedorDisponibilidad.getChildren().clear();
        
        try {
            // Obtener todas las salas habilitadas
            List<Sala> salas = obtenerSalasHabilitadas();
            
            if (salas.isEmpty()) {
                Label mensajeVacio = new Label("No hay salas disponibles");
                mensajeVacio.getStyleClass().add("empty-message");
                contenedorDisponibilidad.getChildren().add(mensajeVacio);
                return;
            }
            
            // Para cada sala, mostrar su disponibilidad
            for (Sala sala : salas) {
                VBox tarjetaSala = crearTarjetaDisponibilidadSala(sala);
                contenedorDisponibilidad.getChildren().add(tarjetaSala);
            }
            
        } catch (SQLException e) {
            System.err.println("Error al cargar disponibilidad de salas: " + e.getMessage());
            e.printStackTrace();
            Label mensajeError = new Label("Error al cargar la disponibilidad de salas");
            mensajeError.getStyleClass().add("empty-message");
            contenedorDisponibilidad.getChildren().add(mensajeError);
        }
    }
    
    /**
     * Crea una tarjeta que muestra la disponibilidad de una sala
     * 
     * @param sala La sala a mostrar
     * @return VBox con la tarjeta de disponibilidad
     */
    private VBox crearTarjetaDisponibilidadSala(Sala sala) {
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
        
        // Horarios disponibles (simplificado - mostrar horarios de 8:00 a 20:00)
        VBox horariosBox = new VBox(5);
        horariosBox.getStyleClass().add("time-slots-container");
        
        // Obtener horarios ocupados para esta sala en la fecha seleccionada
        List<String> horariosOcupados = obtenerHorariosOcupados(sala.getId(), fechaSeleccionada);
        
        // Crear bloques de horarios
        HBox horariosRow = new HBox(10);
        for (int hora = 8; hora < 21; hora++) {
            String horario = String.format("%02d:00", hora);
            boolean ocupado = horariosOcupados.contains(horario);
            
            Label bloqueHorario = crearBloqueHorario(horario, ocupado, sala.getId());
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
     * @param ocupado true si el horario está ocupado, false si está disponible
     * @param salaId El ID de la sala
     * @return Label configurado como bloque de horario
     */
    private Label crearBloqueHorario(String horario, boolean ocupado, Long salaId) {
        Label bloque = new Label(horario);
        bloque.setAlignment(Pos.CENTER);
        bloque.setMinSize(80, 35);
        bloque.setPrefSize(80, 35);
        bloque.setMaxSize(80, 35);
        
        if (ocupado) {
            bloque.getStyleClass().add("time-slot-occupied");
            bloque.setDisable(true);
        } else {
            bloque.getStyleClass().add("time-slot-available");
            bloque.setOnMouseClicked(e -> seleccionarHorario(horario, salaId));
        }
        
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
     * Obtiene los horarios ocupados para una sala en una fecha específica
     * 
     * @param salaId El ID de la sala
     * @param fecha La fecha a consultar
     * @return Lista de horarios ocupados (formato HH:mm)
     * @throws SQLException Si ocurre un error al consultar la base de datos
     */
    private List<String> obtenerHorariosOcupados(Long salaId, LocalDate fecha) {
        List<String> horariosOcupados = new ArrayList<>();
        ConexionBD conexionBD = ConexionBD.obtenerInstancia();
        Connection conexion = null;
        
        try {
            conexion = conexionBD.obtenerConexion();
            
            // Consultar reservas confirmadas o pendientes para esta sala en esta fecha
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
                            
                            java.time.LocalDateTime inicio = resultado.getTimestamp("start_at").toLocalDateTime();
                            java.time.LocalDateTime fin = resultado.getTimestamp("end_at").toLocalDateTime();
                            
                            // Agregar cada hora ocupada entre inicio y fin
                            java.time.LocalDateTime horaActual = inicio;
                            while (horaActual.isBefore(fin)) {
                                String horario = horaActual.format(DateTimeFormatter.ofPattern("HH:mm"));
                                if (!horariosOcupados.contains(horario)) {
                                    horariosOcupados.add(horario);
                                }
                                horaActual = horaActual.plusHours(1);
                            }
                        }
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener horarios ocupados: " + e.getMessage());
            e.printStackTrace();
        }
        
        return horariosOcupados;
    }
}

