package interfaz.reservadesalas.database;

import interfaz.reservadesalas.Modelo.Reserva;
import interfaz.reservadesalas.Modelo.Sala;
import interfaz.reservadesalas.Modelo.Usuario;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object para la entidad Reserva
 * Maneja todas las operaciones de base de datos relacionadas con reservas
 */
public class ReservaDAO {
    private UsuarioDAO usuarioDAO;
    private SalaDAO salaDAO;
    
    public ReservaDAO() {
        this.usuarioDAO = new UsuarioDAO();
        this.salaDAO = new SalaDAO();
    }
    
    /**
     * Crea una nueva reserva
     */
    public boolean crearReserva(Reserva reserva) {
        String sql = "INSERT INTO reservations (room_id, user_id, start_at, end_at, status_id) VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            // Obtener IDs de usuario y sala
            int userId = obtenerUserIdPorEmail(reserva.getUsuario().getEmail());
            int roomId = Integer.parseInt(reserva.getSala().getId());
            
            // Combinar fecha y hora para start_at y end_at
            LocalDateTime startAt = LocalDateTime.of(reserva.getFecha(), reserva.getHoraInicio());
            LocalDateTime endAt = LocalDateTime.of(reserva.getFecha(), reserva.getHoraFin());
            
            stmt.setInt(1, roomId);
            stmt.setInt(2, userId);
            stmt.setTimestamp(3, Timestamp.valueOf(startAt));
            stmt.setTimestamp(4, Timestamp.valueOf(endAt));
            stmt.setInt(5, 2); // status_id = 2 (CONFIRMED)
            
            int filasAfectadas = stmt.executeUpdate();
            
            if (filasAfectadas > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    reserva.setId("RES-" + rs.getInt(1));
                    return true;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al crear reserva: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Actualiza una reserva existente
     */
    public boolean actualizarReserva(Reserva reserva) {
        String sql = "UPDATE reservations SET room_id = ?, start_at = ?, end_at = ? WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            int roomId = Integer.parseInt(reserva.getSala().getId());
            LocalDateTime startAt = LocalDateTime.of(reserva.getFecha(), reserva.getHoraInicio());
            LocalDateTime endAt = LocalDateTime.of(reserva.getFecha(), reserva.getHoraFin());
            
            // Extraer el ID numérico del string "RES-123"
            int reservaId = Integer.parseInt(reserva.getId().replace("RES-", ""));
            
            stmt.setInt(1, roomId);
            stmt.setTimestamp(2, Timestamp.valueOf(startAt));
            stmt.setTimestamp(3, Timestamp.valueOf(endAt));
            stmt.setInt(4, reservaId);
            
            int filasAfectadas = stmt.executeUpdate();
            return filasAfectadas > 0;
            
        } catch (SQLException e) {
            System.err.println("Error al actualizar reserva: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Cancela una reserva (cambia el estado a CANCELED)
     */
    public boolean cancelarReserva(String reservaId) {
        String sql = "UPDATE reservations SET status_id = ? WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            int id = Integer.parseInt(reservaId.replace("RES-", ""));
            stmt.setInt(1, 3); // status_id = 3 (CANCELED) - ajusta según tu esquema
            stmt.setInt(2, id);
            
            int filasAfectadas = stmt.executeUpdate();
            return filasAfectadas > 0;
            
        } catch (SQLException e) {
            System.err.println("Error al cancelar reserva: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Elimina una reserva
     */
    public boolean eliminarReserva(String reservaId) {
        String sql = "DELETE FROM reservations WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            int id = Integer.parseInt(reservaId.replace("RES-", ""));
            stmt.setInt(1, id);
            
            int filasAfectadas = stmt.executeUpdate();
            return filasAfectadas > 0;
            
        } catch (SQLException e) {
            System.err.println("Error al eliminar reserva: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Obtiene todas las reservas de un usuario
     */
    public List<Reserva> obtenerReservasPorUsuario(Usuario usuario) {
        List<Reserva> reservas = new ArrayList<>();
        String sql = "SELECT r.id, r.room_id, r.user_id, r.start_at, r.end_at, r.status_id " +
                     "FROM reservations r " +
                     "INNER JOIN users u ON r.user_id = u.id " +
                     "WHERE u.email = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, usuario.getEmail());
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                reservas.add(mapearResultSetAReserva(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener reservas por usuario: " + e.getMessage());
            e.printStackTrace();
        }
        return reservas;
    }
    
    /**
     * Obtiene reservas por sala y fecha
     */
    public List<Reserva> obtenerReservasPorSalaYFecha(Sala sala, LocalDate fecha) {
        List<Reserva> reservas = new ArrayList<>();
        String sql = "SELECT r.id, r.room_id, r.user_id, r.start_at, r.end_at, r.status_id " +
                     "FROM reservations r " +
                     "WHERE r.room_id = ? AND DATE(r.start_at) = ? AND r.status_id = 2";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            int roomId = Integer.parseInt(sala.getId());
            stmt.setInt(1, roomId);
            stmt.setDate(2, Date.valueOf(fecha));
            
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                reservas.add(mapearResultSetAReserva(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener reservas por sala y fecha: " + e.getMessage());
            e.printStackTrace();
        }
        return reservas;
    }
    
    /**
     * Verifica si hay conflicto de horarios
     */
    public boolean hayConflicto(Sala sala, LocalDate fecha, LocalTime horaInicio, LocalTime horaFin, String excluirId) {
        String sql = "SELECT COUNT(*) as count FROM reservations " +
                     "WHERE room_id = ? AND DATE(start_at) = ? AND status_id = 2 " +
                     "AND ((TIME(start_at) < ? AND TIME(end_at) > ?) OR " +
                     "(TIME(start_at) < ? AND TIME(end_at) > ?))";
        
        if (excluirId != null) {
            sql += " AND id != ?";
        }
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            int roomId = Integer.parseInt(sala.getId());
            stmt.setInt(1, roomId);
            stmt.setDate(2, Date.valueOf(fecha));
            stmt.setTime(3, Time.valueOf(horaFin));
            stmt.setTime(4, Time.valueOf(horaInicio));
            stmt.setTime(5, Time.valueOf(horaFin));
            stmt.setTime(6, Time.valueOf(horaInicio));
            
            if (excluirId != null) {
                int id = Integer.parseInt(excluirId.replace("RES-", ""));
                stmt.setInt(7, id);
            }
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("count") > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error al verificar conflicto: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Busca una reserva por su ID
     */
    public Optional<Reserva> buscarPorId(String reservaId) {
        String sql = "SELECT r.id, r.room_id, r.user_id, r.start_at, r.end_at, r.status_id " +
                     "FROM reservations r WHERE r.id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            int id = Integer.parseInt(reservaId.replace("RES-", ""));
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return Optional.of(mapearResultSetAReserva(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar reserva por ID: " + e.getMessage());
            e.printStackTrace();
        }
        return Optional.empty();
    }
    
    /**
     * Mapea un ResultSet a un objeto Reserva
     */
    private Reserva mapearResultSetAReserva(ResultSet rs) throws SQLException {
        Reserva reserva = new Reserva();
        
        int reservaId = rs.getInt("id");
        reserva.setId("RES-" + reservaId);
        
        // Obtener usuario
        int userId = rs.getInt("user_id");
        Optional<Usuario> usuarioOpt = usuarioDAO.buscarPorId(userId);
        usuarioOpt.ifPresent(reserva::setUsuario);
        
        // Obtener sala
        int roomId = rs.getInt("room_id");
        Optional<Sala> salaOpt = salaDAO.buscarPorId(String.valueOf(roomId));
        if (salaOpt.isPresent()) {
            reserva.setSala(salaOpt.get());
        } else {
            // Si no se encuentra por ID numérico, intentar buscar por room_code
            // Esto es un fallback en caso de que el ID no coincida
            System.err.println("Advertencia: No se encontró sala con ID: " + roomId);
        }
        
        // Obtener fechas y horas
        Timestamp startAt = rs.getTimestamp("start_at");
        Timestamp endAt = rs.getTimestamp("end_at");
        
        if (startAt != null) {
            LocalDateTime start = startAt.toLocalDateTime();
            reserva.setFecha(start.toLocalDate());
            reserva.setHoraInicio(start.toLocalTime());
        }
        
        if (endAt != null) {
            reserva.setHoraFin(endAt.toLocalDateTime().toLocalTime());
        }
        
        // Estado
        int statusId = rs.getInt("status_id");
        reserva.setEstado(obtenerEstadoPorId(statusId));
        
        return reserva;
    }
    
    /**
     * Obtiene el ID de usuario por email
     */
    private int obtenerUserIdPorEmail(String email) throws SQLException {
        String sql = "SELECT id FROM users WHERE email = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
        }
        throw new SQLException("Usuario no encontrado: " + email);
    }
    
    /**
     * Obtiene el estado por ID (ajusta según tu esquema de reservation_status)
     */
    private String obtenerEstadoPorId(int statusId) {
        // Mapeo básico - ajusta según tu tabla reservation_status
        return switch (statusId) {
            case 1 -> "Pendiente";
            case 2 -> "Confirmada";
            case 3 -> "Cancelada";
            case 4 -> "Completada";
            default -> "Desconocido";
        };
    }
}

