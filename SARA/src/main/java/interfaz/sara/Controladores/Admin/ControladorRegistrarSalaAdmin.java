package interfaz.sara.Controladores.Admin;

import interfaz.sara.ConexionBD.ConexionBD;
import interfaz.sara.Utilidades.GestorNavegacionAdmin;
import interfaz.sara.Utilidades.SesionUsuario;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.GridPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Controlador para la vista de registrar nueva sala (VistaRegistrarSalaAdmin.fxml)
 * Permite al administrador crear nuevas salas en el sistema
 */
public class ControladorRegistrarSalaAdmin {

    // ========== Componentes FXML ==========
    
    @FXML
    private VBox sidebarInclude;
    
    @FXML
    private TextField campoNombre;
    
    @FXML
    private TextField campoCodigo;
    
    @FXML
    private TextField campoCapacidad;
    
    @FXML
    private ToggleGroup grupoTipoSala;
    
    @FXML
    private ToggleButton toggleBoardroom;
    
    @FXML
    private ToggleButton toggleLaboratory;
    
    @FXML
    private ToggleButton toggleAuditorium;
    
    @FXML
    private ComboBox<String> comboUbicacion;
    
    @FXML
    private Label mensajeEstado;
    
    @FXML
    private ImageView imagenSalaView;

    // ========== Variables de estado ==========
    
    /** Lista de ubicaciones disponibles (nombres) */
    private ObservableList<String> ubicaciones;
    
    /** Mapeo de nombres de ubicaciones a IDs */
    private java.util.Map<String, Long> mapaUbicaciones;
    
    /** Archivo de imagen seleccionado */
    private File archivoImagenSeleccionado;
    
    private static final String CARPETA_IMAGENES_SALAS = "SARA/room_images";

    // ========== Métodos de inicialización ==========
    
    /**
     * Inicializa el controlador después de que se carga el FXML
     */
    @FXML
    private void initialize() {
        // Verificar que el usuario es admin
        SesionUsuario sesion = SesionUsuario.obtenerInstancia();
        if (!sesion.estaAutenticado() || !sesion.esAdmin()) {
            mostrarAlerta("Acceso Denegado", "No autorizado", 
                         "Solo los administradores pueden acceder a esta sección.", 
                         Alert.AlertType.ERROR);
            return;
        }
        
        // Inicializar ubicaciones
        ubicaciones = FXCollections.observableArrayList();
        mapaUbicaciones = new java.util.HashMap<>();
        
        // Cargar ubicaciones disponibles desde la base de datos
        cargarUbicaciones();
        
        // Configurar el ComboBox para permitir agregar nuevas ubicaciones
        configurarComboUbicacion();
        
        // Cargar imagen por defecto
        cargarImagenPorDefecto();
        
        // Configurar el sidebar
        Platform.runLater(() -> {
            configurarSidebar();
        });
    }
    
    /**
     * Configura el ComboBox de ubicaciones para permitir agregar nuevas
     */
    private void configurarComboUbicacion() {
        // El ComboBox es editable, así que el usuario puede escribir directamente
        // Cuando guarde la sala, si escribió algo nuevo, se agregará automáticamente
    }
    
