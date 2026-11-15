package co.edu.unillanos.campusfest.application.usecase;

import co.edu.unillanos.campusfest.application.port.RepositorioEventos;
import co.edu.unillanos.campusfest.domain.Evento;

/** Busca un evento y rechaza el caso de uso si no existe. */
final class EventoExistente {

    private EventoExistente() {
    }

    static Evento buscar(RepositorioEventos repositorio, String eventoId) {
        Evento evento = repositorio.buscar(eventoId);
        if (evento == null) {
            throw new IllegalArgumentException("El evento " + eventoId + " no existe.");
        }
        return evento;
    }
}