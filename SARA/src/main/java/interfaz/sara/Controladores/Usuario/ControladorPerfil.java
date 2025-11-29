package interfaz.sara.Controladores.Usuario;

import interfaz.sara.ConexionBD.ConexionBD;
import interfaz.sara.Utilidades.SesionUsuario;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
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
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Controlador para la vista de perfil del usuario (VistaPerfil.fxml)
 * Gestiona la visualización y edición de los datos del usuario
 */
public class ControladorPerfil {

    // ========== Componentes FXML ==========
    
    @FXML
    private VBox navbarInclude;
    
    @FXML
    private ImageView profileImageView;
    
    @FXML
    private Label labelNombreUsuario;
    
    @FXML
    private TextField campoNombre;
    
    @FXML
    private TextField campoCorreo;
    
    @FXML
    private TextField campoMatricula;
    
    @FXML
    private PasswordField campoContrasena;
    
    @FXML
    private PasswordField campoConfirmarContrasena;

    // ========== Variables de estado ==========
    
    /** ID del usuario actual */
    private Long usuarioId;
    
    /** Ruta de la imagen de perfil actual */
    private String rutaImagenPerfil;
    
    /** Carpeta donde se guardan las fotos de perfil */
    private static final String CARPETA_FOTOS_PERFIL = "profile_pictures";

    // ========== Métodos de inicialización ==========
    
    /**
     * Inicializa el controlador después de que se carga el FXML
     * Carga los datos del usuario desde la base de datos
     */
    @FXML
    private void initialize() {
        // Obtener el usuario de la sesión
        SesionUsuario sesion = SesionUsuario.obtenerInstancia();
        if (!sesion.estaAutenticado()) {
            mostrarAlerta("Error", "Sesión no válida", 
                         "No hay un usuario autenticado. Por favor, inicie sesión nuevamente.", 
                         Alert.AlertType.ERROR);
            return;
        }
        
        usuarioId = sesion.getUsuarioId();
        
        // Cargar datos del usuario desde la base de datos
        cargarDatosUsuario();
        
        // Configurar el navbar
        Platform.runLater(() -> {
            configurarNavbar();
        });
    }
    
