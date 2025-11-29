package interfaz.sara.Controladores.Admin;

import interfaz.sara.ConexionBD.ConexionBD;
import interfaz.sara.Utilidades.GestorNavegacion;
import interfaz.sara.Utilidades.SesionUsuario;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.geometry.Pos;
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
 * Controlador para la vista de gestión de usuarios del administrador (VistaUsuariosAdmin.fxml)
 * Gestiona la visualización, búsqueda y filtrado de usuarios
 */
public class ControladorUsuariosAdmin {

    // ========== Componentes FXML ==========
    
    @FXML
    private VBox sidebarInclude;
    
    @FXML
    private TextField campoBusqueda;
    
    @FXML
    private ComboBox<String> filtroRol;
    
    @FXML
    private FlowPane contenedorUsuarios;

    // ========== Variables de estado ==========
    
    /** Lista completa de usuarios */
    private List<UsuarioInfo> listaUsuariosCompleta;
    
    /** Lista observable de usuarios filtrados */
    private ObservableList<UsuarioInfo> listaUsuariosFiltrada;
    
    /** Mapa para guardar las rutas de las imágenes de perfil de usuarios (userId -> imagePath) */
    private Map<Long, String> mapaRutasImagenes = new HashMap<>();
    
    /** Carpeta donde se almacenan las fotos de perfil */
    private static final String CARPETA_FOTOS_PERFIL = "profile_pictures";

    // ========== Clase interna para información de usuario ==========
    
    /**
     * Clase para almacenar información de un usuario
     */
    private static class UsuarioInfo {
        private Long id;
        private String username;
        private String email;
        private String matricula;
        private boolean activo;
        private String rol;
        
        public UsuarioInfo(Long id, String username, String email, String matricula, boolean activo, String rol) {
            this.id = id;
            this.username = username;
            this.email = email;
            this.matricula = matricula;
            this.activo = activo;
            this.rol = rol;
        }
        
        // Getters
        public Long getId() { return id; }
        public String getUsername() { return username; }
        public String getEmail() { return email; }
        public String getMatricula() { return matricula; }
        public boolean isActivo() { return activo; }
        public String getRol() { return rol; }
    }

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
        
        // Inicializar listas y mapa
        listaUsuariosCompleta = new ArrayList<>();
        listaUsuariosFiltrada = FXCollections.observableArrayList();
        mapaRutasImagenes = new HashMap<>();
        
        // Configurar filtro de roles
        configurarFiltroRoles();
        
        // Cargar usuarios desde la base de datos
        cargarUsuarios();
        
