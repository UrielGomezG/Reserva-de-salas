package interfaz.sara.Controladores.Admin;

import interfaz.sara.ConexionBD.ConexionBD;
import interfaz.sara.Modelo.Incidente;
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
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controlador para la vista de gestión de reportes/incidentes del administrador (VistaReportesAdmin.fxml)
 * Gestiona la visualización y filtrado de incidentes reportados
 */
public class ControladorReportesAdmin {

    // ========== Componentes FXML ==========
    
    @FXML
    private VBox sidebarInclude;
    
    @FXML
    private ComboBox<String> comboSalas;
    
    @FXML
    private VBox contenedorIncidentes;

    // ========== Variables de estado ==========
    
    /** Lista completa de incidentes */
    private List<Incidente> listaIncidentesCompleta;
    
    /** Lista observable de incidentes filtrados */
    private ObservableList<Incidente> listaIncidentesFiltrada;
    
    /** Mapa de salas (nombre -> id) */
    private Map<String, Long> mapaSalas;
    
    /** Mapa de nombres de usuarios (userId -> nombreUsuario) */
    private Map<Long, String> mapaNombresUsuarios;
    
    /** Formateador de fecha (solo fecha, sin hora) */
    private DateTimeFormatter formateadorFecha = DateTimeFormatter.ofPattern("dd/MM/yyyy");

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
        
        // Inicializar listas y mapas
        listaIncidentesCompleta = new ArrayList<>();
        listaIncidentesFiltrada = FXCollections.observableArrayList();
        mapaSalas = new HashMap<>();
        mapaNombresUsuarios = new HashMap<>();
        
        // Configurar filtro
        configurarFiltro();
        
        // Cargar datos desde la base de datos
        cargarIncidentes();
        
