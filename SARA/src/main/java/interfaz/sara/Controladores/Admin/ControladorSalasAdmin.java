package interfaz.sara.Controladores.Admin;

import interfaz.sara.ConexionBD.ConexionBD;
import interfaz.sara.Modelo.Sala;
import interfaz.sara.Utilidades.GestorNavegacion;
import interfaz.sara.Utilidades.SesionUsuario;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controlador para la vista de gestión de salas del administrador (VistaSalasAdmin.fxml)
 * Gestiona la visualización y administración de salas
 */
public class ControladorSalasAdmin {

    // ========== Componentes FXML ==========
    
    @FXML
    private VBox sidebarInclude;
    
    @FXML
    private FlowPane contenedorSalas;

    // ========== Variables de estado ==========
    
    /** Lista de salas cargadas desde la base de datos */
    private List<Sala> listaSalas;
    
    /** Mapa para guardar las rutas de las imágenes de las salas (salaId -> imagePath) */
    private Map<Long, String> mapaRutasImagenes = new HashMap<>();

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
        
        // Inicializar lista y mapa
        listaSalas = new ArrayList<>();
        mapaRutasImagenes = new HashMap<>();
        
        // Cargar salas desde la base de datos
        cargarSalas();
        
        // Configurar el sidebar
        Platform.runLater(() -> {
            configurarSidebar();
        });
    }
    
    /**
     * Configura el sidebar para resaltar el botón de salas como activo
     */
    private void configurarSidebar() {
        try {
            if (sidebarInclude != null) {
                // Obtener botones directamente y aplicar estilos
                Button btnUsuarios = (Button) sidebarInclude.lookup("#botonUsuarios");
                Button btnSalas = (Button) sidebarInclude.lookup("#botonSalas");
                Button btnReservas = (Button) sidebarInclude.lookup("#botonReservas");
                Button btnReportes = (Button) sidebarInclude.lookup("#botonReportes");
                Button btnPerfil = (Button) sidebarInclude.lookup("#botonPerfil");
                Button btnNotificaciones = (Button) sidebarInclude.lookup("#botonNotificaciones");
                
                if (btnSalas != null) {
                    // Resetear todos
                    resetearBotonesSidebar(btnUsuarios, btnReservas, btnReportes, btnPerfil, btnNotificaciones);
                    
                    // Activar salas
                    btnSalas.getStyleClass().remove("sidebar-button");
                    if (!btnSalas.getStyleClass().contains("sidebar-button-active")) {
                        btnSalas.getStyleClass().add("sidebar-button-active");
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
     * Carga todas las salas desde la base de datos
     */
    private void cargarSalas() {
        ConexionBD conexionBD = ConexionBD.obtenerInstancia();
        
        String sql = "SELECT r.id, r.room_code, r.name, r.capacity, r.room_type, " +
                    "COALESCE(l.name, 'Sin ubicación') as location, r.is_enabled, r.image_path " +
                    "FROM rooms r " +
                    "LEFT JOIN locations l ON r.location_id = l.id " +
                    "WHERE r.deleted_at IS NULL " +
                    "ORDER BY r.name ASC";
        
        try {
            Connection conexion = conexionBD.obtenerConexion();
            try (PreparedStatement statement = conexion.prepareStatement(sql)) {
                try (ResultSet resultado = statement.executeQuery()) {
                    listaSalas.clear();
                    
                    while (resultado.next()) {
                        Sala sala = new Sala();
                        sala.setId(resultado.getLong("id"));
                        sala.setCodigoSala(resultado.getInt("room_code"));
                        sala.setNombre(resultado.getString("name"));
                        sala.setCapacidad(resultado.getInt("capacity"));
                        sala.setTipoSala(resultado.getString("room_type"));
                        sala.setUbicacion(resultado.getString("location"));
                        sala.setHabilitada(resultado.getBoolean("is_enabled"));
                        
                        // Guardar la ruta de la imagen en un mapa para usarla al crear la tarjeta
                        String imagePath = resultado.getString("image_path");
                        mapaRutasImagenes.put(sala.getId(), imagePath);
                        
                        listaSalas.add(sala);
                    }
                    
                    // Actualizar visualización
                    actualizarVisualizacionSalas();
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al cargar salas: " + e.getMessage());
            e.printStackTrace();
            mostrarAlerta("Error", "Error al cargar salas", 
                         "No se pudieron cargar las salas. Por favor, intente más tarde.", 
                         Alert.AlertType.ERROR);
        }
    }
    
    /**
     * Actualiza la visualización de las tarjetas de salas
     */
    private void actualizarVisualizacionSalas() {
        contenedorSalas.getChildren().clear();
        
        for (Sala sala : listaSalas) {
            VBox tarjetaSala = crearTarjetaSala(sala);
            contenedorSalas.getChildren().add(tarjetaSala);
        }
    }
    
    /**
     * Crea una tarjeta visual para una sala con mejor presentación
     * 
     * @param sala Información de la sala
     * @return VBox con la tarjeta de la sala
     */
    private VBox crearTarjetaSala(Sala sala) {
        VBox tarjeta = new VBox();
        tarjeta.getStyleClass().add("tarjeta-sala-admin");
        tarjeta.setSpacing(0);
        tarjeta.setPadding(new Insets(0));
        tarjeta.setPrefWidth(320);
        tarjeta.setMaxWidth(320);
        tarjeta.setMinWidth(320);
        tarjeta.setPrefHeight(420);
        tarjeta.setMaxHeight(420);
        tarjeta.setMinHeight(420);
        tarjeta.setCursor(javafx.scene.Cursor.HAND);
        
        // Hacer toda la tarjeta clicable para ver/editar detalles
        tarjeta.setOnMouseClicked(e -> manejarVerDetalleSala(sala));
        
        // Contenedor para la imagen con overlay
        StackPane imagenContainer = new StackPane();
        imagenContainer.setPrefHeight(200);
        imagenContainer.setMaxHeight(200);
        imagenContainer.setMinHeight(200);
        imagenContainer.setPrefWidth(320);
        imagenContainer.getStyleClass().add("imagen-sala-container");
        
        ImageView imageView = new ImageView();
        imageView.setFitWidth(320);
        imageView.setFitHeight(200);
        imageView.setPreserveRatio(false); // Para que llene el espacio completamente
        imageView.setSmooth(true);
        imageView.setCache(true);
        imageView.getStyleClass().add("imagen-sala-tarjeta");
        
        // Cargar imagen de la sala
        String imagePath = mapaRutasImagenes.get(sala.getId());
        if (imagePath != null && !imagePath.isEmpty()) {
            try {
                File file = new File(imagePath);
                if (file.exists()) {
                    Image image = new Image(new FileInputStream(file));
                    imageView.setImage(image);
                } else {
                    cargarImagenPorDefecto(imageView);
                }
            } catch (FileNotFoundException e) {
                System.err.println("Archivo de imagen de sala no encontrado: " + imagePath);
                cargarImagenPorDefecto(imageView);
            } catch (Exception e) {
                System.err.println("Error al cargar imagen de sala: " + e.getMessage());
                cargarImagenPorDefecto(imageView);
            }
        } else {
            cargarImagenPorDefecto(imageView);
        }
        
        // Aplicar clip redondeado solo en la parte superior
        javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle(320, 200);
        clip.setArcWidth(12);
        clip.setArcHeight(12);
        imageView.setClip(clip);
        
        // Badge de estado en la esquina superior derecha de la imagen
        HBox estadoBadgeContainer = new HBox();
        estadoBadgeContainer.setAlignment(Pos.TOP_RIGHT);
        estadoBadgeContainer.setPadding(new Insets(12, 12, 0, 0));
        
        Label estadoBadge = new Label(sala.getHabilitada() ? "✓ Activa" : "✗ Inactiva");
        estadoBadge.getStyleClass().add(sala.getHabilitada() ? "badge-estado-activa" : "badge-estado-inactiva");
        estadoBadgeContainer.getChildren().add(estadoBadge);
        
        StackPane.setAlignment(estadoBadgeContainer, Pos.TOP_RIGHT);
        
        imagenContainer.getChildren().addAll(imageView, estadoBadgeContainer);
        
        // Contenedor para la información de la sala
        VBox infoContainer = new VBox(12);
        infoContainer.setPadding(new Insets(20));
        infoContainer.setSpacing(12);
        infoContainer.setPrefWidth(320);
        infoContainer.setStyle("-fx-background-color: #ffffff;");
        
        // Encabezado con nombre de la sala
        Label labelNombre = new Label(sala.getNombre());
        labelNombre.getStyleClass().add("tarjeta-sala-nombre");
        labelNombre.setWrapText(true);
        labelNombre.setMaxWidth(Double.MAX_VALUE);
        
        // Separador visual
        javafx.scene.shape.Line separador = new javafx.scene.shape.Line();
        separador.setStartX(0);
        separador.setEndX(280);
        separador.setStroke(javafx.scene.paint.Color.web("#e2e8f0"));
        separador.setStrokeWidth(1);
        
        // Grid de información (2 columnas)
        GridPane infoGrid = new GridPane();
        infoGrid.setHgap(16);
        infoGrid.setVgap(10);
        infoGrid.setPadding(new Insets(8, 0, 0, 0));
        
        // Fila 1: Código y Tipo
        Label labelEtiquetaCodigo = new Label("Código:");
        labelEtiquetaCodigo.getStyleClass().add("tarjeta-etiqueta-info");
        
        Label labelCodigo = new Label(String.valueOf(sala.getCodigoSala()));
        labelCodigo.getStyleClass().add("tarjeta-valor-info");
        
        Label labelEtiquetaTipo = new Label("Tipo:");
        labelEtiquetaTipo.getStyleClass().add("tarjeta-etiqueta-info");
        
        Label labelTipo = new Label(formatearTipoSala(sala.getTipoSala()));
        labelTipo.getStyleClass().add("tarjeta-valor-info");
        labelTipo.setWrapText(true);
        
        infoGrid.add(labelEtiquetaCodigo, 0, 0);
        infoGrid.add(labelCodigo, 1, 0);
        infoGrid.add(labelEtiquetaTipo, 0, 1);
        infoGrid.add(labelTipo, 1, 1);
        
        // Fila 2: Ubicación y Capacidad
        Label labelEtiquetaUbicacion = new Label("Ubicación:");
        labelEtiquetaUbicacion.getStyleClass().add("tarjeta-etiqueta-info");
        
        Label labelUbicacion = new Label(sala.getUbicacion());
        labelUbicacion.getStyleClass().add("tarjeta-valor-info");
        labelUbicacion.setWrapText(true);
        
        Label labelEtiquetaCapacidad = new Label("Capacidad:");
        labelEtiquetaCapacidad.getStyleClass().add("tarjeta-etiqueta-info");
        
        Label labelCapacidad = new Label(sala.getCapacidad() + " personas");
        labelCapacidad.getStyleClass().add("tarjeta-valor-info");
        
        infoGrid.add(labelEtiquetaUbicacion, 0, 2);
        infoGrid.add(labelUbicacion, 1, 2);
        infoGrid.add(labelEtiquetaCapacidad, 0, 3);
        infoGrid.add(labelCapacidad, 1, 3);
        
        // Agregar información al contenedor
        infoContainer.getChildren().addAll(
            labelNombre,
            separador,
            infoGrid
        );
        
        // Agregar componentes a la tarjeta
        tarjeta.getChildren().addAll(
            imagenContainer,
            infoContainer
        );
        
        return tarjeta;
    }
    
    /**
     * Formatea el tipo de sala para mostrarlo de forma legible
     * 
     * @param tipoSala Tipo de sala desde la BD
     * @return Tipo de sala formateado
     */
    private String formatearTipoSala(String tipoSala) {
        if (tipoSala == null || tipoSala.trim().isEmpty()) {
            return "Sin tipo";
        }
        
        String tipoUpper = tipoSala.toUpperCase().trim();
        
        switch (tipoUpper) {
            case "BOARDROOM":
            case "SALA DE JUNTAS":
                return "Sala de Juntas";
            case "LABORATORY":
            case "LABORATORIO":
                return "Laboratorio";
            case "AUDITORIUM":
            case "AUDITORIO":
                return "Auditorio";
            default:
                return tipoSala;
        }
    }
    
    /**
     * Carga una imagen por defecto en el ImageView si no hay imagen de sala
     * @param imageView El ImageView donde cargar la imagen
     */
    private void cargarImagenPorDefecto(ImageView imageView) {
        try {
            InputStream imagenStream = getClass().getResourceAsStream(
                "/interfaz/sara/imagenes/room_placeholder.png"
            );
            if (imagenStream != null) {
                Image imagen = new Image(imagenStream);
                imageView.setImage(imagen);
            } else {
                System.err.println("No se encontró la imagen de placeholder de sala.");
                imageView.setImage(null);
            }
        } catch (Exception e) {
            System.err.println("Error al cargar imagen de placeholder de sala: " + e.getMessage());
            imageView.setImage(null);
        }
    }

    // ========== Métodos de manejo de eventos ==========
    
    /**
     * Maneja el clic en el botón "Registrar Nueva Sala"
     * Navega a la vista de registro de nueva sala
     */
    @FXML
    private void manejarRegistrarSala() {
        GestorNavegacion gestorNavegacion = GestorNavegacion.obtenerInstancia();
        gestorNavegacion.navegarAVistaRegistrarSalaAdmin();
    }
    
    /**
     * Maneja el clic en una tarjeta de sala para ver sus detalles
     * 
     * @param sala Sala seleccionada
     */
    private void manejarVerDetalleSala(Sala sala) {
        GestorNavegacion gestorNavegacion = GestorNavegacion.obtenerInstancia();
        gestorNavegacion.navegarAVistaDetalleSalaAdmin(sala.getId());
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

