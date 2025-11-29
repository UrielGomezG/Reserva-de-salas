package interfaz.sara.Modelo;

/**
 * Modelo que representa una sala en el sistema SARA
 */
public class Sala {
    
    private Long id;
    private Integer codigoSala;
    private String nombre;
    private Integer capacidad;
    private String tipoSala;
    private String ubicacion;
    private Boolean habilitada;
    
    // ========== Constructores ==========
    
    public Sala() {
    }
    
    public Sala(Long id, Integer codigoSala, String nombre, Integer capacidad, 
                String tipoSala, String ubicacion, Boolean habilitada) {
        this.id = id;
        this.codigoSala = codigoSala;
        this.nombre = nombre;
        this.capacidad = capacidad;
        this.tipoSala = tipoSala;
        this.ubicacion = ubicacion;
        this.habilitada = habilitada;
    }
    
    // ========== Getters y Setters ==========
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Integer getCodigoSala() {
        return codigoSala;
    }
    
    public void setCodigoSala(Integer codigoSala) {
        this.codigoSala = codigoSala;
    }
    
    public String getNombre() {
        return nombre;
    }
    
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    
    public Integer getCapacidad() {
        return capacidad;
    }
    
    public void setCapacidad(Integer capacidad) {
        this.capacidad = capacidad;
    }
    
    public String getTipoSala() {
        return tipoSala;
    }
    
    public void setTipoSala(String tipoSala) {
        this.tipoSala = tipoSala;
    }
    
    public String getUbicacion() {
        return ubicacion;
    }
    
    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion;
    }
    
    public Boolean getHabilitada() {
        return habilitada;
    }
    
    public void setHabilitada(Boolean habilitada) {
        this.habilitada = habilitada;
    }
}

