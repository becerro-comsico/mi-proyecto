package co.edu.unillanos.campusfest.application.port;

import co.edu.unillanos.campusfest.domain.Evento;

public interface RepositorioEventos {

    /** Devuelve el evento, o null si no existe. */
    Evento buscar(String eventoId);

    /** Inserta o actualiza el evento con sus reservas y asistencias. */
    void guardar(Evento evento);
}