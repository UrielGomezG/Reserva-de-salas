package interfaz.reservadesalas.Modelo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class Reserva {
    private String id;
    private Usuario usuario;
    private Sala sala;
    private LocalDate fecha;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private String motivo;
    private String estado; // "Pendiente", "Confirmada", "Cancelada", "Completada"
    private LocalDateTime fechaCreacion;

    public Reserva() {
        this.estado = "Pendiente";
        this.fechaCreacion = LocalDateTime.now();
    }

    public Reserva(Usuario usuario, Sala sala, LocalDate fecha, LocalTime horaInicio, LocalTime horaFin, String motivo) {
        this.usuario = usuario;
        this.sala = sala;
        this.fecha = fecha;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
        this.motivo = motivo;
        this.estado = "Pendiente";
        this.fechaCreacion = LocalDateTime.now();
        this.id = generarId();
    }

    private String generarId() {
        return "RES-" + System.currentTimeMillis();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Sala getSala() {
        return sala;
    }

    public void setSala(Sala sala) {
        this.sala = sala;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public LocalTime getHoraInicio() {
        return horaInicio;
    }

    public void setHoraInicio(LocalTime horaInicio) {
        this.horaInicio = horaInicio;
    }

    public LocalTime getHoraFin() {
        return horaFin;
    }

    public void setHoraFin(LocalTime horaFin) {
        this.horaFin = horaFin;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public boolean esPasada() {
        if (fecha == null) return false;
        LocalDate hoy = LocalDate.now();
        if (fecha.isBefore(hoy)) {
            return true;
        }
        if (fecha.equals(hoy) && horaFin != null) {
            return horaFin.isBefore(LocalTime.now());
        }
        return false;
    }

    public boolean esFutura() {
        if (fecha == null) return false;
        LocalDate hoy = LocalDate.now();
        if (fecha.isAfter(hoy)) {
            return true;
        }
        if (fecha.equals(hoy) && horaInicio != null) {
            return horaInicio.isAfter(LocalTime.now());
        }
        return false;
    }
}

