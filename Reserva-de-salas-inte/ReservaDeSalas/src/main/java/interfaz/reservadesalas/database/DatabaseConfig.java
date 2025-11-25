package interfaz.reservadesalas.database;

/**
 * Configuración de la base de datos MySQL
 * Ajusta estos valores según tu configuración de XAMPP
 */
public class DatabaseConfig {
    // Configuración por defecto para XAMPP
    private static final String DEFAULT_HOST = "localhost";
    private static final String DEFAULT_PORT = "3306";
    private static final String DEFAULT_DATABASE = "sara";
    private static final String DEFAULT_USERNAME = "root";
    private static final String DEFAULT_PASSWORD = "";
    
    // Puedes cambiar estos valores si tu configuración es diferente
    private static String host = DEFAULT_HOST;
    private static String port = DEFAULT_PORT;
    private static String database = DEFAULT_DATABASE;
    private static String username = DEFAULT_USERNAME;
    private static String password = DEFAULT_PASSWORD;
    
    /**
     * Obtiene la URL de conexión JDBC
     */
    public static String getConnectionUrl() {
        return String.format("jdbc:mysql://%s:%s/%s?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true", 
                host, port, database);
    }
    
    /**
     * Obtiene el nombre de usuario
     */
    public static String getUsername() {
        return username;
    }
    
    /**
     * Obtiene la contraseña
     */
    public static String getPassword() {
        return password;
    }
    
    // Métodos para cambiar la configuración si es necesario
    public static void setHost(String newHost) {
        host = newHost;
    }
    
    public static void setPort(String newPort) {
        port = newPort;
    }
    
    public static void setDatabase(String newDatabase) {
        database = newDatabase;
    }
    
    public static void setUsername(String newUsername) {
        username = newUsername;
    }
    
    public static void setPassword(String newPassword) {
        password = newPassword;
    }
    
    /**
     * Restablece la configuración a los valores por defecto
     */
    public static void resetToDefaults() {
        host = DEFAULT_HOST;
        port = DEFAULT_PORT;
        database = DEFAULT_DATABASE;
        username = DEFAULT_USERNAME;
        password = DEFAULT_PASSWORD;
    }
}

