package interfaz.reservadesalas.database;

import interfaz.reservadesalas.Modelo.Reserva;
import interfaz.reservadesalas.Modelo.Sala;
import interfaz.reservadesalas.Modelo.Usuario;
import interfaz.reservadesalas.Servicio.ReservaService;
import interfaz.reservadesalas.Servicio.SalaService;
import interfaz.reservadesalas.Servicio.UsuarioService;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Prueba de integración completa del sistema con la base de datos MySQL
 */
public class IntegrationTest {
    
    public static void main(String[] args) {
        System.out.println("==========================================");
        System.out.println("  PRUEBA DE INTEGRACIÓN COMPLETA");
        System.out.println("==========================================");
        System.out.println();
        
        int pruebasExitosas = 0;
        int pruebasFallidas = 0;
        
        // Test 1: Conexión a la base de datos
        System.out.println("TEST 1: Verificar conexión a la base de datos");
        if (DatabaseConnection.testConnection()) {
            System.out.println("✓ Conexión exitosa\n");
            pruebasExitosas++;
        } else {
            System.out.println("✗ Error de conexión\n");
            pruebasFallidas++;
            return; // Si no hay conexión, no podemos continuar
        }
        
        // Test 2: Obtener salas
        System.out.println("TEST 2: Obtener salas de la base de datos");
        SalaService salaService = SalaService.getInstancia();
        List<Sala> salas = salaService.getTodasLasSalas();
        if (!salas.isEmpty()) {
            System.out.println("✓ Se obtuvieron " + salas.size() + " salas");
            System.out.println("  Primera sala: " + salas.get(0).getNombre() + " (" + salas.get(0).getTipo() + ")\n");
            pruebasExitosas++;
        } else {
            System.out.println("✗ No se encontraron salas en la base de datos\n");
            pruebasFallidas++;
        }
        
        // Test 3: Buscar usuario existente
        System.out.println("TEST 3: Buscar usuario existente");
        UsuarioService usuarioService = UsuarioService.getInstancia();
        boolean loginExitoso = usuarioService.iniciarSesion("muchacho123@utez.edu.mx", "password123");
        if (loginExitoso) {
            Usuario usuarioActual = usuarioService.getUsuarioActual();
            System.out.println("✓ Login exitoso");
            System.out.println("  Usuario: " + usuarioActual.getNombre());
            System.out.println("  Email: " + usuarioActual.getEmail() + "\n");
            pruebasExitosas++;
        } else {
            System.out.println("⚠ Usuario de prueba no encontrado (esto es normal si no existe en la BD)");
            System.out.println("  Intentando crear usuario de prueba...\n");
            
            // Test 3b: Crear usuario de prueba
            boolean registroExitoso = usuarioService.registrarUsuario(
                "Usuario Prueba", 
                "prueba@test.com", 
                "PRUEBA001", 
                "password123"
            );
            if (registroExitoso) {
                System.out.println("✓ Usuario de prueba creado exitosamente");
                loginExitoso = usuarioService.iniciarSesion("prueba@test.com", "password123");
                if (loginExitoso) {
                    System.out.println("✓ Login con usuario nuevo exitoso\n");
                    pruebasExitosas++;
                } else {
                    System.out.println("✗ Error al hacer login con usuario nuevo\n");
                    pruebasFallidas++;
                }
            } else {
                System.out.println("✗ Error al crear usuario de prueba (puede que ya exista)\n");
                pruebasFallidas++;
            }
        }
        
        // Test 4: Crear una reserva
        System.out.println("TEST 4: Crear una reserva");
        if (loginExitoso && !salas.isEmpty()) {
            Usuario usuario = usuarioService.getUsuarioActual();
            Sala sala = salas.get(0);
            
            LocalDate fecha = LocalDate.now().plusDays(1); // Mañana
            LocalTime horaInicio = LocalTime.of(10, 0);
            LocalTime horaFin = LocalTime.of(11, 0);
            
            ReservaService reservaService = ReservaService.getInstancia();
            boolean reservaCreada = reservaService.crearReserva(
                usuario, 
                sala, 
                fecha, 
                horaInicio, 
                horaFin, 
                "Prueba de integración"
            );
            
            if (reservaCreada) {
                System.out.println("✓ Reserva creada exitosamente");
                System.out.println("  Sala: " + sala.getNombre());
                System.out.println("  Fecha: " + fecha);
                System.out.println("  Horario: " + horaInicio + " - " + horaFin + "\n");
                pruebasExitosas++;
                
                // Test 5: Obtener reservas del usuario
                System.out.println("TEST 5: Obtener reservas del usuario");
                List<Reserva> reservas = reservaService.getReservasPorUsuario(usuario);
                if (!reservas.isEmpty()) {
                    System.out.println("✓ Se obtuvieron " + reservas.size() + " reserva(s)");
                    Reserva ultimaReserva = reservas.get(reservas.size() - 1);
                    System.out.println("  Última reserva: " + ultimaReserva.getId());
                    System.out.println("  Estado: " + ultimaReserva.getEstado() + "\n");
                    pruebasExitosas++;
                    
                    // Test 6: Obtener reservas futuras
                    System.out.println("TEST 6: Obtener reservas futuras");
                    List<Reserva> reservasFuturas = reservaService.getReservasFuturas(usuario);
                    System.out.println("✓ Se encontraron " + reservasFuturas.size() + " reserva(s) futura(s)\n");
                    pruebasExitosas++;
                    
                } else {
                    System.out.println("✗ No se encontraron reservas\n");
                    pruebasFallidas++;
                }
            } else {
                System.out.println("✗ Error al crear reserva (puede haber conflicto de horario)\n");
                pruebasFallidas++;
            }
        } else {
            System.out.println("⚠ No se puede probar reservas (usuario o salas no disponibles)\n");
        }
        
        // Test 7: Buscar sala por tipo
        System.out.println("TEST 7: Buscar salas por tipo");
        List<Sala> salasPorTipo = salaService.buscarPorTipo("Laboratorio");
        System.out.println("✓ Se encontraron " + salasPorTipo.size() + " sala(s) de tipo 'Laboratorio'\n");
        pruebasExitosas++;
        
        // Resumen
        System.out.println("==========================================");
        System.out.println("  RESUMEN DE PRUEBAS");
        System.out.println("==========================================");
        System.out.println("Pruebas exitosas: " + pruebasExitosas);
        System.out.println("Pruebas fallidas: " + pruebasFallidas);
        System.out.println("Total: " + (pruebasExitosas + pruebasFallidas));
        System.out.println();
        
        if (pruebasFallidas == 0) {
            System.out.println("✓ TODAS LAS PRUEBAS PASARON EXITOSAMENTE");
        } else {
            System.out.println("⚠ ALGUNAS PRUEBAS FALLARON - Revisa los mensajes anteriores");
        }
        System.out.println("==========================================");
        
        // Cerrar conexión
        DatabaseConnection.closeConnection();
    }
}

