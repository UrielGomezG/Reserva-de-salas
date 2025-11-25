package interfaz.reservadesalas.database;

import interfaz.reservadesalas.Modelo.Sala;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object para la entidad Sala
 * Maneja todas las operaciones de base de datos relacionadas con salas
 */
public class SalaDAO {
    
    /**
     * Obtiene todas las salas
     */
    public List<Sala> obtenerTodas() {
        List<Sala> salas = new ArrayList<>();
        String sql = "SELECT id, room_code, capacity, room_type, location FROM rooms";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                salas.add(mapearResultSetASala(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener todas las salas: " + e.getMessage());
            e.printStackTrace();
        }
        return salas;
    }
    
    /**
     * Busca una sala por su ID
     */
    public Optional<Sala> buscarPorId(String id) {
        String sql = "SELECT id, room_code, capacity, room_type, location FROM rooms WHERE id = ? OR room_code = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, id);
            stmt.setString(2, id);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                Sala sala = mapearResultSetASala(rs);
                return Optional.of(sala);
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar sala por ID: " + e.getMessage());
            e.printStackTrace();
        }
        return Optional.empty();
    }
    
    /**
     * Busca salas por tipo
     */
    public List<Sala> buscarPorTipo(String tipo) {
        List<Sala> salas = new ArrayList<>();
        String sql = "SELECT id, room_code, capacity, room_type, location FROM rooms WHERE room_type = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, tipo);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                salas.add(mapearResultSetASala(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar salas por tipo: " + e.getMessage());
            e.printStackTrace();
        }
        return salas;
    }
    
    /**
     * Busca salas por nombre (room_code)
     */
    public List<Sala> buscarPorNombre(String nombre) {
        List<Sala> salas = new ArrayList<>();
        String sql = "SELECT id, room_code, capacity, room_type, location FROM rooms WHERE room_code LIKE ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, "%" + nombre + "%");
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                salas.add(mapearResultSetASala(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar salas por nombre: " + e.getMessage());
            e.printStackTrace();
        }
        return salas;
    }
    
    /**
     * Mapea un ResultSet a un objeto Sala
     */
    private Sala mapearResultSetASala(ResultSet rs) throws SQLException {
        Sala sala = new Sala();
        sala.setId(String.valueOf(rs.getInt("id")));
        sala.setNombre(rs.getString("room_code"));
        sala.setCapacidad(rs.getInt("capacity"));
        sala.setTipo(rs.getString("room_type"));
        sala.setUbicacion(rs.getString("location"));
        sala.setDisponible(true); // Por defecto, asumimos que está disponible
        return sala;
    }
}

