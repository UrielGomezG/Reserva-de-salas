package interfaz.reservadesalas.database;

import interfaz.reservadesalas.Modelo.Usuario;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object para la entidad Usuario
 * Maneja todas las operaciones de base de datos relacionadas con usuarios
 */
public class UsuarioDAO {
    
    /**
     * Busca un usuario por su email
     */
    public Optional<Usuario> buscarPorEmail(String email) {
        String sql = "SELECT id, username, email, matricula, password_hash FROM users WHERE email = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                Usuario usuario = mapearResultSetAUsuario(rs);
                return Optional.of(usuario);
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar usuario por email: " + e.getMessage());
            e.printStackTrace();
        }
        return Optional.empty();
    }
    
    /**
     * Busca un usuario por su matrícula
     */
    public Optional<Usuario> buscarPorMatricula(String matricula) {
        String sql = "SELECT id, username, email, matricula, password_hash FROM users WHERE matricula = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, matricula);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                Usuario usuario = mapearResultSetAUsuario(rs);
                return Optional.of(usuario);
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar usuario por matrícula: " + e.getMessage());
            e.printStackTrace();
        }
        return Optional.empty();
    }
    
    /**
     * Busca un usuario por su ID
     */
    public Optional<Usuario> buscarPorId(int id) {
        String sql = "SELECT id, username, email, matricula, password_hash FROM users WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                Usuario usuario = mapearResultSetAUsuario(rs);
                return Optional.of(usuario);
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar usuario por ID: " + e.getMessage());
            e.printStackTrace();
        }
        return Optional.empty();
    }
    
    /**
     * Crea un nuevo usuario en la base de datos
     */
    public boolean crearUsuario(Usuario usuario) {
        // Intentar primero con role_id, si falla, intentar sin role_id
        String sqlConRole = "INSERT INTO users (username, email, matricula, password_hash, role_id) VALUES (?, ?, ?, ?, ?)";
        String sqlSinRole = "INSERT INTO users (username, email, matricula, password_hash) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Intentar primero con role_id
            try (PreparedStatement stmt = conn.prepareStatement(sqlConRole, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, usuario.getNombre());
                stmt.setString(2, usuario.getEmail());
                stmt.setString(3, usuario.getMatricula());
                stmt.setString(4, usuario.getContrasena()); // En producción, debería ser un hash
                stmt.setInt(5, 2); // Por defecto, role_id = 2 (USER)
                
                int filasAfectadas = stmt.executeUpdate();
                if (filasAfectadas > 0) {
                    ResultSet rs = stmt.getGeneratedKeys();
                    if (rs.next()) {
                        return true;
                    }
                }
            } catch (SQLException e) {
                // Si falla porque no existe role_id, intentar sin role_id
                if (e.getMessage().contains("role_id")) {
                    try (PreparedStatement stmt = conn.prepareStatement(sqlSinRole, Statement.RETURN_GENERATED_KEYS)) {
                        stmt.setString(1, usuario.getNombre());
                        stmt.setString(2, usuario.getEmail());
                        stmt.setString(3, usuario.getMatricula());
                        stmt.setString(4, usuario.getContrasena());
                        
                        int filasAfectadas = stmt.executeUpdate();
                        if (filasAfectadas > 0) {
                            ResultSet rs = stmt.getGeneratedKeys();
                            if (rs.next()) {
                                return true;
                            }
                        }
                    }
                } else {
                    throw e; // Re-lanzar si es otro error
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al crear usuario: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Actualiza un usuario existente
     */
    public boolean actualizarUsuario(Usuario usuario) {
        String sql = "UPDATE users SET username = ?, matricula = ?, password_hash = ? WHERE email = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, usuario.getNombre());
            stmt.setString(2, usuario.getMatricula());
            stmt.setString(3, usuario.getContrasena());
            stmt.setString(4, usuario.getEmail());
            
            int filasAfectadas = stmt.executeUpdate();
            return filasAfectadas > 0;
            
        } catch (SQLException e) {
            System.err.println("Error al actualizar usuario: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Obtiene todos los usuarios
     */
    public List<Usuario> obtenerTodos() {
        List<Usuario> usuarios = new ArrayList<>();
        String sql = "SELECT id, username, email, matricula, password_hash FROM users";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                usuarios.add(mapearResultSetAUsuario(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener todos los usuarios: " + e.getMessage());
            e.printStackTrace();
        }
        return usuarios;
    }
    
    /**
     * Mapea un ResultSet a un objeto Usuario
     */
    private Usuario mapearResultSetAUsuario(ResultSet rs) throws SQLException {
        Usuario usuario = new Usuario();
        usuario.setNombre(rs.getString("username"));
        usuario.setEmail(rs.getString("email"));
        usuario.setMatricula(rs.getString("matricula"));
        usuario.setContrasena(rs.getString("password_hash"));
        return usuario;
    }
}

