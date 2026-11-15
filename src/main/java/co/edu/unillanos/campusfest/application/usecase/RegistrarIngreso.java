package co.edu.unillanos.campusfest.application.usecase;

import co.edu.unillanos.campusfest.application.port.Reloj;
import co.edu.unillanos.campusfest.application.port.RepositorioEventos;
import co.edu.unillanos.campusfest.domain.Asistencia;
import co.edu.unillanos.campusfest.domain.Evento;
import co.edu.unillanos.campusfest.domain.Reserva;

/** CU4: registrar el ingreso de un estudiante con el código de su reserva. */
public class RegistrarIngreso {

    private final RepositorioEventos repositorio;
    private final Reloj reloj;

    public RegistrarIngreso(RepositorioEventos repositorio, Reloj reloj) {
        this.repositorio = repositorio;
        this.reloj = reloj;
    }

    public Asistencia registrar(String eventoId, String codigo) {
        Evento evento = EventoExistente.buscar(repositorio, eventoId);
        Reserva reserva = evento.buscarReservaPorCodigo(codigo);
        Asistencia asistencia = reserva.registrarIngreso(reloj.ahora());
        repositorio.guardar(evento);
        return asistencia;
    }
}