        // Configurar el sidebar
        Platform.runLater(() -> {
            configurarSidebar();
        });
    }
    
    /**
     * Configura el filtro de búsqueda
     */
    private void configurarFiltro() {
        // Configurar listener para el filtro
        comboSalas.setOnAction(e -> aplicarFiltros());
        
        // Cargar opciones de salas
        cargarOpcionesSalas();
    }
    
    /**
     * Carga las opciones para el filtro de salas
     */
    private void cargarOpcionesSalas() {
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
    }
    
    /**
     * Configura el sidebar para resaltar el botón de reportes como activo
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
                
                if (btnReportes != null) {
                    // Resetear todos
                    resetearBotonesSidebar(btnUsuarios, btnSalas, btnReservas, btnPerfil, btnNotificaciones);
                    
                    // Activar reportes
                    btnReportes.getStyleClass().remove("sidebar-button");
                    if (!btnReportes.getStyleClass().contains("sidebar-button-active")) {
                        btnReportes.getStyleClass().add("sidebar-button-active");
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
     * Carga todos los incidentes desde la base de datos
     */
    private void cargarIncidentes() {
        ConexionBD conexionBD = ConexionBD.obtenerInstancia();
        
        String sql = "SELECT i.id, i.room_id, ro.name as nombre_sala, i.reported_by_user_id, " +
                    "u.username as nombre_usuario, " +
                    "it.name as tipo_incidente, i.title, i.description, " +
                    "i.incident_at, i.created_at " +
                    "FROM incidents i " +
                    "INNER JOIN rooms ro ON i.room_id = ro.id " +
                    "INNER JOIN users u ON i.reported_by_user_id = u.id " +
                    "INNER JOIN incident_types it ON i.incident_type_id = it.id " +
                    "ORDER BY i.created_at DESC";
        
        try {
            Connection conexion = conexionBD.obtenerConexion();
            try (PreparedStatement statement = conexion.prepareStatement(sql)) {
                try (ResultSet resultado = statement.executeQuery()) {
                    listaIncidentesCompleta.clear();
                    
                    while (resultado.next()) {
                        Incidente incidente = new Incidente();
                        incidente.setId(resultado.getLong("id"));
                        incidente.setRoomId(resultado.getLong("room_id"));
                        incidente.setNombreSala(resultado.getString("nombre_sala"));
                        Long userId = resultado.getLong("reported_by_user_id");
                        incidente.setUserId(userId);
                        
                        // Almacenar nombre de usuario en el mapa
                        String nombreUsuario = resultado.getString("nombre_usuario");
                        if (nombreUsuario != null) {
                            mapaNombresUsuarios.put(userId, nombreUsuario);
                        }
                        
                        incidente.setTipoIncidente(resultado.getString("tipo_incidente"));
                        incidente.setDescripcion(resultado.getString("description"));
                        
                        // Convertir Timestamp a LocalDateTime
                        if (resultado.getTimestamp("incident_at") != null) {
                            incidente.setFechaReporte(resultado.getTimestamp("incident_at").toLocalDateTime());
                        }
                        if (resultado.getTimestamp("created_at") != null) {
                            incidente.setFechaCreacion(resultado.getTimestamp("created_at").toLocalDateTime());
                        }
                        
                        // La tabla incidents no tiene columna status, establecer un valor por defecto
                        incidente.setEstado("Pendiente");
                        
                        listaIncidentesCompleta.add(incidente);
                    }
                    
                    // Aplicar filtros y mostrar incidentes
                    aplicarFiltros();
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al cargar incidentes: " + e.getMessage());
            e.printStackTrace();
            mostrarAlerta("Error", "Error al cargar incidentes", 
                         "No se pudieron cargar los incidentes. Por favor, intente más tarde.", 
                         Alert.AlertType.ERROR);
        }
    }
    
    /**
     * Aplica los filtros de búsqueda y actualiza la visualización
     */
    @FXML
    private void aplicarFiltros() {
        String salaSeleccionada = comboSalas.getSelectionModel().getSelectedItem();
        
        listaIncidentesFiltrada.clear();
        
        for (Incidente incidente : listaIncidentesCompleta) {
            // Filtro por sala
            boolean coincideSala = salaSeleccionada == null || 
                                  salaSeleccionada.equals("Todas las salas") ||
                                  incidente.getNombreSala().equals(salaSeleccionada);
            
            if (coincideSala) {
                listaIncidentesFiltrada.add(incidente);
            }
        }
        
        // Actualizar visualización
        actualizarVisualizacionIncidentes();
    }
    
    /**
     * Actualiza la visualización de las tarjetas de incidentes
     */
    private void actualizarVisualizacionIncidentes() {
        contenedorIncidentes.getChildren().clear();
        
        if (listaIncidentesFiltrada.isEmpty()) {
            Label mensajeVacio = new Label("No se encontraron incidentes con los filtros seleccionados.");
            mensajeVacio.getStyleClass().add("mensaje-vacio");
            contenedorIncidentes.getChildren().add(mensajeVacio);
            return;
        }
        
        // Usar FlowPane para organizar las tarjetas en grid
        FlowPane flowPane = new FlowPane();
        flowPane.setHgap(16);
        flowPane.setVgap(16);
        flowPane.setPrefWidth(Region.USE_COMPUTED_SIZE);
        
        for (Incidente incidente : listaIncidentesFiltrada) {
            VBox tarjetaIncidente = crearTarjetaIncidente(incidente);
            flowPane.getChildren().add(tarjetaIncidente);
        }
        
        contenedorIncidentes.getChildren().add(flowPane);
    }
    
    /**
     * Crea una tarjeta visual para un incidente
     * 
     * @param incidente El incidente a mostrar
     * @return VBox con la tarjeta del incidente
     */
    private VBox crearTarjetaIncidente(Incidente incidente) {
        VBox tarjeta = new VBox();
        tarjeta.getStyleClass().add("tarjeta-incidente-admin");
        tarjeta.setSpacing(0);
        tarjeta.setPadding(new Insets(0));
        tarjeta.setPrefWidth(320);
        tarjeta.setMaxWidth(400);
        tarjeta.setMinWidth(280);
        tarjeta.setPrefHeight(280);
        tarjeta.setMaxHeight(280);
        tarjeta.setMinHeight(280);
        
        // Contenedor de información
        VBox infoContainer = new VBox(12);
        infoContainer.setPadding(new Insets(20));
        infoContainer.setSpacing(12);
        infoContainer.setPrefWidth(Region.USE_COMPUTED_SIZE);
        infoContainer.setMaxWidth(Double.MAX_VALUE);
        infoContainer.setStyle("-fx-background-color: #ffffff;");
        
        // Encabezado con nombre de sala y tipo de incidente
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        
        Label nombreSala = new Label(incidente.getNombreSala() != null ? incidente.getNombreSala() : "Sala desconocida");
        nombreSala.getStyleClass().add("tarjeta-incidente-nombre");
        
        Label tipoIncidente = new Label(incidente.getTipoIncidente() != null ? incidente.getTipoIncidente() : "Tipo desconocido");
        tipoIncidente.getStyleClass().add("tarjeta-incidente-tipo");
        
        HBox spacer = new HBox();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
        
        header.getChildren().addAll(nombreSala, spacer, tipoIncidente);
        
        // Separador visual
        javafx.scene.shape.Line separador = new javafx.scene.shape.Line();
        separador.setStartX(0);
        separador.setEndX(280);
        separador.setStroke(javafx.scene.paint.Color.web("#e2e8f0"));
        separador.setStrokeWidth(1);
        
        // Información del usuario que reportó
        VBox usuarioBox = new VBox(6);
        String nombreUsuario = mapaNombresUsuarios.get(incidente.getUserId());
        if (nombreUsuario != null && !nombreUsuario.isEmpty()) {
            Label usuarioLabel = new Label("Reportado por: " + nombreUsuario);
            usuarioLabel.getStyleClass().add("tarjeta-incidente-usuario");
            usuarioBox.getChildren().add(usuarioLabel);
        }
        
        // Información de fecha
        VBox fechaBox = new VBox(6);
        if (incidente.getFechaReporte() != null) {
            Label fechaReporte = new Label("Fecha: " + incidente.getFechaReporte().format(formateadorFecha));
            fechaReporte.getStyleClass().add("tarjeta-incidente-fecha");
            fechaBox.getChildren().add(fechaReporte);
        }
        
        // Descripción
        VBox descripcionBox = new VBox(6);
        if (incidente.getDescripcion() != null && !incidente.getDescripcion().isEmpty()) {
            Label descripcion = new Label("Descripción: " + incidente.getDescripcion());
            descripcion.getStyleClass().add("tarjeta-incidente-descripcion");
            descripcion.setWrapText(true);
            descripcionBox.getChildren().add(descripcion);
        }
        
        // Estado (no se muestra si no existe en la BD, pero mantenemos la estructura por si se agrega en el futuro)
        Label estado = new Label("Estado: " + (incidente.getEstado() != null ? incidente.getEstado() : "Pendiente"));
        estado.getStyleClass().add("tarjeta-incidente-estado");
        // Ocultar estado por ahora ya que no existe en la BD
        estado.setVisible(false);
        estado.setManaged(false);
        
        // Agregar todos los elementos a la tarjeta
        infoContainer.getChildren().addAll(header, separador, usuarioBox, fechaBox, descripcionBox, estado);
        
        tarjeta.getChildren().add(infoContainer);
        
        return tarjeta;
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

