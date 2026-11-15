package co.edu.unillanos.campusfest.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Evento {

    private final String id;
    private String nombre;
    private LocalDateTime fecha;
    private String lugar;
    private int aforo;
    private final int bloqueados;
    private final List<Reserva> reservas;

    public Evento(String id, String nombre, LocalDateTime fecha, String lugar,
                  int aforo, int bloqueados) {
        this(id, nombre, fecha, lugar, aforo, bloqueados, new ArrayList<>());
    }

    public Evento(String id, String nombre, LocalDateTime fecha, String lugar,
                  int aforo, int bloqueados, List<Reserva> reservas) {
        validarDatos(nombre, fecha, lugar);
        if (aforo < 0 || bloqueados < 0 || bloqueados > aforo) {
            throw new IllegalArgumentException("El aforo y los cupos bloqueados no son válidos.");
        }
        this.id = id;
        this.nombre = nombre;
        this.fecha = fecha;
        this.lugar = lugar;
        this.aforo = aforo;
        this.bloqueados = bloqueados;
        this.reservas = new ArrayList<>(reservas);
    }

    public int cuposDisponibles(LocalDateTime ahora) {
        return aforo - bloqueados - cuposOcupadosPorReservas(ahora);
    }

    public Reserva reservar(String estudianteId, LocalDateTime ahora) {
        if (cuposDisponibles(ahora) <= 0) {
            throw new IllegalStateException("No hay cupos disponibles para este evento.");
        }
        Reserva reserva = Reserva.pendiente(estudianteId, ahora);
        reservas.add(reserva);
        return reserva;
    }

    public Reserva buscarReservaPorCodigo(String codigo) {
        for (Reserva reserva : reservas) {
            if (reserva.getCodigo().equals(codigo)) {
                return reserva;
            }
        }
        throw new IllegalArgumentException("El código " + codigo + " no pertenece a este evento.");
    }

    public void cambiarDatos(String nombre, LocalDateTime fecha, String lugar) {
        validarDatos(nombre, fecha, lugar);
        this.nombre = nombre;
        this.fecha = fecha;
        this.lugar = lugar;
    }

    public void cambiarAforo(int nuevoAforo, LocalDateTime ahora) {
        int ocupados = cuposOcupadosPorReservas(ahora);
        int aforoMinimo = bloqueados + ocupados;
        if (nuevoAforo < aforoMinimo) {
            throw new IllegalStateException("Hay " + ocupados + " cupos ocupados por reservas y "
                    + bloqueados + " bloqueados: el aforo no puede ser menor que " + aforoMinimo + ".");
        }
        this.aforo = nuevoAforo;
    }

    private int cuposOcupadosPorReservas(LocalDateTime ahora) {
        int ocupados = 0;
        for (Reserva reserva : reservas) {
            if (reserva.ocupaCupo(ahora)) {
                ocupados++;
            }
        }
        return ocupados;
    }

    private static void validarDatos(String nombre, LocalDateTime fecha, String lugar) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre del evento no puede estar vacío.");
        }
        if (lugar == null || lugar.isBlank()) {
            throw new IllegalArgumentException("El lugar del evento no puede estar vacío.");
        }
        if (fecha == null) {
            throw new IllegalArgumentException("La fecha del evento no puede estar vacía.");
        }
    }

    public String getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public String getLugar() {
        return lugar;
    }

    public int getAforo() {
        return aforo;
    }

    public int getBloqueados() {
        return bloqueados;
    }

    public List<Reserva> getReservas() {
        return Collections.unmodifiableList(reservas);
    }
}