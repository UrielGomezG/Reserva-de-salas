package interfaz.sara.Controladores.Usuario;

import interfaz.sara.ConexionBD.ConexionBD;
import interfaz.sara.Modelo.Reserva;
import interfaz.sara.Utilidades.SesionUsuario;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.HBox;
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
 * Controlador para la vista principal del usuario (VistaPrincipalUsuario.fxml)
 * Gestiona la visualización y filtrado de las reservas del usuario
 */
public class ControladorPrincipalUsuario {

    // ========== Componentes FXML - Pestañas de filtro ==========
    
    @FXML
    private VBox tabProximasContainer;
    
    @FXML
    private Label tabProximasLabel;
    
    @FXML
    private Separator tabProximasSeparator;
    
    @FXML
    private VBox tabPasadasContainer;
    
    @FXML
    private Label tabPasadasLabel;
    
    @FXML
    private Separator tabPasadasSeparator;
    
    @FXML
    private VBox tabTodasContainer;
    
    @FXML
    private Label tabTodasLabel;
    
    @FXML
    private Separator tabTodasSeparator;
    
    // ========== Componentes FXML - Contenido ==========
    
    @FXML
    private VBox contenedorListaReservas;
    
    @FXML
    private VBox navbarInclude;

    // ========== Variables de estado ==========
    
    /** Tipo de filtro activo actualmente */
    private TipoFiltro filtroActivo = TipoFiltro.PROXIMAS;
    
    /** Formateador de fechas para mostrar */
    private static final DateTimeFormatter formateadorFecha = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    
    // ========== Enumeración para tipos de filtro ==========
    
    /**
     * Enum que representa los diferentes tipos de filtro disponibles
     */
    private enum TipoFiltro {
        PROXIMAS,   // Reservas próximas
        PASADAS,    // Reservas pasadas
        TODAS       // Todas las reservas
    }

    // ========== Métodos de inicialización ==========
    
    /**
     * Inicializa el controlador después de que se carga el FXML
     * Configura el estado inicial y carga las reservas
     */
    @FXML
    private void initialize() {
        // Actualizar el navbar para marcar "Mis Reservas" como activo
        actualizarNavbarActivo();
        
        // Mostrar las reservas próximas por defecto
        aplicarFiltro(TipoFiltro.PROXIMAS);
        cargarReservas();
    }
    
    /**
     * Actualiza el navbar para marcar "Mis Reservas" como vista activa
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
                    
                    if (!btnNuevaReserva.getStyleClass().contains("navbar-button")) {
                        btnNuevaReserva.getStyleClass().add("navbar-button");
                    }
                    if (!btnPerfil.getStyleClass().contains("navbar-button")) {
                        btnPerfil.getStyleClass().add("navbar-button");
                    }
                    
                    // Activar el botón de Mis Reservas
                    btnMisReservas.getStyleClass().remove("navbar-button");
                    if (!btnMisReservas.getStyleClass().contains("navbar-button-active")) {
                        btnMisReservas.getStyleClass().add("navbar-button-active");
                    }
                }
            }
        });
    }

    // ========== Métodos de manejo de eventos - Filtros ==========
    
    /**
     * Maneja el clic en la pestaña "Próximas"
     * Filtra y muestra solo las reservas próximas
     */
    @FXML
    private void manejarFiltroProximas() {
        aplicarFiltro(TipoFiltro.PROXIMAS);
        cargarReservas();
    }
    
    /**
     * Maneja el clic en la pestaña "Pasadas"
     * Filtra y muestra solo las reservas pasadas
     */
    @FXML
    private void manejarFiltroPasadas() {
        aplicarFiltro(TipoFiltro.PASADAS);
        cargarReservas();
    }
    
    /**
     * Maneja el clic en la pestaña "Todas"
     * Muestra todas las reservas sin filtrar
     */
    @FXML
    private void manejarFiltroTodas() {
        aplicarFiltro(TipoFiltro.TODAS);
        cargarReservas();
    }

    // ========== Métodos de lógica de negocio ==========
    
