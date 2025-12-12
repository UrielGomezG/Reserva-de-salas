package interfaz.sara.Controladores;

import interfaz.sara.ConexionBD.ConexionBD;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;
import java.util.Random;

/**
 * Servicio para el envío de códigos de recuperación de contraseña por correo electrónico
 */
public class EmailRecoveryService {

    private final String senderEmail = "";
    private final String senderPassword = "";

    // Instancia singleton
    private static EmailRecoveryService instancia;

    /**
     * Obtiene la instancia única del servicio (Singleton)
     */
    public static EmailRecoveryService obtenerInstancia() {
        if (instancia == null) {
            instancia = new EmailRecoveryService();
        }
        return instancia;
    }

    /**
     * Verifica si el correo existe en la base de datos y envía el código de recuperación
     * 
     * @param correoDestino El correo electrónico del usuario
     * @return El código de recuperación generado, o null si el correo no existe
     * @throws MessagingException Si hay un error al enviar el correo
     * @throws SQLException Si hay un error al consultar la base de datos
     */
    public String enviarCodigoRecuperacion(String correoDestino) throws MessagingException, SQLException {
        // Verificar que el correo existe en la BD
        if (!existeCorreoEnBD(correoDestino)) {
            return null; // El correo no existe
        }

        String codigo = generarCodigo();
        
        // Guardar el código en la BD con timestamp
        guardarCodigoEnBD(correoDestino, codigo);

        // Configurar propiedades SMTP
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new jakarta.mail.Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(senderEmail, senderPassword);
            }
        });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(senderEmail));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(correoDestino));
        message.setSubject("Código de Recuperación de Contraseña - SARA");
        message.setText("Hola,\n\n" +
                "Has solicitado recuperar tu contraseña en el sistema SARA.\n\n" +
                "Tu código de recuperación es: " + codigo + "\n\n" +
                "Este código es válido por 15 minutos.\n\n" +
                "Si no solicitaste este código, ignora este mensaje.\n\n" +
                "Saludos,\nEquipo SARA");

        Transport.send(message);

        return codigo;
    }

    /**
     * Verifica si un correo existe en la base de datos
     * 
     * @param correo El correo electrónico a verificar
     * @return true si el correo existe, false en caso contrario
     * @throws SQLException Si hay un error al consultar la base de datos
     */
    private boolean existeCorreoEnBD(String correo) throws SQLException {
        ConexionBD conexionBD = ConexionBD.obtenerInstancia();
        Connection conexion = conexionBD.obtenerConexion();
        
        String sql = "SELECT COUNT(*) as count FROM users WHERE email = ? AND is_active = 1";
        
        try (PreparedStatement statement = conexion.prepareStatement(sql)) {
            statement.setString(1, correo);
            
            try (ResultSet resultado = statement.executeQuery()) {
                if (resultado.next()) {
                    return resultado.getInt("count") > 0;
                }
            }
        }
        
        return false;
    }

    /**
     * Guarda el código de recuperación en la base de datos
     * Crea o actualiza el registro en la tabla password_reset_codes
     * 
     * @param correo El correo del usuario
     * @param codigo El código de recuperación
     * @throws SQLException Si hay un error al guardar en la base de datos
     */
    private void guardarCodigoEnBD(String correo, String codigo) throws SQLException {
        ConexionBD conexionBD = ConexionBD.obtenerInstancia();
        Connection conexion = conexionBD.obtenerConexion();
        
        // Obtener el ID del usuario
        Long userId = obtenerUserIdPorCorreo(correo);
        if (userId == null) {
            throw new SQLException("Usuario no encontrado");
        }
        
        // Crear tabla si no existe
        crearTablaSiNoExiste(conexion);
        
        // Eliminar códigos anteriores del usuario
        String sqlDelete = "DELETE FROM password_reset_codes WHERE user_id = ?";
        try (PreparedStatement stmt = conexion.prepareStatement(sqlDelete)) {
            stmt.setLong(1, userId);
            stmt.executeUpdate();
        }
        
        // Insertar nuevo código
        String sqlInsert = "INSERT INTO password_reset_codes (user_id, code, created_at, expires_at) VALUES (?, ?, NOW(), DATE_ADD(NOW(), INTERVAL 15 MINUTE))";
        try (PreparedStatement stmt = conexion.prepareStatement(sqlInsert)) {
            stmt.setLong(1, userId);
            stmt.setString(2, codigo);
            stmt.executeUpdate();
        }
    }

    /**
     * Obtiene el ID del usuario por su correo electrónico
     * 
     * @param correo El correo electrónico
     * @return El ID del usuario o null si no existe
     * @throws SQLException Si hay un error al consultar la base de datos
     */
    private Long obtenerUserIdPorCorreo(String correo) throws SQLException {
        ConexionBD conexionBD = ConexionBD.obtenerInstancia();
        Connection conexion = conexionBD.obtenerConexion();
        
        String sql = "SELECT id FROM users WHERE email = ? AND is_active = 1";
        
        try (PreparedStatement statement = conexion.prepareStatement(sql)) {
            statement.setString(1, correo);
            
            try (ResultSet resultado = statement.executeQuery()) {
                if (resultado.next()) {
                    return resultado.getLong("id");
                }
            }
        }
        
        return null;
    }

    /**
     * Crea la tabla password_reset_codes si no existe
     * Adaptado para MariaDB 10.4.32 con la estructura de la BD existente
     * Usa DATETIME para expires_at para compatibilidad
     * 
     * @param conexion La conexión a la base de datos
     * @throws SQLException Si hay un error al crear la tabla
     */
    private void crearTablaSiNoExiste(Connection conexion) throws SQLException {
        // Verificar si la tabla existe
        boolean tablaExiste = false;
        try (java.sql.ResultSet rs = conexion.getMetaData().getTables(null, null, "password_reset_codes", null)) {
            tablaExiste = rs.next();
        }
        
        if (!tablaExiste) {
            // Crear la tabla sin restricciones primero
            String sqlCreate = "CREATE TABLE password_reset_codes (" +
                        "id bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT, " +
                        "user_id bigint(20) UNSIGNED NOT NULL, " +
                        "code varchar(10) NOT NULL, " +
                        "created_at timestamp NOT NULL DEFAULT current_timestamp(), " +
                        "expires_at datetime NOT NULL, " +
                        "used tinyint(1) NOT NULL DEFAULT 0, " +
                        "PRIMARY KEY (id), " +
                        "KEY fk_password_reset_user (user_id), " +
                        "KEY idx_user_code (user_id, code), " +
                        "KEY idx_expires (expires_at)" +
                        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";
            
            try (PreparedStatement stmt = conexion.prepareStatement(sqlCreate)) {
                stmt.executeUpdate();
            }
            
            // Agregar la clave foránea
            try {
                String sqlFK = "ALTER TABLE password_reset_codes " +
                              "ADD CONSTRAINT fk_password_reset_user " +
                              "FOREIGN KEY (user_id) REFERENCES users(id) " +
                              "ON DELETE CASCADE ON UPDATE CASCADE";
                
                try (PreparedStatement stmt = conexion.prepareStatement(sqlFK)) {
                    stmt.executeUpdate();
                }
            } catch (SQLException e) {
                // La clave foránea ya existe, ignorar el error
                if (!e.getMessage().contains("Duplicate foreign key") && 
                    !e.getMessage().contains("already exists") &&
                    !e.getMessage().contains("Duplicate key name")) {
                    throw e;
                }
            }
        }
        // Si la tabla ya existe, no hacemos nada (asumimos que está correctamente configurada)
    }

    /**
     * Valida si un código de recuperación es válido para un correo
     * 
     * @param correo El correo del usuario
     * @param codigo El código a validar
     * @return true si el código es válido, false en caso contrario
     * @throws SQLException Si hay un error al consultar la base de datos
     */
    public boolean validarCodigo(String correo, String codigo) throws SQLException {
        ConexionBD conexionBD = ConexionBD.obtenerInstancia();
        Connection conexion = conexionBD.obtenerConexion();
        
        Long userId = obtenerUserIdPorCorreo(correo);
        if (userId == null) {
            return false;
        }
        
        String sql = "SELECT id FROM password_reset_codes " +
                    "WHERE user_id = ? AND code = ? AND used = FALSE AND expires_at > NOW()";
        
        try (PreparedStatement statement = conexion.prepareStatement(sql)) {
            statement.setLong(1, userId);
            statement.setString(2, codigo);
            
            try (ResultSet resultado = statement.executeQuery()) {
                return resultado.next();
            }
        }
    }

    /**
     * Marca un código como usado
     * 
     * @param correo El correo del usuario
     * @param codigo El código usado
     * @throws SQLException Si hay un error al actualizar la base de datos
     */
    public void marcarCodigoComoUsado(String correo, String codigo) throws SQLException {
        ConexionBD conexionBD = ConexionBD.obtenerInstancia();
        Connection conexion = conexionBD.obtenerConexion();
        
        Long userId = obtenerUserIdPorCorreo(correo);
        if (userId == null) {
            return;
        }
        
        String sql = "UPDATE password_reset_codes SET used = TRUE WHERE user_id = ? AND code = ?";
        
        try (PreparedStatement statement = conexion.prepareStatement(sql)) {
            statement.setLong(1, userId);
            statement.setString(2, codigo);
            statement.executeUpdate();
        }
    }

    /**
     * Obtiene el ID del usuario por correo (método público)
     * 
     * @param correo El correo electrónico
     * @return El ID del usuario o null si no existe
     * @throws SQLException Si hay un error al consultar la base de datos
     */
    public Long obtenerUserId(String correo) throws SQLException {
        return obtenerUserIdPorCorreo(correo);
    }

    /**
     * Genera un código numérico de 6 dígitos
     * 
     * @return El código generado
     */
    private String generarCodigo() {
        Random random = new Random();
        int numero = 100000 + random.nextInt(900000);
        return String.valueOf(numero);
    }
}
