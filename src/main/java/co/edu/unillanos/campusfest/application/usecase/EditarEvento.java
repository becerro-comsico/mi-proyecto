package co.edu.unillanos.campusfest.application.usecase;

import java.time.LocalDateTime;

import co.edu.unillanos.campusfest.application.port.Reloj;
import co.edu.unillanos.campusfest.application.port.RepositorioEventos;
import co.edu.unillanos.campusfest.domain.Evento;

/** CU3: editar los datos y el aforo de un evento. */
public class EditarEvento {

    private final RepositorioEventos repositorio;
    private final Reloj reloj;

    public EditarEvento(RepositorioEventos repositorio, Reloj reloj) {
        this.repositorio = repositorio;
        this.reloj = reloj;
    }

    public Evento obtener(String eventoId) {
        return EventoExistente.buscar(repositorio, eventoId);
    }

    public void editar(String eventoId, String nombre, LocalDateTime fecha,
                       String lugar, int nuevoAforo) {
        Evento evento = EventoExistente.buscar(repositorio, eventoId);
        evento.cambiarAforo(nuevoAforo, reloj.ahora());
        evento.cambiarDatos(nombre, fecha, lugar);
        repositorio.guardar(evento);
    }
}