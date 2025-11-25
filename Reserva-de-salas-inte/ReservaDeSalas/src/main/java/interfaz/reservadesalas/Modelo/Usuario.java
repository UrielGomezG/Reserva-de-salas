package interfaz.reservadesalas.Modelo;

import java.time.LocalDateTime;

public class Usuario {
    private String nombre;
    private String email;
    private String matricula;
    private String contrasena;
    private LocalDateTime fechaRegistro;

    public Usuario() {
        this.fechaRegistro = LocalDateTime.now();
    }

    public Usuario(String nombre, String email, String matricula, String contrasena) {
        this.nombre = nombre;
        this.email = email;
        this.matricula = matricula;
        this.contrasena = contrasena;
        this.fechaRegistro = LocalDateTime.now();
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMatricula() {
        return matricula;
    }

    public void setMatricula(String matricula) {
        this.matricula = matricula;
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public boolean validarContrasena(String contrasena) {
        return this.contrasena != null && this.contrasena.equals(contrasena);
    }
}

