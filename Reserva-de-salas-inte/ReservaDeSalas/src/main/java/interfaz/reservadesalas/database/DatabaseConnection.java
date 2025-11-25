package interfaz.reservadesalas.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Clase utilitaria para manejar la conexión a la base de datos MySQL
 */
public class DatabaseConnection {
    private static Connection connection = null;
    
    /**
     * Obtiene una conexión a la base de datos
     * Si ya existe una conexión activa, la reutiliza
     * 
     * @return Connection objeto de conexión a la base de datos
     * @throws SQLException si ocurre un error al conectar
     */
    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                String url = DatabaseConfig.getConnectionUrl();
                String username = DatabaseConfig.getUsername();
                String password = DatabaseConfig.getPassword();
                
                connection = DriverManager.getConnection(url, username, password);
                System.out.println("✓ Conexión a la base de datos establecida exitosamente");
            } catch (SQLException e) {
                System.err.println("✗ Error al conectar a la base de datos: " + e.getMessage());
                throw e;
            }
        }
        return connection;
    }
    
    /**
     * Cierra la conexión a la base de datos
     */
    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                connection = null;
                System.out.println("✓ Conexión a la base de datos cerrada");
            } catch (SQLException e) {
                System.err.println("✗ Error al cerrar la conexión: " + e.getMessage());
            }
        }
    }
    
    /**
     * Verifica si la conexión está activa
     * 
     * @return true si la conexión está activa, false en caso contrario
     */
    public static boolean isConnected() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }
    
    /**
     * Prueba la conexión a la base de datos
     * 
     * @return true si la conexión es exitosa, false en caso contrario
     */
    public static boolean testConnection() {
        try {
            Connection conn = getConnection();
            boolean isValid = conn.isValid(5); // Timeout de 5 segundos
            if (isValid) {
                System.out.println("✓ La conexión a la base de datos es válida");
            } else {
                System.out.println("✗ La conexión a la base de datos no es válida");
            }
            return isValid;
        } catch (SQLException e) {
            System.err.println("✗ Error al probar la conexión: " + e.getMessage());
            return false;
        }
    }
}

