package interfaz.reservadesalas.database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Script de prueba para verificar la conexión a la base de datos MySQL
 * Ejecuta un SELECT * FROM roles para verificar que la conexión funciona
 */
public class DatabaseTest {
    
    public static void main(String[] args) {
        System.out.println("==========================================");
        System.out.println("  PRUEBA DE CONEXIÓN A BASE DE DATOS");
        System.out.println("==========================================");
        System.out.println();
        
        // Mostrar configuración
        System.out.println("Configuración de conexión:");
        System.out.println("  Host: " + DatabaseConfig.getConnectionUrl());
        System.out.println("  Usuario: " + DatabaseConfig.getUsername());
        System.out.println("  Base de datos: sara");
        System.out.println();
        
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;
        
        try {
            // 1. Intentar conectar
            System.out.println("1. Intentando conectar a la base de datos...");
            connection = DatabaseConnection.getConnection();
            
            // 2. Verificar que la conexión es válida
            System.out.println("2. Verificando validez de la conexión...");
            if (!connection.isValid(5)) {
                System.err.println("✗ La conexión no es válida");
                return;
            }
            System.out.println("✓ Conexión válida");
            System.out.println();
            
            // 3. Ejecutar consulta de prueba
            System.out.println("3. Ejecutando consulta: SELECT * FROM roles");
            statement = connection.createStatement();
            resultSet = statement.executeQuery("SELECT * FROM roles");
            
            // 4. Mostrar resultados
            System.out.println("4. Resultados de la consulta:");
            System.out.println("   ----------------------------------------");
            
            int rowCount = 0;
            while (resultSet.next()) {
                rowCount++;
                int id = resultSet.getInt("id");
                String name = resultSet.getString("name");
                String description = resultSet.getString("description");
                
                System.out.println(String.format("   Fila %d: ID=%d, Name=%s, Description=%s", 
                    rowCount, id, name, description));
            }
            
            System.out.println("   ----------------------------------------");
            System.out.println("   Total de filas: " + rowCount);
            System.out.println();
            
            // 5. Éxito
            System.out.println("==========================================");
            System.out.println("  ✓ PRUEBA EXITOSA");
            System.out.println("==========================================");
            System.out.println("La conexión a la base de datos funciona correctamente.");
            
        } catch (SQLException e) {
            System.err.println();
            System.err.println("==========================================");
            System.err.println("  ✗ ERROR DE CONEXIÓN");
            System.err.println("==========================================");
            System.err.println("Error SQL: " + e.getMessage());
            System.err.println("Código SQL: " + e.getSQLState());
            System.err.println();
            System.err.println("Posibles causas:");
            System.err.println("  1. MySQL no está ejecutándose en XAMPP");
            System.err.println("  2. La base de datos 'sara' no existe");
            System.err.println("  3. El usuario o contraseña son incorrectos");
            System.err.println("  4. El puerto 3306 está ocupado (prueba con 3307)");
            System.err.println("  5. La tabla 'roles' no existe en la base de datos");
            System.err.println();
            System.err.println("Solución:");
            System.err.println("  - Verifica que XAMPP esté ejecutándose");
            System.err.println("  - Verifica que MySQL esté activo en XAMPP");
            System.err.println("  - Verifica que la base de datos 'sara' exista");
            System.err.println("  - Verifica que la tabla 'roles' exista");
            System.err.println("  - Si el puerto es diferente, usa DatabaseConfig.setPort(\"3307\")");
            
        } catch (Exception e) {
            System.err.println();
            System.err.println("==========================================");
            System.err.println("  ✗ ERROR INESPERADO");
            System.err.println("==========================================");
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            
        } finally {
            // Cerrar recursos
            try {
                if (resultSet != null) resultSet.close();
                if (statement != null) statement.close();
                if (connection != null) DatabaseConnection.closeConnection();
            } catch (SQLException e) {
                System.err.println("Error al cerrar recursos: " + e.getMessage());
            }
        }
    }
}

