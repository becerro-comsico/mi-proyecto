package co.edu.unillanos.campusfest.infrastructure.persistence;

import java.util.HashMap;
import java.util.Map;
import co.edu.unillanos.campusfest.application.port.RepositorioEventos;
import co.edu.unillanos.campusfest.domain.Evento;

/** Adaptador de almacenamiento temporal para eventos. */
public class RepositorioEventosEnMemoria implements RepositorioEventos {
    private final Map<String, Evento> eventos = new HashMap<>();

    @Override
    public Evento buscar(String eventoId) {
        return eventos.get(eventoId);
    }

    @Override
    public void guardar(Evento evento) {
        if (evento == null) throw new IllegalArgumentException("El evento no puede ser nulo.");
        eventos.put(evento.getId(), evento);
    }
}
