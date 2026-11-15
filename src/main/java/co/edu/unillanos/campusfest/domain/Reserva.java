package co.edu.unillanos.campusfest.domain;

import java.time.LocalDateTime;
import java.util.UUID;

public class Reserva {

    private static final int MINUTOS_DE_PLAZO = 10;

    private final String codigo;
    private final String estudianteId;
    private final LocalDateTime venceEn;
    private EstadoReserva estado;
    private Asistencia asistencia;

    private Reserva(String codigo, String estudianteId, LocalDateTime venceEn,
                    EstadoReserva estado, Asistencia asistencia) {
        this.codigo = codigo;
        this.estudianteId = estudianteId;
        this.venceEn = venceEn;
        this.estado = estado;
        this.asistencia = asistencia;
    }

    public static Reserva pendiente(String estudianteId, LocalDateTime ahora) {
        return new Reserva(UUID.randomUUID().toString(), estudianteId,
                ahora.plusMinutes(MINUTOS_DE_PLAZO), EstadoReserva.PENDIENTE, null);
    }

    public static Reserva reconstruir(String codigo, String estudianteId, LocalDateTime venceEn,
                                      EstadoReserva estado, Asistencia asistencia) {
        return new Reserva(codigo, estudianteId, venceEn, estado, asistencia);
    }

    public boolean ocupaCupo(LocalDateTime ahora) {
        if (estado == EstadoReserva.CONFIRMADA) {
            return true;
        }
        return estado == EstadoReserva.PENDIENTE && ahora.isBefore(venceEn);
    }

    public void confirmar(LocalDateTime ahora) {
        if (estado != EstadoReserva.PENDIENTE) {
            throw new IllegalStateException("Solo se puede confirmar una reserva pendiente.");
        }
        if (!ahora.isBefore(venceEn)) {
            throw new IllegalStateException("La reserva venció: el plazo de 10 minutos terminó.");
        }
        estado = EstadoReserva.CONFIRMADA;
    }

    public void cancelar() {
        if (estado != EstadoReserva.PENDIENTE && estado != EstadoReserva.CONFIRMADA) {
            throw new IllegalStateException("La reserva no se puede cancelar en estado " + estado + ".");
        }
        estado = EstadoReserva.CANCELADA;
    }

    public Asistencia registrarIngreso(LocalDateTime ahora) {
        if (estado != EstadoReserva.CONFIRMADA) {
            throw new IllegalStateException("Solo una reserva confirmada puede ingresar.");
        }
        if (asistencia != null) {
            throw new IllegalStateException("La reserva ya registró su ingreso.");
        }
        asistencia = new Asistencia(ahora);
        return asistencia;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getEstudianteId() {
        return estudianteId;
    }

    public LocalDateTime getVenceEn() {
        return venceEn;
    }

    public EstadoReserva getEstado() {
        return estado;
    }

    public Asistencia getAsistencia() {
        return asistencia;
    }
}