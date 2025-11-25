package interfaz.reservadesalas.Servicio;

import interfaz.reservadesalas.Modelo.Reserva;
import interfaz.reservadesalas.Modelo.Sala;
import interfaz.reservadesalas.Modelo.Usuario;
import interfaz.reservadesalas.database.ReservaDAO;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ReservaService {
    private static ReservaService instancia;
    private ReservaDAO reservaDAO;

    private ReservaService() {
        this.reservaDAO = new ReservaDAO();
    }

    public static ReservaService getInstancia() {
        if (instancia == null) {
            instancia = new ReservaService();
        }
        return instancia;
    }

    public boolean crearReserva(Usuario usuario, Sala sala, LocalDate fecha, LocalTime horaInicio, LocalTime horaFin, String motivo) {
        // Validar que no haya conflicto de horarios
        if (reservaDAO.hayConflicto(sala, fecha, horaInicio, horaFin, null)) {
            return false;
        }

        Reserva nuevaReserva = new Reserva(usuario, sala, fecha, horaInicio, horaFin, motivo);
        nuevaReserva.setEstado("Confirmada");
        return reservaDAO.crearReserva(nuevaReserva);
    }

    public boolean actualizarReserva(String id, Sala sala, LocalDate fecha, LocalTime horaInicio, LocalTime horaFin, String motivo) {
        Optional<Reserva> reservaOpt = reservaDAO.buscarPorId(id);
        if (reservaOpt.isPresent()) {
            Reserva reserva = reservaOpt.get();
            
            // Validar que no haya conflicto (excluyendo la reserva actual)
            if (reservaDAO.hayConflicto(sala, fecha, horaInicio, horaFin, id)) {
                return false;
            }

            reserva.setSala(sala);
            reserva.setFecha(fecha);
            reserva.setHoraInicio(horaInicio);
            reserva.setHoraFin(horaFin);
            reserva.setMotivo(motivo);
            return reservaDAO.actualizarReserva(reserva);
        }
        return false;
    }

    public boolean cancelarReserva(String id) {
        return reservaDAO.cancelarReserva(id);
    }

    public boolean eliminarReserva(String id) {
        return reservaDAO.eliminarReserva(id);
    }

    public List<Reserva> getReservasPorUsuario(Usuario usuario) {
        if (usuario == null || usuario.getEmail() == null) {
            return List.of();
        }
        return reservaDAO.obtenerReservasPorUsuario(usuario);
    }

    public List<Reserva> getReservasFuturas(Usuario usuario) {
        return getReservasPorUsuario(usuario).stream()
                .filter(Reserva::esFutura)
                .filter(r -> !r.getEstado().equals("Cancelada"))
                .collect(Collectors.toList());
    }

    public List<Reserva> getReservasPasadas(Usuario usuario) {
        return getReservasPorUsuario(usuario).stream()
                .filter(Reserva::esPasada)
                .collect(Collectors.toList());
    }

    public List<Reserva> getTodasLasReservas(Usuario usuario) {
        return getReservasPorUsuario(usuario);
    }

    public Optional<Reserva> buscarPorId(String id) {
        return reservaDAO.buscarPorId(id);
    }

    public List<Reserva> getReservasPorSalaYFecha(Sala sala, LocalDate fecha) {
        if (sala == null || sala.getId() == null || fecha == null) {
            return List.of();
        }
        return reservaDAO.obtenerReservasPorSalaYFecha(sala, fecha);
    }
}
