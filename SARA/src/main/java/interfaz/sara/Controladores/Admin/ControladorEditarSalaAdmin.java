package interfaz.sara.Controladores.Admin;

import interfaz.sara.ConexionBD.ConexionBD;
import interfaz.sara.Utilidades.GestorNavegacion;
import interfaz.sara.Utilidades.SesionUsuario;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
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
 * Controlador para la vista de editar sala (VistaEditarSalaAdmin.fxml)
 * Permite al administrador editar toda la información de una sala existente
 */
public class ControladorEditarSalaAdmin {

    // ========== Componentes FXML ==========
    
    @FXML
    private VBox sidebarInclude;
    
    @FXML
    private ImageView imagenSalaView;
    
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
    private Button btnActivarDesactivar;

    // ========== Variables de estado ==========
    
    /** ID de la sala que se está editando */
    private Long salaId;
    
    /** Estado actual de la sala */
    private boolean salaHabilitada;
    
    /** Ruta de la imagen actual de la sala */
    private String rutaImagenActual;
    
    /** Lista de ubicaciones disponibles (nombres) */
    private ObservableList<String> ubicaciones;
    
    /** Mapeo de nombres de ubicaciones a IDs */
    private Map<String, Long> mapaUbicaciones;

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
        
        // Obtener el ID de la sala a editar
        GestorNavegacion gestorNavegacion = GestorNavegacion.obtenerInstancia();
        salaId = gestorNavegacion.obtenerSalaIdSeleccionado();
        
        if (salaId == null) {
            mostrarAlerta("Error", "Sala no seleccionada", 
                         "No se ha seleccionado una sala para editar.", 
                         Alert.AlertType.ERROR);
            return;
        }
        
        // Inicializar listas
        ubicaciones = FXCollections.observableArrayList();
        mapaUbicaciones = new HashMap<>();
        
        // Cargar ubicaciones disponibles desde la base de datos
        cargarUbicaciones();
        
        // Configurar el ComboBox para permitir agregar nuevas ubicaciones
        configurarComboUbicacion();
        
        // Cargar datos de la sala desde la base de datos
        cargarDatosSala();
        
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
    
    /**
     * Configura el ComboBox de ubicaciones para permitir agregar nuevas
     */
    private void configurarComboUbicacion() {
        // El ComboBox es editable, así que el usuario puede escribir directamente
    }

    // ========== Métodos de carga de datos ==========
    