    /**
     * Aplica el filtro seleccionado y actualiza la apariencia de las pestañas
     * 
     * @param tipoFiltro El tipo de filtro a aplicar
     */
    private void aplicarFiltro(TipoFiltro tipoFiltro) {
        filtroActivo = tipoFiltro;
        
        // Resetear todas las pestañas a estado inactivo
        establecerPestanaActiva(tabProximasContainer, tabProximasLabel, tabProximasSeparator, false);
        establecerPestanaActiva(tabPasadasContainer, tabPasadasLabel, tabPasadasSeparator, false);
        establecerPestanaActiva(tabTodasContainer, tabTodasLabel, tabTodasSeparator, false);
        
        // Activar la pestaña seleccionada
        switch (tipoFiltro) {
            case PROXIMAS:
                establecerPestanaActiva(tabProximasContainer, tabProximasLabel, tabProximasSeparator, true);
                break;
            case PASADAS:
                establecerPestanaActiva(tabPasadasContainer, tabPasadasLabel, tabPasadasSeparator, true);
                break;
            case TODAS:
                establecerPestanaActiva(tabTodasContainer, tabTodasLabel, tabTodasSeparator, true);
                break;
        }
    }
    
    /**
     * Establece el estado activo o inactivo de una pestaña
     * 
     * @param contenedor El contenedor de la pestaña
     * @param etiqueta La etiqueta de la pestaña
     * @param separador El separador de la pestaña
     * @param activo true si debe estar activa, false en caso contrario
     */
    private void establecerPestanaActiva(VBox contenedor, Label etiqueta, Separator separador, boolean activo) {
        if (activo) {
            etiqueta.getStyleClass().remove("tab-inactive");
            if (!etiqueta.getStyleClass().contains("tab-active")) {
                etiqueta.getStyleClass().add("tab-active");
            }
            separador.setVisible(true);
        } else {
            etiqueta.getStyleClass().remove("tab-active");
            if (!etiqueta.getStyleClass().contains("tab-inactive")) {
                etiqueta.getStyleClass().add("tab-inactive");
            }
            separador.setVisible(false);
        }
    }
    
    /**
     * Carga las reservas según el filtro activo
     * Este método consulta la base de datos y muestra las reservas
     */
    private void cargarReservas() {
        // Limpiar la lista actual
        contenedorListaReservas.getChildren().clear();
        
        try {
            // Obtener las reservas de la base de datos
            List<Reserva> reservas = obtenerReservasDeBD();
            
            // Filtrar según el tipo de filtro activo
            List<Reserva> reservasFiltradas = filtrarReservas(reservas);
            
            // Mostrar las reservas
            if (reservasFiltradas.isEmpty()) {
                Label mensajeVacio = new Label("No hay reservas para mostrar");
                mensajeVacio.getStyleClass().add("empty-message");
                contenedorListaReservas.getChildren().add(mensajeVacio);
            } else {
                for (Reserva reserva : reservasFiltradas) {
                    VBox tarjetaReserva = crearTarjetaReserva(reserva);
                    contenedorListaReservas.getChildren().add(tarjetaReserva);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al cargar reservas: " + e.getMessage());
            e.printStackTrace();
            Label mensajeError = new Label("Error al cargar las reservas. Por favor, intente más tarde.");
            mensajeError.getStyleClass().add("empty-message");
            contenedorListaReservas.getChildren().add(mensajeError);
        }
    }
    
    /**
     * Obtiene todas las reservas del usuario desde la base de datos
     * 
     * @return Lista de reservas del usuario
     * @throws SQLException Si ocurre un error al consultar la base de datos
     */
    private List<Reserva> obtenerReservasDeBD() throws SQLException {
        List<Reserva> reservas = new ArrayList<>();
        
        // Obtener el ID del usuario de la sesión
        SesionUsuario sesion = SesionUsuario.obtenerInstancia();
        if (!sesion.estaAutenticado()) {
            System.err.println("Error: No hay usuario autenticado");
            return reservas;
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
                    
                    reservas.add(reserva);
                }
            }
        }
        
        return reservas;
    }
    
