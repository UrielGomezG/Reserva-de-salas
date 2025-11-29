package interfaz.sara.Modelo;

import java.time.LocalDateTime;

/**
 * Modelo que representa un incidente reportado en el sistema SARA
 */
public class Incidente {
    
    private Long id;
    private Long roomId;
    private String nombreSala;
    private Long userId;
    private String tipoIncidente;
    private String descripcion;
    private LocalDateTime fechaReporte;
    private LocalDateTime fechaCreacion;
    private String estado;
    
    // ========== Constructores ==========
    
    public Incidente() {
    }
    
    public Incidente(Long id, Long roomId, String nombreSala, Long userId, 
                     String tipoIncidente, String descripcion, 
                     LocalDateTime fechaReporte, String estado) {
        this.id = id;
        this.roomId = roomId;
        this.nombreSala = nombreSala;
        this.userId = userId;
        this.tipoIncidente = tipoIncidente;
        this.descripcion = descripcion;
        this.fechaReporte = fechaReporte;
        this.estado = estado;
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
    
    public String getTipoIncidente() {
        return tipoIncidente;
    }
    
    public void setTipoIncidente(String tipoIncidente) {
        this.tipoIncidente = tipoIncidente;
    }
    
    public String getDescripcion() {
        return descripcion;
    }
    
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
    
    public LocalDateTime getFechaReporte() {
        return fechaReporte;
    }
    
    public void setFechaReporte(LocalDateTime fechaReporte) {
        this.fechaReporte = fechaReporte;
    }
    
    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }
    
    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }
    
    public String getEstado() {
        return estado;
    }
    
    public void setEstado(String estado) {
        this.estado = estado;
    }
}