    /**
     * Carga las ubicaciones disponibles desde la tabla locations
     */
    private void cargarUbicaciones() {
        ConexionBD conexionBD = ConexionBD.obtenerInstancia();
        
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
    
    /**
     * Carga los datos de la sala desde la base de datos
     */
    private void cargarDatosSala() {
        ConexionBD conexionBD = ConexionBD.obtenerInstancia();
        
        String sql = "SELECT r.id, r.room_code, r.name, r.capacity, r.room_type, " +
                    "r.location_id, COALESCE(l.name, 'Sin ubicación') as location, " +
                    "r.is_enabled, r.image_path " +
                    "FROM rooms r " +
                    "LEFT JOIN locations l ON r.location_id = l.id " +
                    "WHERE r.id = ?";
        
        try {
            Connection conexion = conexionBD.obtenerConexion();
            try (PreparedStatement statement = conexion.prepareStatement(sql)) {
                statement.setLong(1, salaId);
                
                try (ResultSet resultado = statement.executeQuery()) {
                    if (resultado.next()) {
                        // Cargar datos básicos
                        int codigoSala = resultado.getInt("room_code");
                        String nombre = resultado.getString("name");
                        int capacidad = resultado.getInt("capacity");
                        String tipoSala = resultado.getString("room_type");
                        String ubicacionNombre = resultado.getString("location");
                        boolean habilitada = resultado.getInt("is_enabled") == 1;
                        String rutaImagen = resultado.getString("image_path");
                        
                        salaHabilitada = habilitada;
                        rutaImagenActual = rutaImagen;
                        
                        // Actualizar campos del formulario
                        campoNombre.setText(nombre != null ? nombre : "");
                        campoCodigo.setText(String.valueOf(codigoSala));
                        campoCapacidad.setText(String.valueOf(capacidad));
                        
                        // Seleccionar tipo de sala
                        seleccionarTipoSala(tipoSala);
                        
                        // Seleccionar ubicación
                        if (ubicacionNombre != null && !ubicacionNombre.equals("Sin ubicación")) {
                            comboUbicacion.getSelectionModel().select(ubicacionNombre);
                        }
                        
                        // Actualizar botón de activar/desactivar
                        btnActivarDesactivar.setText(habilitada ? "Desactivar Sala" : "Activar Sala");
                        
                        // Cargar imagen de la sala
                        cargarImagenSala(rutaImagen);
                    } else {
                        mostrarAlerta("Error", "Sala no encontrada", 
                                     "No se encontró la sala en la base de datos.", 
                                     Alert.AlertType.ERROR);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al cargar datos de la sala: " + e.getMessage());
            e.printStackTrace();
            mostrarAlerta("Error", "Error al cargar datos", 
                         "No se pudieron cargar los datos de la sala. Por favor, intente más tarde.", 
                         Alert.AlertType.ERROR);
        }
    }
    
    /**
     * Selecciona el tipo de sala en el ToggleGroup
     * 
     * @param tipoSala Tipo de sala desde la BD
     */
    private void seleccionarTipoSala(String tipoSala) {
        if (tipoSala == null) {
            return;
        }
        
        String tipoUpper = tipoSala.toUpperCase().trim();
        
        switch (tipoUpper) {
            case "BOARDROOM":
            case "SALA DE JUNTAS":
                toggleBoardroom.setSelected(true);
                break;
            case "LABORATORY":
            case "LABORATORIO":
                toggleLaboratory.setSelected(true);
                break;
            case "AUDITORIUM":
            case "AUDITORIO":
                toggleAuditorium.setSelected(true);
                break;
        }
    }
    
    /**
     * Carga la imagen de la sala desde la ruta especificada
     * 
     * @param rutaImagen Ruta de la imagen (puede ser null o vacía)
     */
    private void cargarImagenSala(String rutaImagen) {
        try {
            if (rutaImagen != null && !rutaImagen.trim().isEmpty()) {
                File archivoImagen = new File(rutaImagen);
                if (archivoImagen.exists()) {
                    Image imagen = new Image(new FileInputStream(archivoImagen));
                    imagenSalaView.setImage(imagen);
                    return;
                }
            }
            
            // Si no hay imagen o no se encontró, cargar imagen por defecto
            cargarImagenPorDefecto();
        } catch (FileNotFoundException e) {
            System.err.println("No se encontró la imagen de la sala: " + rutaImagen);
            cargarImagenPorDefecto();
        } catch (Exception e) {
            System.err.println("Error al cargar imagen de sala: " + e.getMessage());
            cargarImagenPorDefecto();
        }
    }
    
    /**
     * Carga una imagen de sala por defecto
     */
    private void cargarImagenPorDefecto() {
        try {
            InputStream imagenStream = getClass().getResourceAsStream(
                "/interfaz/sara/imagenes/room_placeholder.png"
            );
            if (imagenStream != null) {
                Image imagen = new Image(imagenStream);
                imagenSalaView.setImage(imagen);
            } else {
                imagenSalaView.setImage(null);
            }
        } catch (Exception e) {
            System.err.println("Error al cargar imagen por defecto: " + e.getMessage());
            imagenSalaView.setImage(null);
        }
    }

    // ========== Métodos de manejo de eventos ==========
    
    /**
     * Maneja el clic en el botón "Volver"
     * Navega de vuelta a la vista de detalle de sala
     */
    @FXML
    private void manejarVolver() {
        GestorNavegacion gestorNavegacion = GestorNavegacion.obtenerInstancia();
        gestorNavegacion.navegarAVistaDetalleSalaAdmin(salaId);
    }
    
    /**
     * Maneja el clic en el botón "Cancelar"
     * Regresa a la vista de detalle sin guardar cambios
     */
    @FXML
    private void manejarCancelar() {
        manejarVolver();
    }
    
    /**
     * Maneja el clic en el botón "Guardar Cambios"
     * Valida y guarda los cambios en la base de datos
     */
    @FXML
    private void manejarGuardar() {
        // Ocultar mensajes anteriores
        ocultarMensajeEstado();
        
        // Validar campos
        if (!validarCampos()) {
            return;
        }
        
        // Guardar los cambios en la base de datos
        guardarCambiosEnBD();
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
        
        // Validar código
        if (codigo.isEmpty()) {
            mostrarMensajeEstado("El código de la sala es obligatorio.", true);
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
        
        if ((ubicacionSeleccionada == null || ubicacionSeleccionada.isEmpty()) && 
            (ubicacionEscrita == null || ubicacionEscrita.isEmpty())) {
            mostrarMensajeEstado("Debe seleccionar o escribir una ubicación.", true);
            comboUbicacion.requestFocus();
            return false;
        }
        
        return true;
    }
    
    /**
     * Obtiene el tipo de sala seleccionado en el ToggleGroup
     * 
     * @return El código del tipo de sala seleccionado, o null si ninguno está seleccionado
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
     * Guarda los cambios de la sala en la base de datos
     */
    private void guardarCambiosEnBD() {
        String nombre = campoNombre.getText().trim();
        String codigoStr = campoCodigo.getText().trim();
        int capacidad = Integer.parseInt(campoCapacidad.getText().trim());
        String tipoSala = obtenerTipoSalaSeleccionado();
        
        // Obtener ubicación (puede ser seleccionada o escrita)
        String ubicacionEscrita = comboUbicacion.getEditor().getText().trim();
        String ubicacionSeleccionada = comboUbicacion.getSelectionModel().getSelectedItem();
        String nombreUbicacion = ubicacionSeleccionada != null && !ubicacionSeleccionada.isEmpty() 
                                 ? ubicacionSeleccionada 
                                 : ubicacionEscrita;
        
        // Obtener el ID de la ubicación (puede ser null si no se seleccionó/escribió)
        Long locationId = null;
        if (nombreUbicacion != null && !nombreUbicacion.trim().isEmpty()) {
            locationId = mapaUbicaciones.get(nombreUbicacion);
            
            // Si el usuario escribió una ubicación nueva que no está en el mapa, agregarla
            if (locationId == null) {
                // Intentar agregar la nueva ubicación
                boolean agregada = agregarUbicacionSilenciosa(nombreUbicacion);
                if (agregada) {
                    locationId = mapaUbicaciones.get(nombreUbicacion);
                }
            }
        }
        // Nota: locationId puede ser null, lo cual es válido según el esquema (location_id DEFAULT NULL)
        
        // Convertir código a número
        int codigoSala;
        try {
            codigoSala = Integer.parseInt(codigoStr);
        } catch (NumberFormatException e) {
            // Si no es un número, generar un código basado en el hash del string
            codigoSala = Math.abs(codigoStr.hashCode()) % 10000;
        }
        
        // Mantener la imagen actual (no se puede cambiar en edición)
        String rutaImagen = rutaImagenActual;
        
        ConexionBD conexionBD = ConexionBD.obtenerInstancia();
        
        String sql = "UPDATE rooms SET room_code = ?, name = ?, capacity = ?, room_type = ?, " +
                    "location_id = ?, image_path = ? WHERE id = ?";
        
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
                statement.setLong(7, salaId);
                
                System.out.println("Actualizando sala ID: " + salaId);
                System.out.println("Nueva ruta de imagen: " + rutaImagen);
                
                int filasAfectadas = statement.executeUpdate();
                
                if (filasAfectadas > 0) {
                    // Mostrar mensaje de éxito
                    mostrarMensajeEstado("¡Cambios guardados exitosamente! Redirigiendo...", false);
                    
                    // Navegar de vuelta a la lista de salas después de un breve delay
                    Platform.runLater(() -> {
                        try {
                            Thread.sleep(1500); // Esperar 1.5 segundos para que el usuario vea el mensaje
                            Platform.runLater(() -> {
                                GestorNavegacion gestorNavegacion = GestorNavegacion.obtenerInstancia();
                                gestorNavegacion.navegarAVistaSalasAdmin();
                            });
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            // Navegar inmediatamente si se interrumpe
                            GestorNavegacion gestorNavegacion = GestorNavegacion.obtenerInstancia();
                            gestorNavegacion.navegarAVistaSalasAdmin();
                        }
                    });
                } else {
                    mostrarMensajeEstado("Error al guardar los cambios.", true);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al guardar cambios en BD: " + e.getMessage());
            e.printStackTrace();
            
            // Verificar si es un error de duplicado
            if (e.getMessage().contains("Duplicate entry") || e.getMessage().contains("UNIQUE constraint")) {
                if (e.getMessage().contains("room_code")) {
                    mostrarMensajeEstado("El código de sala ya existe. Por favor, use otro código.", true);
                } else if (e.getMessage().contains("name")) {
                    mostrarMensajeEstado("El nombre de sala ya existe. Por favor, use otro nombre.", true);
                } else {
                    mostrarMensajeEstado("Error: Datos duplicados en la base de datos.", true);
                }
            } else {
                mostrarMensajeEstado("Error al guardar cambios: " + e.getMessage(), true);
            }
        }
    }
    
    /**
     * Maneja el clic en el botón "Activar/Desactivar Sala"
     * Cambia el estado activo/inactivo de la sala
     */
    @FXML
    private void manejarActivarDesactivar() {
        String accion = salaHabilitada ? "desactivar" : "activar";
        
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar Acción");
        confirmacion.setHeaderText(accion.substring(0, 1).toUpperCase() + accion.substring(1) + " Sala");
        confirmacion.setContentText("¿Está seguro que desea " + accion + " la sala " + 
                                   campoNombre.getText().trim() + "?");
        
        confirmacion.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                cambiarEstadoSala(!salaHabilitada);
            }
        });
    }
    
    /**
     * Cambia el estado (habilitada/deshabilitada) de la sala
     * 
     * @param nuevoEstado true para habilitar, false para deshabilitar
     */
    private void cambiarEstadoSala(boolean nuevoEstado) {
        ConexionBD conexionBD = ConexionBD.obtenerInstancia();
        
        String sql = "UPDATE rooms SET is_enabled = ? WHERE id = ?";
        
        try {
            Connection conexion = conexionBD.obtenerConexion();
            try (PreparedStatement statement = conexion.prepareStatement(sql)) {
                statement.setInt(1, nuevoEstado ? 1 : 0);
                statement.setLong(2, salaId);
                
                int filasAfectadas = statement.executeUpdate();
                
                if (filasAfectadas > 0) {
                    salaHabilitada = nuevoEstado;
                    btnActivarDesactivar.setText(nuevoEstado ? "Desactivar Sala" : "Activar Sala");
                    
                    mostrarAlerta("Éxito", "Estado actualizado", 
                                 "El estado de la sala ha sido actualizado exitosamente.", 
                                 Alert.AlertType.INFORMATION);
                } else {
                    mostrarAlerta("Error", "Error al actualizar", 
                                 "No se pudo actualizar el estado de la sala.", 
                                 Alert.AlertType.ERROR);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al cambiar estado de la sala: " + e.getMessage());
            e.printStackTrace();
            mostrarAlerta("Error", "Error al actualizar", 
                         "No se pudo cambiar el estado de la sala. Por favor, intente más tarde.", 
                         Alert.AlertType.ERROR);
        }
    }
    
    /**
     * Maneja el clic en el botón "Eliminar Sala"
     * Elimina (soft delete) la sala
     */
    @FXML
    private void manejarEliminar() {
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar Eliminación");
        confirmacion.setHeaderText("Eliminar Sala");
        confirmacion.setContentText("¿Está seguro que desea eliminar la sala " + 
                                   campoNombre.getText().trim() + "?\n\n" +
                                   "Esta acción no se puede deshacer. La sala será eliminada permanentemente.");
        
        confirmacion.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                eliminarSalaDeBD();
            }
        });
    }
    
    /**
     * Elimina la sala de la base de datos (soft delete)
     */
    private void eliminarSalaDeBD() {
        ConexionBD conexionBD = ConexionBD.obtenerInstancia();
        
        // Soft delete: marcar deleted_at en lugar de eliminar físicamente
        String sql = "UPDATE rooms SET deleted_at = NOW() WHERE id = ?";
        
        try {
            Connection conexion = conexionBD.obtenerConexion();
            try (PreparedStatement statement = conexion.prepareStatement(sql)) {
                statement.setLong(1, salaId);
                
                int filasAfectadas = statement.executeUpdate();
                
                if (filasAfectadas > 0) {
                    mostrarAlerta("Éxito", "Sala eliminada", 
                                 "La sala ha sido eliminada exitosamente.", 
                                 Alert.AlertType.INFORMATION);
                    
                    // Volver a la lista de salas
                    GestorNavegacion gestorNavegacion = GestorNavegacion.obtenerInstancia();
                    gestorNavegacion.navegarAVistaSalasAdmin();
                } else {
                    mostrarAlerta("Error", "Error al eliminar", 
                                 "No se pudo eliminar la sala.", 
                                 Alert.AlertType.ERROR);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al eliminar sala: " + e.getMessage());
            e.printStackTrace();
            
            // Verificar si hay restricciones de clave foránea
            if (e.getMessage().contains("foreign key constraint")) {
                mostrarAlerta("Error", "No se puede eliminar", 
                             "No se puede eliminar esta sala porque tiene reservas o reportes asociados. " +
                             "Considere desactivar la sala en su lugar.", 
                             Alert.AlertType.ERROR);
            } else {
                mostrarAlerta("Error", "Error al eliminar", 
                             "No se pudo eliminar la sala. Por favor, intente más tarde.", 
                             Alert.AlertType.ERROR);
            }
        }
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
     * Maneja el clic en el botón "Gestionar" ubicaciones
     * (Reutiliza el mismo método del controlador de registrar sala)
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
        ButtonType botonAgregar = new ButtonType("Agregar", javafx.scene.control.ButtonBar.ButtonData.OK_DONE);
        ButtonType botonEditar = new ButtonType("Editar", javafx.scene.control.ButtonBar.ButtonData.LEFT);
        ButtonType botonEliminar = new ButtonType("Eliminar", javafx.scene.control.ButtonBar.ButtonData.LEFT);
        ButtonType botonCancelar = new ButtonType("Cancelar", javafx.scene.control.ButtonBar.ButtonData.CANCEL_CLOSE);
        
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
                    agregarUbicacionDialogo(textoUbicacion);
                } else {
                    mostrarAlerta("Error", "Campo vacío", 
                                 "Por favor, ingrese un nombre para la ubicación.", 
                                 Alert.AlertType.WARNING);
                }
            } else if (resultado == botonEditar) {
                String ubicacionSeleccionada = comboUbicacionesExistentes.getSelectionModel().getSelectedItem();
                if (ubicacionSeleccionada != null && !textoUbicacion.isEmpty()) {
                    editarUbicacionDialogo(ubicacionSeleccionada, textoUbicacion);
                } else {
                    mostrarAlerta("Error", "Datos incompletos", 
                                 "Seleccione una ubicación existente y escriba el nuevo nombre.", 
                                 Alert.AlertType.WARNING);
                }
            } else if (resultado == botonEliminar) {
                String ubicacionSeleccionada = comboUbicacionesExistentes.getSelectionModel().getSelectedItem();
                if (ubicacionSeleccionada != null) {
                    eliminarUbicacionDialogo(ubicacionSeleccionada);
                } else {
                    mostrarAlerta("Error", "No seleccionada", 
                                 "Por favor, seleccione una ubicación para eliminar.", 
                                 Alert.AlertType.WARNING);
                }
            }
        });
    }
    
    /**
     * Agrega una nueva ubicación desde el diálogo
     */
    private void agregarUbicacionDialogo(String nombreUbicacion) {
        if (ubicaciones.contains(nombreUbicacion)) {
            mostrarAlerta("Información", "Ubicación existente", 
                         "Esta ubicación ya existe en la lista.", 
                         Alert.AlertType.INFORMATION);
            return;
        }
        
        ConexionBD conexionBD = ConexionBD.obtenerInstancia();
        String sql = "INSERT INTO locations (name, description, is_active) VALUES (?, ?, 1)";
        
        try {
            Connection conexion = conexionBD.obtenerConexion();
            try (PreparedStatement statement = conexion.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
                statement.setString(1, nombreUbicacion);
                statement.setString(2, "Ubicación: " + nombreUbicacion);
                
                int filasAfectadas = statement.executeUpdate();
                
                if (filasAfectadas > 0) {
                    try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            Long nuevoId = generatedKeys.getLong(1);
                            ubicaciones.add(nombreUbicacion);
                            mapaUbicaciones.put(nombreUbicacion, nuevoId);
                            comboUbicacion.getItems().setAll(ubicaciones);
                            mostrarAlerta("Éxito", "Ubicación agregada", 
                                         "La ubicación ha sido agregada exitosamente.", 
                                         Alert.AlertType.INFORMATION);
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
                             "No se pudo agregar la ubicación.", 
                             Alert.AlertType.ERROR);
            }
        }
    }
    
    /**
     * Edita una ubicación desde el diálogo
     */
    private void editarUbicacionDialogo(String ubicacionAntigua, String ubicacionNueva) {
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
        
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar Edición");
        confirmacion.setHeaderText("Actualizar ubicación");
        confirmacion.setContentText("¿Desea cambiar el nombre de la ubicación de '" + 
                                   ubicacionAntigua + "' a '" + ubicacionNueva + "'?");
        
        confirmacion.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                ConexionBD conexionBD = ConexionBD.obtenerInstancia();
                String sql = "UPDATE locations SET name = ?, description = ? WHERE id = ?";
                
                try {
                    Connection conexionEditar = conexionBD.obtenerConexion();
                    try (PreparedStatement statement = conexionEditar.prepareStatement(sql)) {
                        statement.setString(1, ubicacionNueva);
                        statement.setString(2, "Ubicación: " + ubicacionNueva);
                        statement.setLong(3, ubicacionId);
                        
                        int filasAfectadas = statement.executeUpdate();
                        
                        if (filasAfectadas > 0) {
                            int indice = ubicaciones.indexOf(ubicacionAntigua);
                            if (indice >= 0) {
                                ubicaciones.set(indice, ubicacionNueva);
                                mapaUbicaciones.remove(ubicacionAntigua);
                                mapaUbicaciones.put(ubicacionNueva, ubicacionId);
                                comboUbicacion.getItems().setAll(ubicaciones);
                                
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
                                     "No se pudo actualizar la ubicación.", 
                                     Alert.AlertType.ERROR);
                    }
                }
            }
        });
    }
    
    /**
     * Elimina una ubicación desde el diálogo
     */
    private void eliminarUbicacionDialogo(String nombreUbicacion) {
        Long ubicacionId = mapaUbicaciones.get(nombreUbicacion);
        if (ubicacionId == null) {
            mostrarAlerta("Error", "Ubicación no encontrada", 
                         "No se encontró el ID de la ubicación.", 
                         Alert.AlertType.ERROR);
            return;
        }
        
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
                                         " sala(s) que la están usando.", 
                                         Alert.AlertType.ERROR);
                            return;
                        }
                    }
                }
            }
            
            Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
            confirmacion.setTitle("Confirmar Eliminación");
            confirmacion.setHeaderText("Eliminar ubicación");
            confirmacion.setContentText("¿Está seguro que desea eliminar la ubicación '" + nombreUbicacion + "'?");
            
            confirmacion.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    String sqlEliminar = "UPDATE locations SET is_active = 0 WHERE id = ?";
                    
                    try {
                        Connection conexionEliminar = conexionBD.obtenerConexion();
                        try (PreparedStatement statement = conexionEliminar.prepareStatement(sqlEliminar)) {
                            statement.setLong(1, ubicacionId);
                            int filasAfectadas = statement.executeUpdate();
                            
                            if (filasAfectadas > 0) {
                                ubicaciones.remove(nombreUbicacion);
                                mapaUbicaciones.remove(nombreUbicacion);
                                comboUbicacion.getItems().setAll(ubicaciones);
                                
                                if (comboUbicacion.getSelectionModel().getSelectedItem() != null &&
                                    comboUbicacion.getSelectionModel().getSelectedItem().equals(nombreUbicacion)) {
                                    comboUbicacion.getSelectionModel().clearSelection();
                                }
                                
                                mostrarAlerta("Éxito", "Ubicación eliminada", 
                                             "La ubicación ha sido eliminada exitosamente.", 
                                             Alert.AlertType.INFORMATION);
                            }
                        }
                    } catch (SQLException e) {
                        System.err.println("Error al eliminar ubicación: " + e.getMessage());
                        mostrarAlerta("Error", "Error al eliminar", 
                                     "No se pudo eliminar la ubicación.", 
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