    /**
     * Filtra las reservas según el tipo de filtro activo
     * 
     * @param reservas Lista completa de reservas
     * @return Lista de reservas filtradas
     */
    private List<Reserva> filtrarReservas(List<Reserva> reservas) {
        List<Reserva> reservasFiltradas = new ArrayList<>();
        LocalDateTime ahora = LocalDateTime.now();
        
        for (Reserva reserva : reservas) {
            switch (filtroActivo) {
                case PROXIMAS:
                    if (reserva.getFechaInicio() != null && reserva.getFechaInicio().isAfter(ahora)) {
                        reservasFiltradas.add(reserva);
                    }
                    break;
                case PASADAS:
                    if (reserva.getFechaFin() != null && reserva.getFechaFin().isBefore(ahora)) {
                        reservasFiltradas.add(reserva);
                    }
                    break;
                case TODAS:
                    reservasFiltradas.add(reserva);
                    break;
            }
        }
        
        return reservasFiltradas;
    }
    
    /**
     * Crea una tarjeta visual para mostrar una reserva
     * 
     * @param reserva La reserva a mostrar
     * @return VBox con la tarjeta de la reserva
     */
    private VBox crearTarjetaReserva(Reserva reserva) {
        VBox tarjeta = new VBox(10);
        tarjeta.getStyleClass().add("booking-card");
        tarjeta.setPadding(new Insets(20));
        
        // Header con nombre de sala y estado
        HBox header = new HBox(10);
        header.setStyle("-fx-alignment: center-left;");
        
        Label nombreSala = new Label(reserva.getNombreSala() != null ? reserva.getNombreSala() : "Sala desconocida");
        nombreSala.getStyleClass().add("booking-room-name");
        
        Label estado = new Label(reserva.getEstadoReserva() != null ? reserva.getEstadoReserva() : "Estado desconocido");
        estado.getStyleClass().add("booking-status-" + obtenerClaseEstado(reserva.getCodigoEstado()));
        
        HBox spacer = new HBox();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
        
        header.getChildren().addAll(nombreSala, spacer, estado);
        
        // Información de fechas
        VBox fechasBox = new VBox(5);
        if (reserva.getFechaInicio() != null) {
            Label fechaInicio = new Label("Inicio: " + reserva.getFechaInicio().format(formateadorFecha));
            fechaInicio.getStyleClass().add("booking-date");
            fechasBox.getChildren().add(fechaInicio);
        }
        if (reserva.getFechaFin() != null) {
            Label fechaFin = new Label("Fin: " + reserva.getFechaFin().format(formateadorFecha));
            fechaFin.getStyleClass().add("booking-date");
            fechasBox.getChildren().add(fechaFin);
        }
        
        // Información adicional
        HBox infoBox = new HBox(20);
        if (reserva.getCantidadPersonas() != null) {
            Label personas = new Label(reserva.getCantidadPersonas() + " persona(s)");
            personas.getStyleClass().add("booking-info");
            infoBox.getChildren().add(personas);
        }
        if (reserva.getMotivo() != null && !reserva.getMotivo().isEmpty()) {
            Label motivo = new Label("Motivo: " + reserva.getMotivo());
            motivo.getStyleClass().add("booking-info");
            infoBox.getChildren().add(motivo);
        }
        
        // Agregar todos los elementos a la tarjeta
        tarjeta.getChildren().addAll(header, fechasBox, infoBox);
        
        // Si está cancelada, mostrar motivo de cancelación
        if (reserva.estaCancelada() && reserva.getMotivoCancelacion() != null) {
            Label motivoCancelacion = new Label("Cancelación: " + reserva.getMotivoCancelacion());
            motivoCancelacion.getStyleClass().add("booking-cancellation");
            tarjeta.getChildren().add(motivoCancelacion);
        } else if (!reserva.estaCancelada() && reserva.esProxima()) {
            // Agregar botón de cancelar solo para reservas futuras no canceladas
            Button btnCancelar = new Button("Cancelar Reserva");
            btnCancelar.getStyleClass().add("button-secondary-new");
            btnCancelar.setOnAction(e -> manejarCancelarReserva(reserva));
            tarjeta.getChildren().add(btnCancelar);
        }
        
        return tarjeta;
    }
    
