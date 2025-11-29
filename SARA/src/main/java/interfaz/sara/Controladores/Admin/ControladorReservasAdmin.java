package interfaz.sara.Controladores.Admin;

import interfaz.sara.ConexionBD.ConexionBD;
import interfaz.sara.Modelo.Reserva;
import interfaz.sara.Utilidades.GestorNavegacion;
import interfaz.sara.Utilidades.SesionUsuario;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controlador para la vista de gestión de reservas del administrador (VistaReservasAdmin.fxml)
 * Gestiona la visualización, búsqueda y filtrado de reservas
 */
public class ControladorReservasAdmin {

    // ========== Componentes FXML ==========
    
    @FXML
    private VBox sidebarInclude;
    
    @FXML
    private ComboBox<String> comboSalas;
    
    @FXML
    private ComboBox<String> comboUsuarios;
    
    @FXML
    private DatePicker datePickerFecha;
    
    @FXML
    private VBox contenedorReservas;

    // ========== Variables de estado ==========
    
    /** Lista completa de reservas */
    private List<Reserva> listaReservasCompleta;
    
    /** Lista observable de reservas filtradas */
    private ObservableList<Reserva> listaReservasFiltrada;
    
    /** Mapa de salas (nombre -> id) */
    private Map<String, Long> mapaSalas;
    
    /** Mapa de usuarios (nombre -> id) */
    private Map<String, Long> mapaUsuarios;
    
    /** Mapa de nombres de usuarios (userId -> nombreUsuario) */
    private Map<Long, String> mapaNombresUsuarios;
    
    /** Formateador de fecha y hora */
    private DateTimeFormatter formateadorFecha = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

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
        
        // Inicializar listas y mapas
        listaReservasCompleta = new ArrayList<>();
        listaReservasFiltrada = FXCollections.observableArrayList();
        mapaSalas = new HashMap<>();
        mapaUsuarios = new HashMap<>();
        mapaNombresUsuarios = new HashMap<>();
        
        // Configurar filtros
        configurarFiltros();
        
        // Cargar datos desde la base de datos
        cargarReservas();
        