    /**
     * Configura el navbar para resaltar el botón de perfil como activo
     */
    private void configurarNavbar() {
        try {
            if (navbarInclude != null) {
                Object controller = navbarInclude.getProperties().get("fx:controller");
                if (controller == null) {
                    // Buscar el controlador del navbar incluido
                    Button btnPerfil = (Button) navbarInclude.lookup("#botonPerfil");
                    Button btnMisReservas = (Button) navbarInclude.lookup("#botonMisReservas");
                    Button btnNuevaReserva = (Button) navbarInclude.lookup("#botonNuevaReserva");
                    Button btnReporte = (Button) navbarInclude.lookup("#botonReporte");
                    
                    if (btnMisReservas != null && btnNuevaReserva != null && 
                        btnPerfil != null && btnReporte != null) {
                        // Resetear todos los botones
                        btnMisReservas.getStyleClass().remove("navbar-button-active");
                        btnNuevaReserva.getStyleClass().remove("navbar-button-active");
                        btnReporte.getStyleClass().remove("navbar-button-active");
                        btnPerfil.getStyleClass().remove("navbar-button-active");
                        
                        if (!btnMisReservas.getStyleClass().contains("navbar-button")) {
                            btnMisReservas.getStyleClass().add("navbar-button");
                        }
                        if (!btnNuevaReserva.getStyleClass().contains("navbar-button")) {
                            btnNuevaReserva.getStyleClass().add("navbar-button");
                        }
                        if (!btnReporte.getStyleClass().contains("navbar-button")) {
                            btnReporte.getStyleClass().add("navbar-button");
                        }
                        
                        // Activar el botón de Perfil
                        btnPerfil.getStyleClass().remove("navbar-button-profile");
                        if (!btnPerfil.getStyleClass().contains("navbar-button-profile")) {
                            btnPerfil.getStyleClass().add("navbar-button-profile");
                        }
                        btnPerfil.getStyleClass().add("active");
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error al configurar navbar: " + e.getMessage());
        }
    }
    
    /**
     * Carga los datos del usuario desde la base de datos
     */
    private void cargarDatosUsuario() {
        ConexionBD conexionBD = ConexionBD.obtenerInstancia();
        
        String sql = "SELECT username, email, matricula, profile_picture_path " +
                    "FROM users WHERE id = ?";
        
        try {
            Connection conexion = conexionBD.obtenerConexion();
            try (PreparedStatement statement = conexion.prepareStatement(sql)) {
            statement.setLong(1, usuarioId);
            
            try (ResultSet resultado = statement.executeQuery()) {
                if (resultado.next()) {
                    // Cargar datos en los campos
                    String nombre = resultado.getString("username");
                    String correo = resultado.getString("email");
                    String matricula = resultado.getString("matricula");
                    String rutaImagen = resultado.getString("profile_picture_path");
                    
                    campoNombre.setText(nombre != null ? nombre : "");
                    campoCorreo.setText(correo != null ? correo : "");
                    campoMatricula.setText(matricula != null ? matricula : "");
                    labelNombreUsuario.setText(nombre != null ? nombre : "Usuario");
                    
                    // Cargar imagen de perfil si existe
                    rutaImagenPerfil = rutaImagen;
                    cargarImagenPerfil(rutaImagen);
                } else {
                    mostrarAlerta("Error", "Usuario no encontrado", 
                                 "No se encontraron los datos del usuario en la base de datos.", 
                                 Alert.AlertType.ERROR);
                }
            }
            }
        } catch (SQLException e) {
            System.err.println("Error al cargar datos del usuario: " + e.getMessage());
            mostrarAlerta("Error", "Error al cargar datos", 
                         "No se pudieron cargar los datos del usuario. Por favor, intente más tarde.", 
                         Alert.AlertType.ERROR);
        }
    }
    
    /**
     * Carga la imagen de perfil desde la ruta especificada
     * 
     * @param rutaImagen Ruta de la imagen (puede ser null o vacía)
     */
    private void cargarImagenPerfil(String rutaImagen) {
        try {
            if (rutaImagen != null && !rutaImagen.trim().isEmpty()) {
                // Intentar cargar desde la ruta del archivo
                File archivoImagen = new File(rutaImagen);
                if (archivoImagen.exists()) {
                    Image imagen = new Image(new FileInputStream(archivoImagen));
                    profileImageView.setImage(imagen);
                    return;
                }
            }
            
            // Si no hay imagen o no se encontró, cargar imagen por defecto
            cargarImagenPorDefecto();
        } catch (FileNotFoundException e) {
            System.err.println("No se encontró la imagen de perfil: " + rutaImagen);
            cargarImagenPorDefecto();
        } catch (Exception e) {
            System.err.println("Error al cargar imagen de perfil: " + e.getMessage());
            cargarImagenPorDefecto();
        }
    }
    
    /**
     * Carga una imagen de perfil por defecto
     */
    private void cargarImagenPorDefecto() {
        try {
            // Intentar cargar desde recursos
            InputStream imagenStream = getClass().getResourceAsStream(
                "/interfaz/sara/imagenes/perfil_placeholder.png"
            );
            if (imagenStream != null) {
                Image imagen = new Image(imagenStream);
                profileImageView.setImage(imagen);
            } else {
                // Si no hay imagen en recursos, usar un círculo gris (se puede implementar después)
                profileImageView.setImage(null);
            }
        } catch (Exception e) {
            System.err.println("Error al cargar imagen por defecto: " + e.getMessage());
        }
    }

    // ========== Métodos de manejo de eventos ==========
    
    /**
     * Maneja el clic en el botón "Cambiar foto"
     * Abre un selector de archivos para elegir una nueva imagen de perfil
     */
    @FXML
    private void manejarCambiarFoto() {
        // Obtener el Stage actual
        Stage stage = (Stage) profileImageView.getScene().getWindow();
        
        // Configurar el selector de archivos
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar Foto de Perfil");
        
        // Filtrar solo imágenes
        FileChooser.ExtensionFilter filtroImagenes = new FileChooser.ExtensionFilter(
            "Archivos de Imagen", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp"
        );
        fileChooser.getExtensionFilters().add(filtroImagenes);
        
        // Mostrar el diálogo de selección
        File archivoSeleccionado = fileChooser.showOpenDialog(stage);
        
        if (archivoSeleccionado != null) {
            try {
                // Copiar el archivo a la carpeta de fotos de perfil
                String nuevaRuta = copiarImagenPerfil(archivoSeleccionado);
                
                if (nuevaRuta != null) {
                    // Actualizar la base de datos con la nueva ruta
                    actualizarRutaImagenEnBD(nuevaRuta);
                    
                    // Actualizar la visualización en la pantalla de perfil
                    cargarImagenPerfil(nuevaRuta);
                    
                    // Actualizar la imagen en el navbar
                    actualizarImagenEnNavbar(nuevaRuta);
                    
                    // Guardar la nueva ruta
                    rutaImagenPerfil = nuevaRuta;
                    
                    mostrarAlerta("Éxito", "Foto actualizada", 
                                 "Tu foto de perfil ha sido actualizada correctamente.", 
                                 Alert.AlertType.INFORMATION);
                }
            } catch (Exception e) {
                System.err.println("Error al cambiar foto de perfil: " + e.getMessage());
                e.printStackTrace();
                mostrarAlerta("Error", "Error al cambiar foto", 
                             "No se pudo cambiar la foto de perfil. Por favor, intente nuevamente.", 
                             Alert.AlertType.ERROR);
            }
        }
    }
    
    /**
     * Copia la imagen seleccionada a la carpeta de fotos de perfil
     * Elimina la imagen anterior si existe
     * 
     * @param archivoOriginal Archivo de imagen seleccionado por el usuario
     * @return Ruta relativa donde se guardó la imagen, o null si hubo un error
     * @throws IOException Si ocurre un error al copiar el archivo
     */
    private String copiarImagenPerfil(File archivoOriginal) throws IOException {
        // Eliminar la imagen anterior si existe
        if (rutaImagenPerfil != null && !rutaImagenPerfil.trim().isEmpty()) {
            try {
                Path rutaAnterior = Paths.get(rutaImagenPerfil);
                if (Files.exists(rutaAnterior)) {
                    Files.delete(rutaAnterior);
                    System.out.println("Imagen anterior eliminada: " + rutaImagenPerfil);
                }
            } catch (Exception e) {
                System.err.println("No se pudo eliminar la imagen anterior: " + e.getMessage());
                // Continuar de todas formas
            }
        }
        
        // Crear la carpeta de fotos de perfil si no existe
        Path carpetaPerfiles = Paths.get(CARPETA_FOTOS_PERFIL);
        if (!Files.exists(carpetaPerfiles)) {
            Files.createDirectories(carpetaPerfiles);
        }
        
        // Generar un nombre único para el archivo
        // Formato: profile_{timestamp}_{hash}.{extension}
        String extension = obtenerExtension(archivoOriginal.getName());
        long timestamp = System.currentTimeMillis();
        String nombreArchivo = String.format("profile_%d_%s.%s", 
                                             timestamp, 
                                             generarHashAleatorio(),
                                             extension);
        
        // Ruta completa del nuevo archivo
        Path archivoDestino = carpetaPerfiles.resolve(nombreArchivo);
        
        // Copiar el archivo
        Files.copy(archivoOriginal.toPath(), archivoDestino, 
                  java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        
        // Devolver la ruta relativa (formato compatible con la BD)
        return CARPETA_FOTOS_PERFIL + "\\" + nombreArchivo;
    }
    
    /**
     * Obtiene la extensión de un archivo
     * 
     * @param nombreArchivo Nombre del archivo
     * @return Extensión sin el punto
     */
    private String obtenerExtension(String nombreArchivo) {
        int ultimoPunto = nombreArchivo.lastIndexOf('.');
        if (ultimoPunto > 0 && ultimoPunto < nombreArchivo.length() - 1) {
            return nombreArchivo.substring(ultimoPunto + 1).toLowerCase();
        }
        return "jpg"; // Extensión por defecto
    }
    
    /**
     * Genera un hash aleatorio corto para el nombre del archivo
     * 
     * @return String con 8 caracteres hexadecimales
     */
    private String generarHashAleatorio() {
        return Long.toHexString(System.nanoTime()).substring(0, 8);
    }
    
    /**
     * Actualiza la ruta de la imagen de perfil en la base de datos
     * 
     * @param nuevaRuta Nueva ruta de la imagen de perfil
     */
    private void actualizarRutaImagenEnBD(String nuevaRuta) {
        ConexionBD conexionBD = ConexionBD.obtenerInstancia();
        
        String sql = "UPDATE users SET profile_picture_path = ? WHERE id = ?";
        
        try {
            Connection conexion = conexionBD.obtenerConexion();
            try (PreparedStatement statement = conexion.prepareStatement(sql)) {
                statement.setString(1, nuevaRuta);
                statement.setLong(2, usuarioId);
                
                int filasAfectadas = statement.executeUpdate();
                
                if (filasAfectadas > 0) {
                    System.out.println("Ruta de imagen de perfil actualizada: " + nuevaRuta);
                } else {
                    System.err.println("No se pudo actualizar la ruta de la imagen de perfil");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al actualizar ruta de imagen en BD: " + e.getMessage());
            throw new RuntimeException("Error al actualizar la base de datos", e);
        }
    }
    
    /**
     * Maneja el clic en el botón "Guardar Cambios"
     * Valida y guarda los cambios en la base de datos
     */
    @FXML
    private void manejarGuardarCambios() {
        // Validar campos
        if (!validarCampos()) {
            return;
        }
        
        // Validar contraseñas si se proporcionaron
        String nuevaContrasena = campoContrasena.getText().trim();
        String confirmarContrasena = campoConfirmarContrasena.getText().trim();
        
        if (!nuevaContrasena.isEmpty() || !confirmarContrasena.isEmpty()) {
            if (!nuevaContrasena.equals(confirmarContrasena)) {
                mostrarAlerta("Error de validación", "Contraseñas no coinciden", 
                             "Las contraseñas ingresadas no coinciden. Por favor, verifique e intente nuevamente.", 
                             Alert.AlertType.ERROR);
                return;
            }
            
            if (nuevaContrasena.length() < 6) {
                mostrarAlerta("Error de validación", "Contraseña muy corta", 
                             "La contraseña debe tener al menos 6 caracteres.", 
                             Alert.AlertType.ERROR);
                return;
            }
        }
        
        // Guardar cambios en la base de datos
        guardarCambiosEnBD(nuevaContrasena);
    }
    
    /**
     * Valida que los campos requeridos estén completos
     * 
     * @return true si todos los campos son válidos, false en caso contrario
     */
    private boolean validarCampos() {
        String nombre = campoNombre.getText().trim();
        String correo = campoCorreo.getText().trim();
        String matricula = campoMatricula.getText().trim();
        
        if (nombre.isEmpty()) {
            mostrarAlerta("Error de validación", "Campo requerido", 
                         "El nombre es obligatorio. Por favor, ingrese su nombre completo.", 
                         Alert.AlertType.ERROR);
            campoNombre.requestFocus();
            return false;
        }
        
        if (correo.isEmpty()) {
            mostrarAlerta("Error de validación", "Campo requerido", 
                         "El correo electrónico es obligatorio. Por favor, ingrese su correo.", 
                         Alert.AlertType.ERROR);
            campoCorreo.requestFocus();
            return false;
        }
        
        // Validar formato de correo básico
        if (!correo.contains("@") || !correo.contains(".")) {
            mostrarAlerta("Error de validación", "Correo inválido", 
                         "Por favor, ingrese un correo electrónico válido.", 
                         Alert.AlertType.ERROR);
            campoCorreo.requestFocus();
            return false;
        }
        
        if (matricula.isEmpty()) {
            mostrarAlerta("Error de validación", "Campo requerido", 
                         "La matrícula es obligatoria. Por favor, ingrese su matrícula.", 
                         Alert.AlertType.ERROR);
            campoMatricula.requestFocus();
            return false;
        }
        
        return true;
    }
    
    /**
     * Guarda los cambios del usuario en la base de datos
     * 
     * @param nuevaContrasena Nueva contraseña (puede ser null o vacía si no se cambió)
     */
    private void guardarCambiosEnBD(String nuevaContrasena) {
        ConexionBD conexionBD = ConexionBD.obtenerInstancia();
        
        String nombre = campoNombre.getText().trim();
        String correo = campoCorreo.getText().trim();
        String matricula = campoMatricula.getText().trim();
        
        String sql;
        PreparedStatement statement = null;
        
        try {
            Connection conexion = conexionBD.obtenerConexion();
            if (nuevaContrasena != null && !nuevaContrasena.isEmpty()) {
                // Actualizar incluyendo la contraseña
                sql = "UPDATE users SET username = ?, email = ?, matricula = ?, " +
                      "password_hash = ? WHERE id = ?";
                statement = conexion.prepareStatement(sql);
                statement.setString(1, nombre);
                statement.setString(2, correo);
                statement.setString(3, matricula);
                
                // Hash de la contraseña (simple, se puede mejorar con bcrypt después)
                String hashContrasena = hashPassword(nuevaContrasena);
                statement.setString(4, hashContrasena);
                statement.setLong(5, usuarioId);
            } else {
                // Actualizar sin cambiar la contraseña
                sql = "UPDATE users SET username = ?, email = ?, matricula = ? WHERE id = ?";
                statement = conexion.prepareStatement(sql);
                statement.setString(1, nombre);
                statement.setString(2, correo);
                statement.setString(3, matricula);
                statement.setLong(4, usuarioId);
            }
            
            int filasAfectadas = statement.executeUpdate();
            
            if (filasAfectadas > 0) {
                // Actualizar la sesión con el nuevo nombre
                SesionUsuario sesion = SesionUsuario.obtenerInstancia();
                sesion.iniciarSesion(usuarioId, nombre, correo);
                
                // Actualizar el label del nombre
                labelNombreUsuario.setText(nombre);
                
                // Limpiar campos de contraseña
                campoContrasena.clear();
                campoConfirmarContrasena.clear();
                
                mostrarAlerta("Éxito", "Cambios guardados", 
                             "Sus datos han sido actualizados correctamente.", 
                             Alert.AlertType.INFORMATION);
            } else {
                mostrarAlerta("Error", "Error al guardar", 
                             "No se pudieron guardar los cambios. Por favor, intente nuevamente.", 
                             Alert.AlertType.ERROR);
            }
            
        } catch (SQLException e) {
            System.err.println("Error al guardar cambios: " + e.getMessage());
            
            // Verificar si es un error de duplicado (correo o matrícula)
            if (e.getMessage().contains("Duplicate entry")) {
                if (e.getMessage().contains("email")) {
                    mostrarAlerta("Error", "Correo ya registrado", 
                                 "El correo electrónico ingresado ya está registrado. Por favor, use otro correo.", 
                                 Alert.AlertType.ERROR);
                } else if (e.getMessage().contains("matricula")) {
                    mostrarAlerta("Error", "Matrícula ya registrada", 
                                 "La matrícula ingresada ya está registrada. Por favor, use otra matrícula.", 
                                 Alert.AlertType.ERROR);
                } else {
                    mostrarAlerta("Error", "Error al guardar", 
                                 "No se pudieron guardar los cambios debido a un error en la base de datos.", 
                                 Alert.AlertType.ERROR);
                }
            } else {
                mostrarAlerta("Error", "Error al guardar", 
                             "No se pudieron guardar los cambios. Por favor, intente más tarde.", 
                             Alert.AlertType.ERROR);
            }
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    System.err.println("Error al cerrar statement: " + e.getMessage());
                }
            }
        }
    }
    
    /**
     * Genera un hash simple de la contraseña
     * TODO: Mejorar con bcrypt o similar para mayor seguridad
     * 
     * @param password Contraseña en texto plano
     * @return Hash de la contraseña
     */
    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            System.err.println("Error al generar hash: " + e.getMessage());
            // En caso de error, devolver la contraseña sin hashear (no recomendado para producción)
            return password;
        }
    }
    
    /**
     * Actualiza la imagen de perfil en el navbar
     * 
     * @param nuevaRuta Ruta de la nueva imagen de perfil
     */
    private void actualizarImagenEnNavbar(String nuevaRuta) {
        try {
            // Buscar el controlador del navbar incluido
            if (navbarInclude != null) {
                // Buscar el ImageView del perfil en el navbar
                javafx.scene.image.ImageView navbarImageView = 
                    (javafx.scene.image.ImageView) navbarInclude.lookup("#perfilImageView");
                
                if (navbarImageView != null && nuevaRuta != null && !nuevaRuta.trim().isEmpty()) {
                    // Cargar y actualizar la imagen
                    File archivoImagen = new File(nuevaRuta);
                    if (archivoImagen.exists()) {
                        Image nuevaImagen = new Image(new FileInputStream(archivoImagen));
                        navbarImageView.setImage(nuevaImagen);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error al actualizar imagen en navbar: " + e.getMessage());
            // No es crítico, solo registramos el error
        }
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