    /**
     * Obtiene la clase CSS para el estado de la reserva
     * 
     * @param codigoEstado El código del estado
     * @return String con la clase CSS
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
    
    // ========== Métodos auxiliares ==========
    
    /**
     * Obtiene el filtro actualmente activo
     * 
     * @return El tipo de filtro activo
     */
    public TipoFiltro getFiltroActivo() {
        return filtroActivo;
    }
    
    /**
     * Maneja la cancelación de una reserva por el usuario
     * Valida que haya al menos 2 horas de antelación
     * 
     * @param reserva La reserva a cancelar
     */
    private void manejarCancelarReserva(Reserva reserva) {
        if (reserva.getFechaInicio() == null) {
            mostrarAlerta("Error", "Error al cancelar", 
                         "No se puede cancelar esta reserva. Fecha de inicio no disponible.", 
                         Alert.AlertType.ERROR);
            return;
        }
        
        // Validar que haya al menos 2 horas de antelación
        LocalDateTime ahora = LocalDateTime.now();
        LocalDateTime fechaInicio = reserva.getFechaInicio();
        long horasRestantes = java.time.Duration.between(ahora, fechaInicio).toHours();
        
        if (horasRestantes < 2) {
            mostrarAlerta("Error", "No se puede cancelar", 
                         "No se puede cancelar la reserva con menos de 2 horas de antelación.\n\n" +
                         "Tiempo restante: " + horasRestantes + " hora(s)", 
                         Alert.AlertType.WARNING);
            return;
        }
        
        // Solicitar motivo de cancelación (opcional)
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Cancelar Reserva");
        dialog.setHeaderText("¿Desea cancelar esta reserva?");
        dialog.setContentText("Motivo de cancelación (opcional):");
        
        dialog.showAndWait().ifPresent(motivo -> {
            // Confirmar cancelación
            Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
            confirmacion.setTitle("Confirmar Cancelación");
            confirmacion.setHeaderText("¿Cancelar esta reserva?");
            confirmacion.setContentText("Esta acción no se puede deshacer.");
            
            confirmacion.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    cancelarReserva(reserva, motivo);
                }
            });
        });
    }
    
    /**
     * Cancela la reserva en la base de datos
     * 
     * @param reserva La reserva a cancelar
     * @param motivo El motivo de cancelación
     */
    private void cancelarReserva(Reserva reserva, String motivo) {
        ConexionBD conexionBD = ConexionBD.obtenerInstancia();
        SesionUsuario sesion = SesionUsuario.obtenerInstancia();
        
        String sql = "UPDATE reservations SET " +
                    "status_id = (SELECT id FROM reservation_status WHERE code = 'CANCELLED_USER' LIMIT 1), " +
                    "cancelled_by_user_id = ?, " +
                    "cancellation_reason = ? " +
                    "WHERE id = ?";
        
        try {
            Connection conexion = conexionBD.obtenerConexion();
            try (PreparedStatement statement = conexion.prepareStatement(sql)) {
                statement.setLong(1, sesion.getUsuarioId());
                statement.setString(2, motivo != null && !motivo.trim().isEmpty() ? motivo.trim() : "Cancelada por el usuario");
                statement.setLong(3, reserva.getId());
                
                int filasAfectadas = statement.executeUpdate();
                
                if (filasAfectadas > 0) {
                    mostrarAlerta("Éxito", "Reserva cancelada", 
                                 "La reserva ha sido cancelada exitosamente.", 
                                 Alert.AlertType.INFORMATION);
                    
                    // Recargar reservas
                    cargarReservas();
                } else {
                    mostrarAlerta("Error", "Error al cancelar", 
                                 "No se pudo cancelar la reserva. Por favor, intente más tarde.", 
                                 Alert.AlertType.ERROR);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al cancelar reserva: " + e.getMessage());
            e.printStackTrace();
            mostrarAlerta("Error", "Error al cancelar", 
                         "No se pudo cancelar la reserva. Por favor, intente más tarde.", 
                         Alert.AlertType.ERROR);
        }
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

