package interfaz.sara.Modelo;

import java.time.LocalDateTime;

/**
 * Modelo que representa una reserva en el sistema SARA
 */
public class Reserva {
    
    private Long id;
    private Long roomId;
    private String nombreSala;
    private Long userId;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;
    private Integer cantidadPersonas;
    private String motivo;
    private Integer statusId;
    private String estadoReserva;
    private String codigoEstado;
    private Long canceladoPorUsuarioId;
    private String motivoCancelacion;
    private LocalDateTime fechaCreacion;
    
    // ========== Constructores ==========
    
    public Reserva() {
    }
    
    public Reserva(Long id, Long roomId, String nombreSala, Long userId, 
                   LocalDateTime fechaInicio, LocalDateTime fechaFin, 
                   Integer cantidadPersonas, String motivo, Integer statusId,
                   String estadoReserva, String codigoEstado) {
        this.id = id;
        this.roomId = roomId;
        this.nombreSala = nombreSala;
        this.userId = userId;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.cantidadPersonas = cantidadPersonas;
        this.motivo = motivo;
        this.statusId = statusId;
        this.estadoReserva = estadoReserva;
        this.codigoEstado = codigoEstado;
    }
    
    // ========== Métodos de utilidad ==========
    
    /**
     * Verifica si la reserva es próxima (fecha de inicio es futura)
     * 
     * @return true si la reserva es próxima, false en caso contrario
     */
    public boolean esProxima() {
        return fechaInicio != null && fechaInicio.isAfter(LocalDateTime.now());
    }
    
    /**
     * Verifica si la reserva es pasada (fecha de fin es pasada)
     * 
     * @return true si la reserva es pasada, false en caso contrario
     */
    public boolean esPasada() {
        return fechaFin != null && fechaFin.isBefore(LocalDateTime.now());
    }
    
    /**
     * Verifica si la reserva está cancelada
     * 
     * @return true si la reserva está cancelada, false en caso contrario
     */
    public boolean estaCancelada() {
        return codigoEstado != null && 
               (codigoEstado.equals("CANCELLED_USER") || codigoEstado.equals("CANCELLED_ADMIN"));
    }
    
    // ========== Getters y Setters ==========
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getRoomId() {
        return roomId;
    }
    
    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }
    
    public String getNombreSala() {
        return nombreSala;
    }
    
    public void setNombreSala(String nombreSala) {
        this.nombreSala = nombreSala;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public LocalDateTime getFechaInicio() {
        return fechaInicio;
    }
    
    public void setFechaInicio(LocalDateTime fechaInicio) {
        this.fechaInicio = fechaInicio;
    }
    
    public LocalDateTime getFechaFin() {
        return fechaFin;
    }
    
    public void setFechaFin(LocalDateTime fechaFin) {
        this.fechaFin = fechaFin;
    }
    
    public Integer getCantidadPersonas() {
        return cantidadPersonas;
    }
    
    public void setCantidadPersonas(Integer cantidadPersonas) {
        this.cantidadPersonas = cantidadPersonas;
    }
    
    public String getMotivo() {
        return motivo;
    }
    
    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }
    
    public Integer getStatusId() {
        return statusId;
    }
    
    public void setStatusId(Integer statusId) {
        this.statusId = statusId;
    }
    
    public String getEstadoReserva() {
        return estadoReserva;
    }
    
    public void setEstadoReserva(String estadoReserva) {
        this.estadoReserva = estadoReserva;
    }
    
    public String getCodigoEstado() {
        return codigoEstado;
    }
    
    public void setCodigoEstado(String codigoEstado) {
        this.codigoEstado = codigoEstado;
    }
    
    public Long getCanceladoPorUsuarioId() {
        return canceladoPorUsuarioId;
    }
    
    public void setCanceladoPorUsuarioId(Long canceladoPorUsuarioId) {
        this.canceladoPorUsuarioId = canceladoPorUsuarioId;
    }
    
    public String getMotivoCancelacion() {
        return motivoCancelacion;
    }
    
    public void setMotivoCancelacion(String motivoCancelacion) {
        this.motivoCancelacion = motivoCancelacion;
    }
    
    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }
    
    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }
}

