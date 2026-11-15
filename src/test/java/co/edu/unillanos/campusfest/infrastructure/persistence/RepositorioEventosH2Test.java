package co.edu.unillanos.campusfest.infrastructure.persistence;

import co.edu.unillanos.campusfest.application.port.RepositorioEventos;
import co.edu.unillanos.campusfest.domain.EstadoReserva;
import co.edu.unillanos.campusfest.domain.Evento;
import co.edu.unillanos.campusfest.domain.Reserva;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RepositorioEventosH2Test {
    private final LocalDateTime fecha = LocalDateTime.of(2026, 11, 28, 19, 0);

    private Connection conexionNueva() {
        try {
            return DriverManager.getConnection("jdbc:h2:mem:test_" + UUID.randomUUID());
        } catch (SQLException error) {
            throw new IllegalStateException(error);
        }
    }

    private Connection conexionCerrada() {
        try {
            Connection conexion = conexionNueva();
            conexion.close();
            return conexion;
        } catch (SQLException error) {
            throw new IllegalStateException(error);
        }
    }

    private RepositorioEventosH2 nuevoRepositorio() {
        return new RepositorioEventosH2(conexionNueva());
    }

    @Test void implementaPuertoYNoEncuentraEventoInexistente() {
        RepositorioEventos repo = nuevoRepositorio();
        assertInstanceOf(RepositorioEventos.class, repo);
        assertNull(repo.buscar("inexistente"));
    }

    @Test void guardaYRecuperaEvento() {
        RepositorioEventos repo = nuevoRepositorio();
        repo.guardar(new Evento("ev1", "Concierto", fecha, "Auditorio", 50, 3));
        Evento recuperado = repo.buscar("ev1");
        assertEquals("Concierto", recuperado.getNombre());
        assertEquals(fecha, recuperado.getFecha());
        assertEquals("Auditorio", recuperado.getLugar());
        assertEquals(50, recuperado.getAforo());
        assertEquals(3, recuperado.getBloqueados());
    }

    @Test void guardarDosVecesActualizaSinDuplicar() {
        RepositorioEventos repo = nuevoRepositorio();
        Evento evento = new Evento("ev1", "Original", fecha, "Aula", 5, 0);
        repo.guardar(evento);
        evento.cambiarDatos("Actualizado", fecha.plusDays(1), "Auditorio");
        repo.guardar(evento);
        assertEquals("Actualizado", repo.buscar("ev1").getNombre());
        assertEquals(fecha.plusDays(1), repo.buscar("ev1").getFecha());
    }

    @Test void persisteReservasEstadosYAsistenciasSinDuplicar() {
        RepositorioEventos repo = nuevoRepositorio();
        Evento evento = new Evento("ev1", "Evento", fecha, "Aula", 10, 0);
        LocalDateTime ahora = fecha.minusDays(1);
        Reserva confirmada = evento.reservar("est1", ahora);
        confirmada.confirmar(ahora.plusMinutes(1));
        confirmada.registrarIngreso(ahora.plusMinutes(2));
        Reserva pendiente = evento.reservar("est2", ahora);
        repo.guardar(evento);
        repo.guardar(evento);
        Evento recuperado = repo.buscar("ev1");
        assertEquals(2, recuperado.getReservas().size());
        Reserva restaurada = recuperado.buscarReservaPorCodigo(confirmada.getCodigo());
        assertEquals(EstadoReserva.CONFIRMADA, restaurada.getEstado());
        assertEquals(ahora.plusMinutes(2), restaurada.getAsistencia().getHora());
        assertEquals(pendiente.getVenceEn(), recuperado.buscarReservaPorCodigo(pendiente.getCodigo()).getVenceEn());
    }

    @Test void falloDeBaseDeDatosSeConvierteEnIllegalStateException() {
        assertThrows(IllegalStateException.class,
                () -> new RepositorioEventosH2(conexionCerrada()));
    }

    @Test void rollbackEvitaGuardarParcialmente() {
        RepositorioEventos repo = nuevoRepositorio();
        Evento original = new Evento("ev1", "Original", fecha, "Aula", 10, 0);
        repo.guardar(original);
        Reserva reservaInvalida = Reserva.reconstruir("r1", null, fecha, EstadoReserva.PENDIENTE, null);
        Evento modificado = new Evento("ev1", "Nuevo", fecha, "Aula", 10, 0, java.util.List.of(reservaInvalida));
        assertThrows(IllegalStateException.class, () -> repo.guardar(modificado));
        assertEquals("Original", repo.buscar("ev1").getNombre());
        assertTrue(repo.buscar("ev1").getReservas().isEmpty());
    }
}
