package interfaz.sara.ConexionBD;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Clase singleton para gestionar la conexión a la base de datos MySQL
 * Proporciona métodos para obtener y cerrar conexiones de forma segura
 */
public class ConexionBD {
    
    // ========== Instancia singleton ==========
    
    /** Instancia única de la clase ConexionBD */
    private static ConexionBD instancia;
    
    /** Objeto Connection activo a la base de datos */
    private Connection conexion;
    
    // ========== Constructor privado (Singleton) ==========
    
    /**
     * Constructor privado para prevenir la creación de instancias externas
     * Inicializa el driver de MySQL
     */
    private ConexionBD() {
        try {
            // Cargar el driver de MySQL
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("Error: No se pudo cargar el driver de MySQL");
            System.err.println("Mensaje: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // ========== Método para obtener la instancia singleton ==========
    
    /**
     * Obtiene la instancia única de ConexionBD (patrón Singleton)
     * 
     * @return La instancia única de ConexionBD
     */
    public static ConexionBD obtenerInstancia() {
        if (instancia == null) {
            synchronized (ConexionBD.class) {
                if (instancia == null) {
                    instancia = new ConexionBD();
                }
            }
        }
        return instancia;
    }
    
    // ========== Métodos de conexión ==========
    
    /**
     * Obtiene una conexión a la base de datos
     * Si ya existe una conexión activa, la reutiliza
     * Si no existe o está cerrada, crea una nueva
     * 
     * @return Connection activa a la base de datos
     * @throws SQLException Si ocurre un error al establecer la conexión
     */
    public Connection obtenerConexion() throws SQLException {
        // Verificar si la conexión existe y está abierta
        if (conexion == null || conexion.isClosed()) {
            try {
                // Establecer nueva conexión usando los parámetros de configuración
                conexion = DriverManager.getConnection(
                    ConfiguracionBD.getUrlConexion(),
                    ConfiguracionBD.getUsuario(),
                    ConfiguracionBD.getContrasena()
                );
                
                // Configurar autocommit (true por defecto)
                conexion.setAutoCommit(true);
                
                System.out.println("Conexión a la base de datos establecida correctamente");
            } catch (SQLException e) {
                System.err.println("Error al establecer conexión con la base de datos");
                System.err.println("URL: " + ConfiguracionBD.getUrlConexion());
                System.err.println("Mensaje: " + e.getMessage());
                throw e;
            }
        }
        
        return conexion;
    }
    
    /**
     * Cierra la conexión actual a la base de datos
     * Si la conexión está abierta, la cierra de forma segura
     */
    public void cerrarConexion() {
        if (conexion != null) {
            try {
                if (!conexion.isClosed()) {
                    conexion.close();
                    System.out.println("Conexión a la base de datos cerrada correctamente");
                }
            } catch (SQLException e) {
                System.err.println("Error al cerrar la conexión con la base de datos");
                System.err.println("Mensaje: " + e.getMessage());
                e.printStackTrace();
            } finally {
                conexion = null;
            }
        }
    }
    
    /**
     * Verifica si la conexión a la base de datos está activa
     * 
     * @return true si la conexión está abierta y válida, false en caso contrario
     */
    public boolean estaConectado() {
        try {
            return conexion != null && !conexion.isClosed() && conexion.isValid(2);
        } catch (SQLException e) {
            return false;
        }
    }
    
    /**
     * Prueba la conexión a la base de datos
     * Útil para verificar que la configuración es correcta
     * 
     * @return true si la conexión es exitosa, false en caso contrario
     */
    public boolean probarConexion() {
        try {
            Connection conexionPrueba = obtenerConexion();
            boolean esValida = conexionPrueba.isValid(ConfiguracionBD.getTimeoutConexion() / 1000);
            return esValida;
        } catch (SQLException e) {
            System.err.println("Error al probar la conexión: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Inicia una transacción desactivando el autocommit
     * Útil para operaciones que requieren atomicidad
     * 
     * @throws SQLException Si ocurre un error al iniciar la transacción
     */
    public void iniciarTransaccion() throws SQLException {
        Connection conn = obtenerConexion();
        conn.setAutoCommit(false);
    }
    
    /**
     * Confirma una transacción (commit)
     * 
     * @throws SQLException Si ocurre un error al hacer commit
     */
    public void confirmarTransaccion() throws SQLException {
        if (conexion != null && !conexion.getAutoCommit()) {
            conexion.commit();
            conexion.setAutoCommit(true);
        }
    }
    
    /**
     * Revierte una transacción (rollback)
     * 
     * @throws SQLException Si ocurre un error al hacer rollback
     */
    public void revertirTransaccion() throws SQLException {
        if (conexion != null && !conexion.getAutoCommit()) {
            conexion.rollback();
            conexion.setAutoCommit(true);
        }
    }
}

