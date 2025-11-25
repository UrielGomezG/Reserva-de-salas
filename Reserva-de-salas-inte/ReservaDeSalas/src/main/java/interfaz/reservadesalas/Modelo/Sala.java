package interfaz.reservadesalas.Modelo;

public class Sala {
    private String id;
    private String nombre;
    private String ubicacion;
    private int capacidad;
    private String tipo; // "Laboratorio", "Sala de Juntas", "Aula", etc.
    private boolean disponible;

    public Sala() {
        this.disponible = true;
    }

    public Sala(String id, String nombre, String ubicacion, int capacidad, String tipo) {
        this.id = id;
        this.nombre = nombre;
        this.ubicacion = ubicacion;
        this.capacidad = capacidad;
        this.tipo = tipo;
        this.disponible = true;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion;
    }

    public int getCapacidad() {
        return capacidad;
    }

    public void setCapacidad(int capacidad) {
        this.capacidad = capacidad;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public boolean isDisponible() {
        return disponible;
    }

    public void setDisponible(boolean disponible) {
        this.disponible = disponible;
    }

    @Override
    public String toString() {
        return nombre + " (" + ubicacion + ")";
    }
}

