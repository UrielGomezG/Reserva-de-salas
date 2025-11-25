package interfaz.reservadesalas.Servicio;

import interfaz.reservadesalas.Modelo.Usuario;
import interfaz.reservadesalas.database.UsuarioDAO;
import java.util.List;
import java.util.Optional;

public class UsuarioService {
    private static UsuarioService instancia;
    private UsuarioDAO usuarioDAO;
    private Usuario usuarioActual;

    private UsuarioService() {
        this.usuarioDAO = new UsuarioDAO();
    }

    public static UsuarioService getInstancia() {
        if (instancia == null) {
            instancia = new UsuarioService();
        }
        return instancia;
    }

    public boolean registrarUsuario(String nombre, String email, String matricula, String contrasena) {
        // Validar que el email no esté registrado
        if (usuarioDAO.buscarPorEmail(email).isPresent()) {
            return false;
        }

        // Validar que la matrícula no esté registrada
        if (usuarioDAO.buscarPorMatricula(matricula).isPresent()) {
            return false;
        }

        Usuario nuevoUsuario = new Usuario(nombre, email, matricula, contrasena);
        return usuarioDAO.crearUsuario(nuevoUsuario);
    }

    public boolean iniciarSesion(String email, String contrasena) {
        Optional<Usuario> usuarioOpt = usuarioDAO.buscarPorEmail(email);
        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            if (usuario.validarContrasena(contrasena)) {
                this.usuarioActual = usuario;
                return true;
            }
        }
        return false;
    }

    public void cerrarSesion() {
        this.usuarioActual = null;
    }

    public Usuario getUsuarioActual() {
        return usuarioActual;
    }

    public boolean actualizarUsuario(Usuario usuario) {
        boolean exito = usuarioDAO.actualizarUsuario(usuario);
        if (exito && usuarioActual != null && usuarioActual.getEmail().equals(usuario.getEmail())) {
            // Actualizar el usuario actual en memoria
            usuarioActual.setNombre(usuario.getNombre());
            usuarioActual.setMatricula(usuario.getMatricula());
            if (usuario.getContrasena() != null && !usuario.getContrasena().isEmpty()) {
                usuarioActual.setContrasena(usuario.getContrasena());
            }
        }
        return exito;
    }

    public List<Usuario> getUsuarios() {
        return usuarioDAO.obtenerTodos();
    }
}