    /**
     * Carga una imagen por defecto para la vista previa
     */
    private void cargarImagenPorDefecto() {
        try {
            InputStream imagenStream = getClass().getResourceAsStream(
                "/interfaz/sara/imagenes/sala_placeholder.png"
            );
            if (imagenStream != null) {
                Image imagen = new Image(imagenStream);
                imagenSalaView.setImage(imagen);
            } else {
                // Si no hay placeholder, dejar vacío
                imagenSalaView.setImage(null);
            }
        } catch (Exception e) {
            System.err.println("Error al cargar imagen por defecto: " + e.getMessage());
            imagenSalaView.setImage(null);
        }
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
     * Maneja el clic en el botón "Gestionar" ubicaciones
     * Abre un diálogo para agregar, editar o eliminar ubicaciones
     */
    @FXML
    private void manejarGestionarUbicaciones() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Gestionar Ubicaciones");
        dialog.setHeaderText("Agregar, editar o eliminar ubicaciones de salas");
        
        // Crear el contenido del diálogo
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 20, 10, 20));
        
        // ComboBox con las ubicaciones existentes
        ComboBox<String> comboUbicacionesExistentes = new ComboBox<>();
        comboUbicacionesExistentes.setItems(ubicaciones);
        comboUbicacionesExistentes.setPrefWidth(300);
        comboUbicacionesExistentes.setPromptText("Seleccionar ubicación para editar/eliminar");
        
        // Campo para agregar/editar ubicación
        TextField campoNuevaUbicacion = new TextField();
        campoNuevaUbicacion.setPromptText("Escriba el nombre de la ubicación");
        campoNuevaUbicacion.setPrefWidth(300);
        
        grid.add(new Label("Ubicación existente:"), 0, 0);
        grid.add(comboUbicacionesExistentes, 1, 0);
        grid.add(new Label("Nueva/Editar ubicación:"), 0, 1);
        grid.add(campoNuevaUbicacion, 1, 1);
        
        dialog.getDialogPane().setContent(grid);
        
        // Botones del diálogo
        ButtonType botonAgregar = new ButtonType("Agregar", ButtonData.OK_DONE);
        ButtonType botonEditar = new ButtonType("Editar", ButtonData.LEFT);
        ButtonType botonEliminar = new ButtonType("Eliminar", ButtonData.LEFT);
        ButtonType botonCancelar = new ButtonType("Cancelar", ButtonData.CANCEL_CLOSE);
        
        dialog.getDialogPane().getButtonTypes().addAll(botonAgregar, botonEditar, botonEliminar, botonCancelar);
        
        // Pre-llenar el campo cuando se selecciona una ubicación existente
        comboUbicacionesExistentes.setOnAction(e -> {
            String seleccionada = comboUbicacionesExistentes.getSelectionModel().getSelectedItem();
            if (seleccionada != null) {
                campoNuevaUbicacion.setText(seleccionada);
            }
        });
        
        // Mostrar el diálogo y procesar la respuesta
        dialog.showAndWait().ifPresent(resultado -> {
            String textoUbicacion = campoNuevaUbicacion.getText().trim();
            
            if (resultado == botonAgregar) {
                if (!textoUbicacion.isEmpty()) {
                    agregarUbicacion(textoUbicacion);
                } else {
                    mostrarAlerta("Error", "Campo vacío", 
                                 "Por favor, ingrese un nombre para la ubicación.", 
                                 Alert.AlertType.WARNING);
                }
            } else if (resultado == botonEditar) {
                String ubicacionSeleccionada = comboUbicacionesExistentes.getSelectionModel().getSelectedItem();
                if (ubicacionSeleccionada != null && !textoUbicacion.isEmpty()) {
                    editarUbicacion(ubicacionSeleccionada, textoUbicacion);
                } else {
                    mostrarAlerta("Error", "Datos incompletos", 
                                 "Seleccione una ubicación existente y escriba el nuevo nombre.", 
                                 Alert.AlertType.WARNING);
                }
            } else if (resultado == botonEliminar) {
                String ubicacionSeleccionada = comboUbicacionesExistentes.getSelectionModel().getSelectedItem();
                if (ubicacionSeleccionada != null) {
                    eliminarUbicacion(ubicacionSeleccionada);
                } else {
                    mostrarAlerta("Error", "No seleccionada", 
                                 "Por favor, seleccione una ubicación para eliminar.", 
                                 Alert.AlertType.WARNING);
                }
            }
        });
    }
    
    /**
     * Agrega una nueva ubicación silenciosamente (sin mostrar alertas)
     * 
     * @param nombreUbicacion Nombre de la nueva ubicación
     * @return true si se agregó exitosamente, false en caso contrario
     */
    private boolean agregarUbicacionSilenciosa(String nombreUbicacion) {
        if (mapaUbicaciones.containsKey(nombreUbicacion)) {
            return true; // Ya existe
        }
        
        // Insertar en la base de datos
        ConexionBD conexionBD = ConexionBD.obtenerInstancia();
        String sql = "INSERT INTO locations (name, description, is_active) VALUES (?, ?, 1)";
        
        try {
            Connection conexion = conexionBD.obtenerConexion();
            try (PreparedStatement statement = conexion.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
                statement.setString(1, nombreUbicacion);
                statement.setString(2, "Ubicación: " + nombreUbicacion);
                
                int filasAfectadas = statement.executeUpdate();
                
                if (filasAfectadas > 0) {
                    // Obtener el ID generado
                    try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            Long nuevoId = generatedKeys.getLong(1);
                            
                            // Agregar a la lista local
                            ubicaciones.add(nombreUbicacion);
                            mapaUbicaciones.put(nombreUbicacion, nuevoId);
                            comboUbicacion.getItems().setAll(ubicaciones);
                            return true;
                        }
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al agregar ubicación silenciosamente: " + e.getMessage());
            return false;
        }
        return false;
    }
    
    /**
     * Agrega una nueva ubicación a la tabla locations
     * 
     * @param nombreUbicacion Nombre de la nueva ubicación
     */
    private void agregarUbicacion(String nombreUbicacion) {
        if (ubicaciones.contains(nombreUbicacion)) {
            mostrarAlerta("Información", "Ubicación existente", 
                         "Esta ubicación ya existe en la lista.", 
                         Alert.AlertType.INFORMATION);
            return;
        }
        
        // Insertar en la base de datos
        ConexionBD conexionBD = ConexionBD.obtenerInstancia();
        String sql = "INSERT INTO locations (name, description, is_active) VALUES (?, ?, 1)";
        
        try {
            Connection conexion = conexionBD.obtenerConexion();
            try (PreparedStatement statement = conexion.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
                statement.setString(1, nombreUbicacion);
                statement.setString(2, "Ubicación: " + nombreUbicacion);
                
                int filasAfectadas = statement.executeUpdate();
                
                if (filasAfectadas > 0) {
                    // Obtener el ID generado
                    try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            Long nuevoId = generatedKeys.getLong(1);
                            
                            // Agregar a la lista local
                            ubicaciones.add(nombreUbicacion);
                            mapaUbicaciones.put(nombreUbicacion, nuevoId);
                            comboUbicacion.getItems().setAll(ubicaciones);
                            
                            mostrarAlerta("Éxito", "Ubicación agregada", 
                                         "La ubicación '" + nombreUbicacion + "' ha sido agregada exitosamente.", 
                                         Alert.AlertType.INFORMATION);
                            
                            // Seleccionar la nueva ubicación en el ComboBox
                            comboUbicacion.getSelectionModel().select(nombreUbicacion);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al agregar ubicación: " + e.getMessage());
            if (e.getMessage().contains("Duplicate entry") || e.getMessage().contains("UNIQUE constraint")) {
                mostrarAlerta("Error", "Ubicación duplicada", 
                             "Ya existe una ubicación con ese nombre.", 
                             Alert.AlertType.ERROR);
            } else {
                mostrarAlerta("Error", "Error al agregar", 
                             "No se pudo agregar la ubicación: " + e.getMessage(), 
                             Alert.AlertType.ERROR);
            }
        }
    }
    
    /**
     * Edita una ubicación existente en la tabla locations
     * 
     * @param ubicacionAntigua Nombre de la ubicación a editar
     * @param ubicacionNueva Nuevo nombre de la ubicación
     */
    private void editarUbicacion(String ubicacionAntigua, String ubicacionNueva) {
        if (ubicacionAntigua.equals(ubicacionNueva)) {
            mostrarAlerta("Información", "Sin cambios", 
                         "El nombre de la ubicación no ha cambiado.", 
                         Alert.AlertType.INFORMATION);
            return;
        }
        
        if (ubicaciones.contains(ubicacionNueva)) {
            mostrarAlerta("Error", "Ubicación duplicada", 
                         "Ya existe una ubicación con ese nombre.", 
                         Alert.AlertType.ERROR);
            return;
        }
        
        Long ubicacionId = mapaUbicaciones.get(ubicacionAntigua);
        if (ubicacionId == null) {
            mostrarAlerta("Error", "Ubicación no encontrada", 
                         "No se encontró el ID de la ubicación.", 
                         Alert.AlertType.ERROR);
            return;
        }
        
        // Confirmar la edición
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar Edición");
        confirmacion.setHeaderText("Actualizar ubicación");
        confirmacion.setContentText("¿Desea cambiar el nombre de la ubicación de '" + 
                                   ubicacionAntigua + "' a '" + ubicacionNueva + "'?");
        
            confirmacion.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    // Actualizar en la tabla locations
                    ConexionBD conexionBDEditar = ConexionBD.obtenerInstancia();
                    String sql = "UPDATE locations SET name = ?, description = ? WHERE id = ?";
                    
                    try {
                        Connection conexionEditar = conexionBDEditar.obtenerConexion();
                        try (PreparedStatement statement = conexionEditar.prepareStatement(sql)) {
                        statement.setString(1, ubicacionNueva);
                        statement.setString(2, "Ubicación: " + ubicacionNueva);
                        statement.setLong(3, ubicacionId);
                        
                        int filasAfectadas = statement.executeUpdate();
                        
                        if (filasAfectadas > 0) {
                            // Actualizar la lista local
                            int indice = ubicaciones.indexOf(ubicacionAntigua);
                            if (indice >= 0) {
                                ubicaciones.set(indice, ubicacionNueva);
                                mapaUbicaciones.remove(ubicacionAntigua);
                                mapaUbicaciones.put(ubicacionNueva, ubicacionId);
                                comboUbicacion.getItems().setAll(ubicaciones);
                                
                                // Si estaba seleccionada, actualizar la selección
                                if (comboUbicacion.getSelectionModel().getSelectedItem() != null &&
                                    comboUbicacion.getSelectionModel().getSelectedItem().equals(ubicacionAntigua)) {
                                    comboUbicacion.getSelectionModel().select(ubicacionNueva);
                                }
                            }
                            
                            mostrarAlerta("Éxito", "Ubicación actualizada", 
                                         "La ubicación ha sido actualizada exitosamente.", 
                                         Alert.AlertType.INFORMATION);
                        }
                    }
                } catch (SQLException e) {
                    System.err.println("Error al editar ubicación: " + e.getMessage());
                    if (e.getMessage().contains("Duplicate entry") || e.getMessage().contains("UNIQUE constraint")) {
                        mostrarAlerta("Error", "Ubicación duplicada", 
                                     "Ya existe una ubicación con ese nombre.", 
                                     Alert.AlertType.ERROR);
                    } else {
                        mostrarAlerta("Error", "Error al actualizar", 
                                     "No se pudo actualizar la ubicación: " + e.getMessage(), 
                                     Alert.AlertType.ERROR);
                    }
                }
            }
        });
    }
    
    /**
     * Elimina (desactiva) una ubicación después de verificar que no esté en uso
     * 
     * @param nombreUbicacion Nombre de la ubicación a eliminar
     */
    private void eliminarUbicacion(String nombreUbicacion) {
        Long ubicacionId = mapaUbicaciones.get(nombreUbicacion);
        if (ubicacionId == null) {
            mostrarAlerta("Error", "Ubicación no encontrada", 
                         "No se encontró el ID de la ubicación.", 
                         Alert.AlertType.ERROR);
            return;
        }
        
        // Verificar si hay salas usando esta ubicación
        ConexionBD conexionBD = ConexionBD.obtenerInstancia();
        String sqlVerificar = "SELECT COUNT(*) as total FROM rooms WHERE location_id = ? AND deleted_at IS NULL";
        
        try {
            Connection conexion = conexionBD.obtenerConexion();
            try (PreparedStatement statement = conexion.prepareStatement(sqlVerificar)) {
                statement.setLong(1, ubicacionId);
                
                try (ResultSet resultado = statement.executeQuery()) {
                    if (resultado.next()) {
                        int total = resultado.getInt("total");
                        
                        if (total > 0) {
                            mostrarAlerta("Error", "Ubicación en uso", 
                                         "No se puede eliminar esta ubicación porque hay " + total + 
                                         " sala(s) que la están usando. " +
                                         "Edite o elimine esas salas primero.", 
                                         Alert.AlertType.ERROR);
                            return;
                        }
                    }
                }
            }
            
            // Confirmar eliminación
            Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
            confirmacion.setTitle("Confirmar Eliminación");
            confirmacion.setHeaderText("Eliminar ubicación");
            confirmacion.setContentText("¿Está seguro que desea eliminar la ubicación '" + nombreUbicacion + "'?");
            
            confirmacion.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    // Desactivar la ubicación en lugar de eliminarla (soft delete)
                    String sqlEliminar = "UPDATE locations SET is_active = 0 WHERE id = ?";
                    
                    try {
                        Connection conexionEliminar = conexionBD.obtenerConexion();
                        try (PreparedStatement statement = conexionEliminar.prepareStatement(sqlEliminar)) {
                            statement.setLong(1, ubicacionId);
                            int filasAfectadas = statement.executeUpdate();
                            
                            if (filasAfectadas > 0) {
                                // Remover de la lista local
                                ubicaciones.remove(nombreUbicacion);
                                mapaUbicaciones.remove(nombreUbicacion);
                                comboUbicacion.getItems().setAll(ubicaciones);
                                
                                // Limpiar selección si estaba seleccionada
                                if (comboUbicacion.getSelectionModel().getSelectedItem() != null &&
                                    comboUbicacion.getSelectionModel().getSelectedItem().equals(nombreUbicacion)) {
                                    comboUbicacion.getSelectionModel().clearSelection();
                                }
                                
                                mostrarAlerta("Éxito", "Ubicación eliminada", 
                                             "La ubicación '" + nombreUbicacion + "' ha sido eliminada exitosamente.", 
                                             Alert.AlertType.INFORMATION);
                            }
                        }
                    } catch (SQLException e) {
                        System.err.println("Error al eliminar ubicación: " + e.getMessage());
                        mostrarAlerta("Error", "Error al eliminar", 
                                     "No se pudo eliminar la ubicación: " + e.getMessage(), 
                                     Alert.AlertType.ERROR);
                    }
                }
            });
            
        } catch (SQLException e) {
            System.err.println("Error al verificar ubicación: " + e.getMessage());
            mostrarAlerta("Error", "Error al verificar", 
                         "No se pudo verificar si la ubicación está en uso.", 
                         Alert.AlertType.ERROR);
        }
    }
    
    /**
     * Carga las ubicaciones disponibles desde la tabla locations
     */
    private void cargarUbicaciones() {
        ConexionBD conexionBD = ConexionBD.obtenerInstancia();
        
        // Obtener ubicaciones activas de la tabla locations
        String sql = "SELECT id, name FROM locations WHERE is_active = 1 ORDER BY name ASC";
        
        try {
            Connection conexion = conexionBD.obtenerConexion();
            try (PreparedStatement statement = conexion.prepareStatement(sql)) {
                try (ResultSet resultado = statement.executeQuery()) {
                    List<String> listaUbicaciones = new ArrayList<>();
                    mapaUbicaciones.clear();
                    
                    while (resultado.next()) {
                        Long id = resultado.getLong("id");
                        String nombre = resultado.getString("name");
                        if (nombre != null && !nombre.trim().isEmpty()) {
                            listaUbicaciones.add(nombre);
                            mapaUbicaciones.put(nombre, id);
                        }
                    }
                    
                    ubicaciones.setAll(listaUbicaciones);
                    comboUbicacion.setItems(ubicaciones);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al cargar ubicaciones: " + e.getMessage());
            e.printStackTrace();
            mostrarAlerta("Error", "Error al cargar ubicaciones", 
                         "No se pudieron cargar las ubicaciones disponibles.", 
                         Alert.AlertType.WARNING);
        }
    }

    // ========== Métodos de manejo de eventos ==========
    
    /**
     * Maneja el clic en el botón "Volver"
     * Navega de vuelta a la vista de gestión de salas
     */
    @FXML
    private void manejarVolver() {
        GestorNavegacionAdmin gestorNavegacion = GestorNavegacionAdmin.obtenerInstancia();
        gestorNavegacion.navegarAVistaSalasAdmin();
    }
    
    /**
     * Maneja el clic en el botón "Cancelar"
     * Regresa a la vista de gestión de salas sin guardar
     */
    @FXML
    private void manejarCancelar() {
        manejarVolver();
    }
    
    /**
     * Maneja el clic en el botón "Seleccionar Imagen"
     * Abre un selector de archivos para elegir una imagen de la sala
     */
    @FXML
    private void manejarSeleccionarImagen() {
        Stage stage = (Stage) imagenSalaView.getScene().getWindow();
        
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar Imagen de la Sala");
        FileChooser.ExtensionFilter filtroImagenes = new FileChooser.ExtensionFilter(
            "Archivos de Imagen", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp"
        );
        fileChooser.getExtensionFilters().add(filtroImagenes);
        
        File archivoSeleccionado = fileChooser.showOpenDialog(stage);
        
        if (archivoSeleccionado != null) {
            try {
                archivoImagenSeleccionado = archivoSeleccionado;
                
                // Mostrar la imagen seleccionada en el ImageView
                Image imagen = new Image(new FileInputStream(archivoSeleccionado));
                imagenSalaView.setImage(imagen);
                
                System.out.println("Imagen seleccionada: " + archivoSeleccionado.getAbsolutePath());
            } catch (FileNotFoundException e) {
                System.err.println("Error al cargar imagen: " + e.getMessage());
                mostrarMensajeEstado("Error al cargar la imagen seleccionada.", true);
            }
        }
    }
    
    /**
     * Maneja el clic en el botón "Guardar Sala"
     * Valida y guarda la nueva sala en la base de datos
     */
    @FXML
    private void manejarGuardar() {
        // Ocultar mensajes anteriores
        ocultarMensajeEstado();
        
        // Validar campos
        if (!validarCampos()) {
            return;
        }
        
        // Guardar la sala en la base de datos
        guardarSalaEnBD();
    }
    
    /**
     * Valida que todos los campos requeridos estén completos y sean válidos
     * 
     * @return true si todos los campos son válidos, false en caso contrario
     */
    private boolean validarCampos() {
        String nombre = campoNombre.getText().trim();
        String codigo = campoCodigo.getText().trim();
        String capacidadStr = campoCapacidad.getText().trim();
        String tipoSala = obtenerTipoSalaSeleccionado();
        
        // Validar nombre
        if (nombre.isEmpty()) {
            mostrarMensajeEstado("El nombre de la sala es obligatorio.", true);
            campoNombre.requestFocus();
            return false;
        }
        
        if (nombre.length() < 3) {
            mostrarMensajeEstado("El nombre de la sala debe tener al menos 3 caracteres.", true);
            campoNombre.requestFocus();
            return false;
        }
        
        if (nombre.length() > 100) {
            mostrarMensajeEstado("El nombre de la sala no puede exceder 100 caracteres.", true);
            campoNombre.requestFocus();
            return false;
        }
        
        // Validar código
        if (codigo.isEmpty()) {
            mostrarMensajeEstado("El código de la sala es obligatorio.", true);
            campoCodigo.requestFocus();
            return false;
        }
        
        if (codigo.length() > 20) {
            mostrarMensajeEstado("El código de la sala no puede exceder 20 caracteres.", true);
            campoCodigo.requestFocus();
            return false;
        }
        
        // Validar capacidad
        if (capacidadStr.isEmpty()) {
            mostrarMensajeEstado("La capacidad es obligatoria.", true);
            campoCapacidad.requestFocus();
            return false;
        }
        
        try {
            int capacidad = Integer.parseInt(capacidadStr);
            if (capacidad <= 0) {
                mostrarMensajeEstado("La capacidad debe ser mayor a 0.", true);
                campoCapacidad.requestFocus();
                return false;
            }
            
            if (capacidad > 1000) {
                mostrarMensajeEstado("La capacidad no puede exceder 1000 personas.", true);
                campoCapacidad.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            mostrarMensajeEstado("La capacidad debe ser un número válido.", true);
            campoCapacidad.requestFocus();
            return false;
        }
        
        // Validar tipo de sala
        if (tipoSala == null || tipoSala.isEmpty()) {
            mostrarMensajeEstado("Debe seleccionar un tipo de sala.", true);
            return false;
        }
        
        // Validar ubicación (puede ser de la lista o escrita por el usuario)
        String ubicacionEscrita = comboUbicacion.getEditor().getText().trim();
        String ubicacionSeleccionada = comboUbicacion.getSelectionModel().getSelectedItem();
        String ubicacionFinal = ubicacionSeleccionada != null ? ubicacionSeleccionada : ubicacionEscrita;
        
        if (ubicacionFinal == null || ubicacionFinal.isEmpty()) {
            mostrarMensajeEstado("Debe seleccionar o escribir una ubicación.", true);
            comboUbicacion.requestFocus();
            return false;
        }
        
        if (ubicacionFinal.length() > 100) {
            mostrarMensajeEstado("La ubicación no puede exceder 100 caracteres.", true);
            comboUbicacion.requestFocus();
            return false;
        }
        
        // Si el usuario escribió una ubicación nueva, agregarla a la lista
        if (ubicacionSeleccionada == null && !ubicacionEscrita.isEmpty() && 
            !ubicaciones.contains(ubicacionEscrita)) {
            ubicaciones.add(ubicacionEscrita);
            comboUbicacion.getItems().setAll(ubicaciones);
        }
        
        return true;
    }
    
    /**
     * Obtiene el tipo de sala seleccionado en el ToggleGroup
     * 
     * @return El nombre del tipo de sala seleccionado, o null si ninguno está seleccionado
     */
    private String obtenerTipoSalaSeleccionado() {
        ToggleButton seleccionado = (ToggleButton) grupoTipoSala.getSelectedToggle();
        if (seleccionado == null) {
            return null;
        }
        
        // Mapear los textos de los botones a los valores de la base de datos
        String texto = seleccionado.getText();
        if (texto.equals("Sala de Juntas")) {
            return "BOARDROOM";
        } else if (texto.equals("Laboratorio")) {
            return "LABORATORY";
        } else if (texto.equals("Auditorio")) {
            return "AUDITORIUM";
        }
        
        return texto.toUpperCase();
    }
    
    /**
     * Guarda la nueva sala en la base de datos
     */
    private void guardarSalaEnBD() {
        String nombre = campoNombre.getText().trim();
        String codigoStr = campoCodigo.getText().trim();
        int capacidad = Integer.parseInt(campoCapacidad.getText().trim());
        String tipoSala = obtenerTipoSalaSeleccionado();
        
        // Obtener ubicación (puede ser seleccionada o escrita)
        String ubicacionEscrita = comboUbicacion.getEditor().getText().trim();
        String ubicacionSeleccionada = comboUbicacion.getSelectionModel().getSelectedItem();
        String nombreUbicacion = ubicacionSeleccionada != null ? ubicacionSeleccionada : ubicacionEscrita;
        
        // Obtener el ID de la ubicación
        Long locationId = mapaUbicaciones.get(nombreUbicacion);
        
        // Si el usuario escribió una ubicación nueva que no está en el mapa, agregarla
        if (locationId == null && !nombreUbicacion.isEmpty()) {
            // Intentar agregar la nueva ubicación
            agregarUbicacionSilenciosa(nombreUbicacion);
            locationId = mapaUbicaciones.get(nombreUbicacion);
        }
        
        if (locationId == null) {
            mostrarMensajeEstado("Error: No se pudo obtener el ID de la ubicación.", true);
            return;
        }
        
        // Convertir código a número (intentar parsear, si falla usar un hash del string)
        int codigoSala;
        try {
            codigoSala = Integer.parseInt(codigoStr);
        } catch (NumberFormatException e) {
            // Si no es un número, generar un código basado en el hash del string
            codigoSala = Math.abs(codigoStr.hashCode()) % 10000; // Código de hasta 4 dígitos
        }
        
        // Guardar la imagen si se seleccionó una
        String rutaImagen = null;
        if (archivoImagenSeleccionado != null) {
            try {
                rutaImagen = copiarImagenSala(archivoImagenSeleccionado, codigoSala);
            } catch (IOException e) {
                System.err.println("Error al copiar imagen de sala: " + e.getMessage());
                mostrarMensajeEstado("Error al guardar la imagen. La sala se guardará sin imagen.", true);
            }
        }
        
        ConexionBD conexionBD = ConexionBD.obtenerInstancia();
        
        // Nota: created_at tiene DEFAULT current_timestamp() en la BD, no es necesario establecerlo explícitamente
        String sql = "INSERT INTO rooms (room_code, name, capacity, room_type, location_id, is_enabled, image_path) " +
                    "VALUES (?, ?, ?, ?, ?, 1, ?)";
        
        try {
            Connection conexion = conexionBD.obtenerConexion();
            try (PreparedStatement statement = conexion.prepareStatement(sql)) {
                statement.setInt(1, codigoSala);
                statement.setString(2, nombre);
                statement.setInt(3, capacidad);
                statement.setString(4, tipoSala);
                // location_id puede ser NULL según el esquema
                if (locationId != null) {
                    statement.setLong(5, locationId);
                } else {
                    statement.setNull(5, java.sql.Types.BIGINT);
                }
                // image_path puede ser NULL según el esquema
                if (rutaImagen != null && !rutaImagen.trim().isEmpty()) {
                    statement.setString(6, rutaImagen);
                } else {
                    statement.setNull(6, java.sql.Types.VARCHAR);
                }
                
                int filasAfectadas = statement.executeUpdate();
                
                if (filasAfectadas > 0) {
                    mostrarMensajeEstado("¡Sala registrada exitosamente!", false);
                    
                    // Limpiar formulario después de un breve delay
                    Platform.runLater(() -> {
                        try {
                            Thread.sleep(1500);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                        limpiarFormulario();
                        manejarVolver();
                    });
                } else {
                    mostrarMensajeEstado("Error al registrar la sala. Por favor, intente nuevamente.", true);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al guardar sala en BD: " + e.getMessage());
            e.printStackTrace();
            
            // Verificar si es un error de duplicado
            if (e.getMessage().contains("Duplicate entry") || e.getMessage().contains("duplicate")) {
                mostrarMensajeEstado("El código de sala ya existe. Por favor, use otro código.", true);
            } else {
                mostrarMensajeEstado("Error al registrar la sala: " + e.getMessage(), true);
            }
        }
    }
    
    /**
     * Copia la imagen seleccionada a la carpeta de imágenes de salas
     * 
     * @param archivoOriginal Archivo de imagen seleccionado
     * @param codigoSala Código de la sala para usar en el nombre del archivo
     * @return Ruta relativa donde se guardó la imagen
     * @throws IOException Si ocurre un error al copiar el archivo
     */
    private String copiarImagenSala(File archivoOriginal, int codigoSala) throws IOException {
        Path carpetaImagenes = Paths.get(CARPETA_IMAGENES_SALAS);
        if (!Files.exists(carpetaImagenes)) {
            Files.createDirectories(carpetaImagenes);
        }
        
        String extension = obtenerExtension(archivoOriginal.getName());
        String nombreArchivo = String.format("sala_%d_%d.%s",
                                             codigoSala,
                                             System.currentTimeMillis(),
                                             extension);
        
        Path nuevoArchivoPath = carpetaImagenes.resolve(nombreArchivo);
        Files.copy(archivoOriginal.toPath(), nuevoArchivoPath);
        
        return nuevoArchivoPath.toString(); // Retorna la ruta relativa
    }
    
    /**
     * Obtiene la extensión de un nombre de archivo
     * 
     * @param nombreArchivo El nombre del archivo
     * @return La extensión del archivo (ej. "png", "jpg"), o "jpg" si no tiene
     */
    private String obtenerExtension(String nombreArchivo) {
        int lastDotIndex = nombreArchivo.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < nombreArchivo.length() - 1) {
            return nombreArchivo.substring(lastDotIndex + 1).toLowerCase();
        }
        return "jpg"; // Extensión por defecto
    }
    
    /**
     * Limpia todos los campos del formulario
     */
    private void limpiarFormulario() {
        campoNombre.clear();
        campoCodigo.clear();
        campoCapacidad.clear();
        grupoTipoSala.selectToggle(null);
        comboUbicacion.getSelectionModel().clearSelection();
        archivoImagenSeleccionado = null;
        cargarImagenPorDefecto();
        ocultarMensajeEstado();
    }
    
    /**
     * Muestra un mensaje de estado en el formulario
     * 
     * @param mensaje El mensaje a mostrar
     * @param esError true si es un mensaje de error, false si es de éxito
     */
    private void mostrarMensajeEstado(String mensaje, boolean esError) {
        mensajeEstado.setText(mensaje);
        mensajeEstado.setVisible(true);
        mensajeEstado.setManaged(true);
        
        if (esError) {
            mensajeEstado.getStyleClass().remove("mensaje-estado-exito");
            if (!mensajeEstado.getStyleClass().contains("mensaje-estado-error")) {
                mensajeEstado.getStyleClass().add("mensaje-estado-error");
            }
            mensajeEstado.setStyle("-fx-text-fill: #ef4444;");
        } else {
            mensajeEstado.getStyleClass().remove("mensaje-estado-error");
            if (!mensajeEstado.getStyleClass().contains("mensaje-estado-exito")) {
                mensajeEstado.getStyleClass().add("mensaje-estado-exito");
            }
            mensajeEstado.setStyle("-fx-text-fill: #10b981;");
        }
    }
    
    /**
     * Oculta el mensaje de estado
     */
    private void ocultarMensajeEstado() {
        mensajeEstado.setVisible(false);
        mensajeEstado.setManaged(false);
        mensajeEstado.setText("");
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