        // Configurar el sidebar
        Platform.runLater(() -> {
            configurarSidebar();
        });
    }
    
    /**
     * Configura el filtro de roles
     */
    private void configurarFiltroRoles() {
        ObservableList<String> roles = FXCollections.observableArrayList(
            "Todos los roles", "ADMIN", "USER"
        );
        filtroRol.setItems(roles);
        filtroRol.getSelectionModel().selectFirst();
    }
    
    /**
     * Configura el sidebar para resaltar el botón de usuarios como activo
     */
    private void configurarSidebar() {
        try {
            if (sidebarInclude != null) {
                // Intentar obtener el controlador desde el nodo raíz del include
                javafx.fxml.FXMLLoader loader = (javafx.fxml.FXMLLoader) sidebarInclude.getProperties().get("FXML_LOADER");
                if (loader == null) {
                    // Si no está en propiedades, buscar el controlador directamente
                    Object controller = sidebarInclude.getUserData();
                    if (controller instanceof ControladorSidebarAdmin) {
                        ((ControladorSidebarAdmin) controller).establecerVistaActiva("usuarios");
                        return;
                    }
                } else {
                    Object controller = loader.getController();
                    if (controller instanceof ControladorSidebarAdmin) {
                        ((ControladorSidebarAdmin) controller).establecerVistaActiva("usuarios");
                        return;
                    }
                }
                
                // Método alternativo: obtener botones directamente y aplicar estilos
                Button btnUsuarios = (Button) sidebarInclude.lookup("#botonUsuarios");
                Button btnSalas = (Button) sidebarInclude.lookup("#botonSalas");
                Button btnReservas = (Button) sidebarInclude.lookup("#botonReservas");
                Button btnReportes = (Button) sidebarInclude.lookup("#botonReportes");
                Button btnPerfil = (Button) sidebarInclude.lookup("#botonPerfil");
                Button btnNotificaciones = (Button) sidebarInclude.lookup("#botonNotificaciones");
                
                if (btnUsuarios != null) {
                    // Resetear todos
                    resetearBotonesSidebar(btnSalas, btnReservas, btnReportes, btnPerfil, btnNotificaciones);
                    
                    // Activar usuarios
                    btnUsuarios.getStyleClass().remove("sidebar-button");
                    if (!btnUsuarios.getStyleClass().contains("sidebar-button-active")) {
                        btnUsuarios.getStyleClass().add("sidebar-button-active");
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
     * Carga todos los usuarios desde la base de datos
     */
    private void cargarUsuarios() {
        ConexionBD conexionBD = ConexionBD.obtenerInstancia();
        
        String sql = "SELECT u.id, u.username, u.email, u.matricula, u.is_active, " +
                    "u.profile_picture_path, " +
                    "COALESCE(GROUP_CONCAT(DISTINCT r.name SEPARATOR ', '), 'Sin rol') as roles " +
                    "FROM users u " +
                    "LEFT JOIN user_roles ur ON u.id = ur.user_id " +
                    "LEFT JOIN roles r ON ur.role_id = r.id " +
                    "GROUP BY u.id, u.username, u.email, u.matricula, u.is_active, u.profile_picture_path " +
                    "ORDER BY u.username ASC";
        
        try {
            Connection conexion = conexionBD.obtenerConexion();
            try (PreparedStatement statement = conexion.prepareStatement(sql)) {
                try (ResultSet resultado = statement.executeQuery()) {
                    listaUsuariosCompleta.clear();
                    
                    while (resultado.next()) {
                        Long id = resultado.getLong("id");
                        String username = resultado.getString("username");
                        String email = resultado.getString("email");
                        String matricula = resultado.getString("matricula");
                        boolean activo = resultado.getInt("is_active") == 1;
                        String roles = resultado.getString("roles");
                        String profilePicturePath = resultado.getString("profile_picture_path");
                        
                        // Guardar la ruta de la imagen en un mapa para usarla al crear la tarjeta
                        mapaRutasImagenes.put(id, profilePicturePath);
                        
                        UsuarioInfo usuario = new UsuarioInfo(id, username, email, matricula, activo, roles);
                        listaUsuariosCompleta.add(usuario);
                    }
                    
                    // Aplicar filtros y mostrar usuarios
                    aplicarFiltros();
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al cargar usuarios: " + e.getMessage());
            mostrarAlerta("Error", "Error al cargar usuarios", 
                         "No se pudieron cargar los usuarios. Por favor, intente más tarde.", 
                         Alert.AlertType.ERROR);
        }
    }
    
    /**
     * Aplica los filtros de búsqueda y rol, y actualiza la visualización
     */
    @FXML
    private void filtrarUsuarios() {
        aplicarFiltros();
    }
    
    /**
     * Aplica los filtros y actualiza la lista visualizada
     */
    private void aplicarFiltros() {
        String textoBusqueda = campoBusqueda.getText().toLowerCase().trim();
        String rolSeleccionado = filtroRol.getSelectionModel().getSelectedItem();
        
        listaUsuariosFiltrada.clear();
        
        for (UsuarioInfo usuario : listaUsuariosCompleta) {
            // Filtro por texto de búsqueda
            boolean coincideTexto = textoBusqueda.isEmpty() ||
                                   usuario.getUsername().toLowerCase().contains(textoBusqueda) ||
                                   usuario.getEmail().toLowerCase().contains(textoBusqueda) ||
                                   usuario.getMatricula().toLowerCase().contains(textoBusqueda);
            
            // Filtro por rol
            boolean coincideRol = rolSeleccionado == null || 
                                 rolSeleccionado.equals("Todos los roles") ||
                                 usuario.getRol().contains(rolSeleccionado);
            
            if (coincideTexto && coincideRol) {
                listaUsuariosFiltrada.add(usuario);
            }
        }
        
        // Actualizar visualización
        actualizarVisualizacionUsuarios();
    }
    
    /**
     * Actualiza la visualización de las tarjetas de usuarios
     */
    private void actualizarVisualizacionUsuarios() {
        contenedorUsuarios.getChildren().clear();
        
        for (UsuarioInfo usuario : listaUsuariosFiltrada) {
            VBox tarjetaUsuario = crearTarjetaUsuario(usuario);
            contenedorUsuarios.getChildren().add(tarjetaUsuario);
        }
    }
    
    /**
     * Crea una tarjeta visual para un usuario con mejor presentación
     * 
     * @param usuario Información del usuario
     * @return VBox con la tarjeta del usuario
     */
    private VBox crearTarjetaUsuario(UsuarioInfo usuario) {
        VBox tarjeta = new VBox();
        tarjeta.getStyleClass().add("tarjeta-usuario");
        tarjeta.setSpacing(0);
        tarjeta.setPadding(new Insets(0));
        tarjeta.setPrefWidth(260);
        tarjeta.setMaxWidth(260);
        tarjeta.setMinWidth(260);
        tarjeta.setPrefHeight(380);
        tarjeta.setMaxHeight(380);
        tarjeta.setMinHeight(380);
        tarjeta.setCursor(javafx.scene.Cursor.HAND);
        
        // Hacer toda la tarjeta clicable para ver detalles
        tarjeta.setOnMouseClicked(e -> manejarEditarUsuario(usuario));
        
        // Contenedor para la imagen de perfil circular con overlay
        StackPane imagenContainer = new StackPane();
        imagenContainer.setPrefHeight(180);
        imagenContainer.setMaxHeight(180);
        imagenContainer.setMinHeight(180);
        imagenContainer.setPrefWidth(260);
        imagenContainer.getStyleClass().add("imagen-usuario-container");
        imagenContainer.setAlignment(Pos.CENTER);
        
        // Imagen circular
        ImageView imageView = new ImageView();
        imageView.setFitWidth(140);
        imageView.setFitHeight(140);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);
        imageView.setCache(true);
        imageView.getStyleClass().add("imagen-usuario-tarjeta");
        
        // Aplicar clip circular
        javafx.scene.shape.Circle clip = new javafx.scene.shape.Circle(70, 70, 70);
        imageView.setClip(clip);
        
        // Cargar imagen de perfil del usuario
        String imagePath = mapaRutasImagenes.get(usuario.getId());
        if (imagePath != null && !imagePath.isEmpty()) {
            try {
                // Construir la ruta completa desde la carpeta profile_pictures
                // Si la ruta ya incluye el nombre de la carpeta, usarla directamente
                // Si solo tiene el nombre del archivo, agregar la carpeta
                File file;
                if (imagePath.startsWith(CARPETA_FOTOS_PERFIL)) {
                    // La ruta ya incluye la carpeta
                    file = new File(imagePath);
                } else {
                    // Solo tiene el nombre del archivo, construir la ruta completa
                    file = new File(CARPETA_FOTOS_PERFIL, imagePath);
                }
                
                if (file.exists()) {
                    Image image = new Image(new FileInputStream(file));
                    imageView.setImage(image);
                } else {
                    cargarImagenPorDefectoUsuario(imageView);
                }
            } catch (FileNotFoundException e) {
                System.err.println("Archivo de imagen de usuario no encontrado: " + imagePath);
                cargarImagenPorDefectoUsuario(imageView);
            } catch (Exception e) {
                System.err.println("Error al cargar imagen de usuario: " + e.getMessage());
                cargarImagenPorDefectoUsuario(imageView);
            }
        } else {
            cargarImagenPorDefectoUsuario(imageView);
        }
        
        // Badge de estado en la esquina superior derecha de la imagen
        HBox estadoBadgeContainer = new HBox();
        estadoBadgeContainer.setAlignment(Pos.TOP_RIGHT);
        estadoBadgeContainer.setPadding(new Insets(12, 12, 0, 0));
        
        Label estadoBadge = new Label(usuario.isActivo() ? "✓ Activo" : "✗ Inactivo");
        estadoBadge.getStyleClass().add(usuario.isActivo() ? "badge-estado-activa" : "badge-estado-inactiva");
        estadoBadgeContainer.getChildren().add(estadoBadge);
        
        StackPane.setAlignment(estadoBadgeContainer, Pos.TOP_RIGHT);
        
        imagenContainer.getChildren().addAll(imageView, estadoBadgeContainer);
        
        // Contenedor para la información del usuario
        VBox infoContainer = new VBox(10);
        infoContainer.setPadding(new Insets(16));
        infoContainer.setSpacing(10);
        infoContainer.setPrefWidth(260);
        infoContainer.setStyle("-fx-background-color: #ffffff;");
        
        // Encabezado con nombre del usuario
        Label labelNombre = new Label(usuario.getUsername());
        labelNombre.getStyleClass().add("tarjeta-usuario-nombre");
        labelNombre.setWrapText(true);
        labelNombre.setMaxWidth(Double.MAX_VALUE);
        
        // Separador visual
        javafx.scene.shape.Line separador = new javafx.scene.shape.Line();
        separador.setStartX(0);
        separador.setEndX(228);
        separador.setStroke(javafx.scene.paint.Color.web("#e2e8f0"));
        separador.setStrokeWidth(1);
        
        // Grid de información (2 columnas)
        GridPane infoGrid = new GridPane();
        infoGrid.setHgap(12);
        infoGrid.setVgap(8);
        infoGrid.setPadding(new Insets(6, 0, 0, 0));
        
        // Fila 1: Email y Matrícula
        Label labelEtiquetaEmail = new Label("Email:");
        labelEtiquetaEmail.getStyleClass().add("tarjeta-etiqueta-info");
        
        Label labelEmail = new Label(usuario.getEmail());
        labelEmail.getStyleClass().add("tarjeta-valor-info");
        labelEmail.setWrapText(true);
        
        Label labelEtiquetaMatricula = new Label("Matrícula:");
        labelEtiquetaMatricula.getStyleClass().add("tarjeta-etiqueta-info");
        
        Label labelMatricula = new Label(usuario.getMatricula() != null ? usuario.getMatricula() : "N/A");
        labelMatricula.getStyleClass().add("tarjeta-valor-info");
        
        infoGrid.add(labelEtiquetaEmail, 0, 0);
        infoGrid.add(labelEmail, 1, 0);
        infoGrid.add(labelEtiquetaMatricula, 0, 1);
        infoGrid.add(labelMatricula, 1, 1);
        
        // Fila 2: Rol
        Label labelEtiquetaRol = new Label("Rol:");
        labelEtiquetaRol.getStyleClass().add("tarjeta-etiqueta-info");
        
        Label labelRol = new Label(usuario.getRol());
        labelRol.getStyleClass().add("tarjeta-valor-info");
        labelRol.setWrapText(true);
        
        infoGrid.add(labelEtiquetaRol, 0, 2);
        infoGrid.add(labelRol, 1, 2);
        
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
     * Carga una imagen por defecto en el ImageView si no hay imagen de perfil
     * @param imageView El ImageView donde cargar la imagen
     */
    private void cargarImagenPorDefectoUsuario(ImageView imageView) {
        try {
            // Primero intentar desde la carpeta profile_pictures
            File archivoDefault = new File(CARPETA_FOTOS_PERFIL, "usuario.png");
            if (archivoDefault.exists()) {
                Image imagen = new Image(new FileInputStream(archivoDefault));
                imageView.setImage(imagen);
                return;
            }
            
            // Si no existe en la carpeta, intentar desde recursos
            InputStream imagenStream = getClass().getResourceAsStream(
                "/interfaz/sara/imagenes/usuario.png"
            );
            if (imagenStream != null) {
                Image imagen = new Image(imagenStream);
                imageView.setImage(imagen);
            } else {
                System.err.println("No se encontró la imagen de perfil por defecto (usuario.png).");
                imageView.setImage(null);
            }
        } catch (Exception e) {
            System.err.println("Error al cargar imagen de perfil por defecto: " + e.getMessage());
            imageView.setImage(null);
        }
    }

    // ========== Métodos de manejo de eventos ==========
    
    /**
     * Maneja la acción de editar un usuario
     * Navega a la vista de detalle del usuario
     * 
     * @param usuario Usuario a editar
     */
    private void manejarEditarUsuario(UsuarioInfo usuario) {
        GestorNavegacion gestorNavegacion = GestorNavegacion.obtenerInstancia();
        gestorNavegacion.navegarAVistaDetalleUsuarioAdmin(usuario.getId());
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

