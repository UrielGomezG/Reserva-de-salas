package interfaz.sara.Utilidades;

/**
 * Clase singleton para gestionar la sesión del usuario actual
 * Almacena información del usuario autenticado
 */
public class SesionUsuario {
    
    /** Instancia única del singleton */
    private static SesionUsuario instancia;
    
    /** ID del usuario autenticado */
    private Long usuarioId;
    
    /** Nombre de usuario autenticado */
    private String username;
    
    /** Email del usuario autenticado */
    private String email;
    
    /** Indica si el usuario es administrador */
    private boolean esAdmin;
    
    // ========== Constructor privado (Singleton) ==========
    
    private SesionUsuario() {
    }
    
    // ========== Método para obtener la instancia singleton ==========
    
    /**
     * Obtiene la instancia única de SesionUsuario
     * 
     * @return La instancia única de SesionUsuario
     */
    public static SesionUsuario obtenerInstancia() {
        if (instancia == null) {
            synchronized (SesionUsuario.class) {
                if (instancia == null) {
                    instancia = new SesionUsuario();
                }
            }
        }
        return instancia;
    }
    
    // ========== Métodos de sesión ==========
    
    /**
     * Establece la información del usuario autenticado
     * 
     * @param usuarioId El ID del usuario
     * @param username El nombre de usuario
     * @param email El email del usuario
     * @param esAdmin true si el usuario es administrador, false en caso contrario
     */
    public void iniciarSesion(Long usuarioId, String username, String email, boolean esAdmin) {
        this.usuarioId = usuarioId;
        this.username = username;
        this.email = email;
        this.esAdmin = esAdmin;
    }
    
    /**
     * Establece la información del usuario autenticado (sin rol, por compatibilidad)
     * 
     * @param usuarioId El ID del usuario
     * @param username El nombre de usuario
     * @param email El email del usuario
     */
    public void iniciarSesion(Long usuarioId, String username, String email) {
        iniciarSesion(usuarioId, username, email, false);
    }
    
    /**
     * Cierra la sesión del usuario actual
     */
    public void cerrarSesion() {
        this.usuarioId = null;
        this.username = null;
        this.email = null;
        this.esAdmin = false;
    }
    
    /**
     * Verifica si hay un usuario autenticado
     * 
     * @return true si hay un usuario autenticado, false en caso contrario
     */
    public boolean estaAutenticado() {
        return usuarioId != null;
    }
    
    // ========== Getters ==========
    
    public Long getUsuarioId() {
        return usuarioId;
    }
    
    public String getUsername() {
        return username;
    }
    
    public String getEmail() {
        return email;
    }
    
    /**
     * Verifica si el usuario actual es administrador
     * 
     * @return true si es administrador, false en caso contrario
     */
    public boolean esAdmin() {
        return esAdmin;
    }
}