        // Configurar el sidebar
        Platform.runLater(() -> {
            configurarSidebar();
        });
    }
    
    /**
     * Configura los filtros de búsqueda
     */
    private void configurarFiltros() {
        // Configurar listeners para los filtros
        comboSalas.setOnAction(e -> aplicarFiltros());
        comboUsuarios.setOnAction(e -> aplicarFiltros());
        datePickerFecha.setOnAction(e -> aplicarFiltros());
        
        // Cargar opciones de salas y usuarios
        cargarOpcionesFiltros();
    }
    
    /**
     * Carga las opciones para los filtros (salas y usuarios)
     */
    private void cargarOpcionesFiltros() {
        // Cargar salas
        ObservableList<String> opcionesSalas = FXCollections.observableArrayList();
        opcionesSalas.add("Todas las salas");
        
        ConexionBD conexionBD = ConexionBD.obtenerInstancia();
        String sqlSalas = "SELECT id, name FROM rooms WHERE deleted_at IS NULL ORDER BY name ASC";
        
        try (Connection conexion = conexionBD.obtenerConexion();
             PreparedStatement statement = conexion.prepareStatement(sqlSalas);
             ResultSet resultado = statement.executeQuery()) {
            
            while (resultado.next()) {
                String nombreSala = resultado.getString("name");
                Long idSala = resultado.getLong("id");
                opcionesSalas.add(nombreSala);
                mapaSalas.put(nombreSala, idSala);
            }
        } catch (SQLException e) {
            System.err.println("Error al cargar salas para filtro: " + e.getMessage());
        }
        
        comboSalas.setItems(opcionesSalas);
        comboSalas.getSelectionModel().selectFirst();
        
        // Cargar usuarios
        ObservableList<String> opcionesUsuarios = FXCollections.observableArrayList();
        opcionesUsuarios.add("Todos los usuarios");
        
        String sqlUsuarios = "SELECT id, username FROM users ORDER BY username ASC";
        
        try (Connection conexion = conexionBD.obtenerConexion();
             PreparedStatement statement = conexion.prepareStatement(sqlUsuarios);
             ResultSet resultado = statement.executeQuery()) {
            
            while (resultado.next()) {
                String nombreUsuario = resultado.getString("username");
                Long idUsuario = resultado.getLong("id");
                opcionesUsuarios.add(nombreUsuario);
                mapaUsuarios.put(nombreUsuario, idUsuario);
            }
        } catch (SQLException e) {
            System.err.println("Error al cargar usuarios para filtro: " + e.getMessage());
        }
        
        comboUsuarios.setItems(opcionesUsuarios);
        comboUsuarios.getSelectionModel().selectFirst();
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
                    // Resetear todos
                    resetearBotonesSidebar(btnUsuarios, btnSalas, btnReportes, btnPerfil, btnNotificaciones);
                    
                    // Activar reservas
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
     * Carga todas las reservas desde la base de datos
     */
    private void cargarReservas() {
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
                    "ORDER BY r.start_at DESC";
        
        try {
            Connection conexion = conexionBD.obtenerConexion();
            try (PreparedStatement statement = conexion.prepareStatement(sql)) {
                try (ResultSet resultado = statement.executeQuery()) {
                    listaReservasCompleta.clear();
                    
                    while (resultado.next()) {
                        Reserva reserva = new Reserva();
                        reserva.setId(resultado.getLong("id"));
                        reserva.setRoomId(resultado.getLong("room_id"));
                        reserva.setNombreSala(resultado.getString("nombre_sala"));
                        Long userId = resultado.getLong("user_id");
                        reserva.setUserId(userId);
                        
                        // Almacenar nombre de usuario en el mapa
                        String nombreUsuario = resultado.getString("nombre_usuario");
                        if (nombreUsuario != null) {
                            mapaNombresUsuarios.put(userId, nombreUsuario);
                        }
                        
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
                        
                        listaReservasCompleta.add(reserva);
                    }
                    
                    // Aplicar filtros y mostrar reservas
                    aplicarFiltros();
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al cargar reservas: " + e.getMessage());
            e.printStackTrace();
            mostrarAlerta("Error", "Error al cargar reservas", 
                         "No se pudieron cargar las reservas. Por favor, intente más tarde.", 
                         Alert.AlertType.ERROR);
        }
    }
    
    /**
     * Aplica los filtros de búsqueda y actualiza la visualización
     */
    @FXML
    private void aplicarFiltros() {
        String salaSeleccionada = comboSalas.getSelectionModel().getSelectedItem();
        String usuarioSeleccionado = comboUsuarios.getSelectionModel().getSelectedItem();
        LocalDate fechaSeleccionada = datePickerFecha.getValue();
        
        listaReservasFiltrada.clear();
        
        for (Reserva reserva : listaReservasCompleta) {
            // Filtro por sala
            boolean coincideSala = salaSeleccionada == null || 
                                  salaSeleccionada.equals("Todas las salas") ||
                                  reserva.getNombreSala().equals(salaSeleccionada);
            
            // Filtro por usuario
            boolean coincideUsuario = usuarioSeleccionado == null || 
                                     usuarioSeleccionado.equals("Todos los usuarios") ||
                                     (mapaUsuarios.containsKey(usuarioSeleccionado) && 
                                      reserva.getUserId().equals(mapaUsuarios.get(usuarioSeleccionado)));
            
            // Filtro por fecha
            boolean coincideFecha = fechaSeleccionada == null;
            if (!coincideFecha && reserva.getFechaInicio() != null) {
                LocalDate fechaReserva = reserva.getFechaInicio().toLocalDate();
                coincideFecha = fechaReserva.equals(fechaSeleccionada);
            }
            
            if (coincideSala && coincideUsuario && coincideFecha) {
                listaReservasFiltrada.add(reserva);
            }
        }
        
        // Actualizar visualización
        actualizarVisualizacionReservas();
    }
    
    /**
     * Actualiza la visualización de las tarjetas de reservas
     */
    private void actualizarVisualizacionReservas() {
        contenedorReservas.getChildren().clear();
        
        if (listaReservasFiltrada.isEmpty()) {
            Label mensajeVacio = new Label("No se encontraron reservas con los filtros seleccionados.");
            mensajeVacio.getStyleClass().add("mensaje-vacio");
            contenedorReservas.getChildren().add(mensajeVacio);
            return;
        }
        
        // Usar FlowPane para organizar las tarjetas en grid
        FlowPane flowPane = new FlowPane();
        flowPane.setHgap(16);
        flowPane.setVgap(16);
        flowPane.setPrefWidth(Region.USE_COMPUTED_SIZE);
        
        for (Reserva reserva : listaReservasFiltrada) {
            VBox tarjetaReserva = crearTarjetaReserva(reserva);
            flowPane.getChildren().add(tarjetaReserva);
        }
        
        contenedorReservas.getChildren().add(flowPane);
    }
    
    /**
     * Crea una tarjeta visual para una reserva
     * 
     * @param reserva La reserva a mostrar
     * @return VBox con la tarjeta de la reserva
     */
    private VBox crearTarjetaReserva(Reserva reserva) {
        VBox tarjeta = new VBox();
        tarjeta.getStyleClass().add("tarjeta-reserva-admin");
        tarjeta.setSpacing(0);
        tarjeta.setPadding(new Insets(0));
        tarjeta.setPrefWidth(320);
        tarjeta.setMaxWidth(320);
        tarjeta.setMinWidth(320);
        tarjeta.setPrefHeight(280);
        tarjeta.setMaxHeight(280);
        tarjeta.setMinHeight(280);
        tarjeta.setCursor(javafx.scene.Cursor.HAND);
        
        // Hacer toda la tarjeta clicable para ver/editar detalles
        tarjeta.setOnMouseClicked(e -> manejarVerDetalleReserva(reserva));
        
        // Contenedor de información
        VBox infoContainer = new VBox(12);
        infoContainer.setPadding(new Insets(20));
        infoContainer.setSpacing(12);
        infoContainer.setPrefWidth(320);
        infoContainer.setStyle("-fx-background-color: #ffffff;");
        
        // Encabezado con nombre de sala y estado
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        
        Label nombreSala = new Label(reserva.getNombreSala() != null ? reserva.getNombreSala() : "Sala desconocida");
        nombreSala.getStyleClass().add("tarjeta-reserva-nombre");
        
        Label estado = new Label(reserva.getEstadoReserva() != null ? reserva.getEstadoReserva() : "Estado desconocido");
        estado.getStyleClass().add("tarjeta-reserva-estado-" + obtenerClaseEstado(reserva.getCodigoEstado()));
        
        HBox spacer = new HBox();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
        
        header.getChildren().addAll(nombreSala, spacer, estado);
        
        // Separador visual
        javafx.scene.shape.Line separador = new javafx.scene.shape.Line();
        separador.setStartX(0);
        separador.setEndX(280);
        separador.setStroke(javafx.scene.paint.Color.web("#e2e8f0"));
        separador.setStrokeWidth(1);
        
        // Información de usuario que solicita
        VBox usuarioBox = new VBox(6);
        String nombreUsuario = mapaNombresUsuarios.get(reserva.getUserId());
        if (nombreUsuario != null && !nombreUsuario.isEmpty()) {
            Label usuarioLabel = new Label("Solicitado por: " + nombreUsuario);
            usuarioLabel.getStyleClass().add("tarjeta-reserva-usuario");
            usuarioBox.getChildren().add(usuarioLabel);
        }
        
        // Información de fechas
        VBox fechasBox = new VBox(6);
        if (reserva.getFechaInicio() != null) {
            Label fechaInicio = new Label("Inicio: " + reserva.getFechaInicio().format(formateadorFecha));
            fechaInicio.getStyleClass().add("tarjeta-reserva-fecha");
            fechasBox.getChildren().add(fechaInicio);
        }
        if (reserva.getFechaFin() != null) {
            Label fechaFin = new Label("Fin: " + reserva.getFechaFin().format(formateadorFecha));
            fechaFin.getStyleClass().add("tarjeta-reserva-fecha");
            fechasBox.getChildren().add(fechaFin);
        }
        
        // Información adicional
        VBox infoBox = new VBox(6);
        if (reserva.getCantidadPersonas() != null) {
            Label personas = new Label("Personas: " + reserva.getCantidadPersonas());
            personas.getStyleClass().add("tarjeta-reserva-info");
            infoBox.getChildren().add(personas);
        }
        if (reserva.getMotivo() != null && !reserva.getMotivo().isEmpty()) {
            Label motivo = new Label("Motivo: " + reserva.getMotivo());
            motivo.getStyleClass().add("tarjeta-reserva-info");
            motivo.setWrapText(true);
            infoBox.getChildren().add(motivo);
        }
        
        // Agregar todos los elementos a la tarjeta
        infoContainer.getChildren().addAll(header, separador, usuarioBox, fechasBox, infoBox);
        
        // Si está cancelada, mostrar motivo de cancelación
        if (reserva.estaCancelada() && reserva.getMotivoCancelacion() != null) {
            Label motivoCancelacion = new Label("Cancelación: " + reserva.getMotivoCancelacion());
            motivoCancelacion.getStyleClass().add("tarjeta-reserva-cancelacion");
            motivoCancelacion.setWrapText(true);
            infoContainer.getChildren().add(motivoCancelacion);
        }
        
        tarjeta.getChildren().add(infoContainer);
        
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
    
    // ========== Métodos de manejo de eventos ==========
    
    /**
     * Maneja la acción de ver el detalle de una reserva
     * Navega a la vista de detalle/autorización de la reserva
     * 
     * @param reserva La reserva a ver
     */
    private void manejarVerDetalleReserva(Reserva reserva) {
        GestorNavegacion gestorNavegacion = GestorNavegacion.obtenerInstancia();
        gestorNavegacion.navegarAVistaDetalleReservaAdmin(reserva.getId());
    }
    
    // ========== Métodos auxiliares ==========
    
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

