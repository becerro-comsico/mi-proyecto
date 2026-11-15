package co.edu.unillanos.campusfest.application.usecase;

import co.edu.unillanos.campusfest.application.port.Reloj;
import co.edu.unillanos.campusfest.application.port.RepositorioEventos;

/** CU1: consultar los cupos disponibles de un evento. */
public class ConsultarCupos {

    private final RepositorioEventos repositorio;
    private final Reloj reloj;

    public ConsultarCupos(RepositorioEventos repositorio, Reloj reloj) {
        this.repositorio = repositorio;
        this.reloj = reloj;
    }

    public int consultar(String eventoId) {
        return EventoExistente.buscar(repositorio, eventoId).cuposDisponibles(reloj.ahora());
    }
}