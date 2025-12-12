package interfaz.sara.ConexionBD;

/**
 * Clase de configuración para los parámetros de conexión a la base de datos
 * Contiene los valores predeterminados para conectarse a MySQL
 */
public class ConfiguracionBD {
    
    // ========== Parámetros de conexión ==========
    
    /** Nombre del servidor de base de datos (host) */
    private static final String HOST = "localhost";
    
    /** Puerto de la base de datos MySQL (por defecto 3306) */
    private static final int PUERTO = 3306;
    
    /** Nombre de la base de datos */
    private static final String NOMBRE_BD = "sara";
    
    /** Nombre de usuario para la conexión */
    private static final String USUARIO = "root";
    
    /** Contraseña para la conexión */
    private static final String CONTRASENA = "";
    
    /** Timeout para establecer la conexión (en milisegundos) */
    private static final int TIMEOUT_CONEXION = 5000;
    
    /** URL de conexión completa a la base de datos */
    private static final String URL_CONEXION = String.format(
        "jdbc:mysql://%s:%d/%s?useSSL=false&serverTimezone=America/Mexico_City&allowPublicKeyRetrieval=true&useLegacyDatetimeCode=false",
        HOST, PUERTO, NOMBRE_BD
    );
    
    // ========== Métodos de acceso ==========
    
    /**
     * Obtiene la URL de conexión completa a la base de datos
     * 
     * @return String con la URL de conexión JDBC
     */
    public static String getUrlConexion() {
        return URL_CONEXION;
    }
    
    /**
     * Obtiene el nombre de usuario para la conexión
     * 
     * @return String con el nombre de usuario
     */
    public static String getUsuario() {
        return USUARIO;
    }
    
    /**
     * Obtiene la contraseña para la conexión
     * 
     * @return String con la contraseña
     */
    public static String getContrasena() {
        return CONTRASENA;
    }
    
    /**
     * Obtiene el timeout para establecer la conexión
     * 
     * @return int con el timeout en milisegundos
     */
    public static int getTimeoutConexion() {
        return TIMEOUT_CONEXION;
    }
    
    /**
     * Obtiene el host del servidor de base de datos
     * 
     * @return String con el host
     */
    public static String getHost() {
        return HOST;
    }
    
    /**
     * Obtiene el puerto de la base de datos
     * 
     * @return int con el puerto
     */
    public static int getPuerto() {
        return PUERTO;
    }
    
    /**
     * Obtiene el nombre de la base de datos
     * 
     * @return String con el nombre de la base de datos
     */
    public static String getNombreBD() {
        return NOMBRE_BD;
    }
}

