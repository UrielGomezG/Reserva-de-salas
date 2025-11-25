package interfaz.reservadesalas.Servicio;

import interfaz.reservadesalas.Modelo.Sala;
import interfaz.reservadesalas.database.SalaDAO;
import java.util.List;
import java.util.Optional;

public class SalaService {
    private static SalaService instancia;
    private SalaDAO salaDAO;

    private SalaService() {
        this.salaDAO = new SalaDAO();
    }

    public static SalaService getInstancia() {
        if (instancia == null) {
            instancia = new SalaService();
        }
        return instancia;
    }

    public List<Sala> getTodasLasSalas() {
        return salaDAO.obtenerTodas();
    }

    public Optional<Sala> buscarPorId(String id) {
        return salaDAO.buscarPorId(id);
    }

    public List<Sala> buscarPorTipo(String tipo) {
        return salaDAO.buscarPorTipo(tipo);
    }

    public List<Sala> buscarPorNombre(String nombre) {
        return salaDAO.buscarPorNombre(nombre);
    }
}
