package co.edu.unillanos.campusfest.infrastructure.persistence;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import co.edu.unillanos.campusfest.application.port.RepositorioEventos;
import co.edu.unillanos.campusfest.domain.Asistencia;
import co.edu.unillanos.campusfest.domain.EstadoReserva;
import co.edu.unillanos.campusfest.domain.Evento;
import co.edu.unillanos.campusfest.domain.Reserva;

/** Adaptador JDBC H2. Recibe una conexión abierta. Cada guardar es una transacción atómica. */
public class RepositorioEventosH2 implements RepositorioEventos {

    private final Connection conexion;

    public RepositorioEventosH2(Connection conexion) {
        this.conexion = conexion;
        inicializarEsquema();
    }

    private void inicializarEsquema() {
        try (InputStream recurso = getClass().getClassLoader().getResourceAsStream("db/schema.sql")) {
            if (recurso == null) {
                throw new IllegalStateException("No se encontró db/schema.sql");
            }
            String contenido = new String(recurso.readAllBytes(), StandardCharsets.UTF_8);
            try (Statement sentencia = conexion.createStatement()) {
                for (String instruccion : contenido.split(";")) {
                    if (!instruccion.isBlank()) {
                        sentencia.execute(instruccion.trim());
                    }
                }
            }
        } catch (SQLException | IOException error) {
            throw new IllegalStateException("No fue posible inicializar el esquema H2", error);
        }
    }

    @Override
    public Evento buscar(String eventoId) {
        String consulta = "SELECT id, nombre, fecha, lugar, aforo, bloqueados FROM evento WHERE id = ?";
        try (PreparedStatement sentencia = conexion.prepareStatement(consulta)) {
            sentencia.setString(1, eventoId);
            try (ResultSet resultado = sentencia.executeQuery()) {
                if (!resultado.next()) {
                    return null;
                }
                String id = resultado.getString("id");
                List<Reserva> reservas = cargarReservas(id);
                return new Evento(id, resultado.getString("nombre"),
                        resultado.getTimestamp("fecha").toLocalDateTime(),
                        resultado.getString("lugar"), resultado.getInt("aforo"),
                        resultado.getInt("bloqueados"), reservas);
            }
        } catch (SQLException error) {
            throw new IllegalStateException("No fue posible consultar el evento " + eventoId, error);
        }
    }

    private List<Reserva> cargarReservas(String eventoId) throws SQLException {
        List<Reserva> reservas = new ArrayList<>();
        String sql = "SELECT r.codigo, r.estudiante_id, r.vence_en, r.estado, a.hora "
                + "FROM reserva r LEFT JOIN asistencia a ON a.reserva_codigo = r.codigo "
                + "WHERE r.evento_id = ? ORDER BY r.codigo";
        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setString(1, eventoId);
            try (ResultSet resultado = sentencia.executeQuery()) {
                while (resultado.next()) {
                    Timestamp hora = resultado.getTimestamp("hora");
                    Asistencia asistencia = null;
                    if (hora != null) {
                        asistencia = new Asistencia(hora.toLocalDateTime());
                    }
                    reservas.add(Reserva.reconstruir(resultado.getString("codigo"),
                            resultado.getString("estudiante_id"),
                            resultado.getTimestamp("vence_en").toLocalDateTime(),
                            EstadoReserva.valueOf(resultado.getString("estado")), asistencia));
                }
            }
        }
        return reservas;
    }

    @Override
    public void guardar(Evento evento) {
        if (evento == null) {
            throw new IllegalArgumentException("El evento no puede ser nulo.");
        }
        try {
            conexion.setAutoCommit(false);
            guardarEvento(evento);
            for (Reserva reserva : evento.getReservas()) {
                guardarReserva(evento.getId(), reserva);
                if (reserva.getAsistencia() != null) {
                    guardarAsistencia(reserva);
                }
            }
            conexion.commit();
        } catch (SQLException | RuntimeException error) {
            deshacer(error);
            throw new IllegalStateException("No fue posible guardar el evento y sus relaciones", error);
        } finally {
            restaurarAutoCommit();
        }
    }

    private void deshacer(Exception causa) {
        try {
            conexion.rollback();
        } catch (SQLException errorDeRollback) {
            causa.addSuppressed(errorDeRollback);
        }
    }

    private void restaurarAutoCommit() {
        try {
            conexion.setAutoCommit(true);
        } catch (SQLException error) {
            // La conexión ya no está disponible; no hay nada más que restaurar.
        }
    }

    private void guardarEvento(Evento evento) throws SQLException {
        String sql = "MERGE INTO evento (id, nombre, fecha, lugar, aforo, bloqueados) KEY(id) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setString(1, evento.getId());
            sentencia.setString(2, evento.getNombre());
            sentencia.setTimestamp(3, Timestamp.valueOf(evento.getFecha()));
            sentencia.setString(4, evento.getLugar());
            sentencia.setInt(5, evento.getAforo());
            sentencia.setInt(6, evento.getBloqueados());
            sentencia.executeUpdate();
        }
    }

    private void guardarReserva(String eventoId, Reserva reserva) throws SQLException {
        String sql = "MERGE INTO reserva (codigo, evento_id, estudiante_id, vence_en, estado) KEY(codigo) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setString(1, reserva.getCodigo());
            sentencia.setString(2, eventoId);
            sentencia.setString(3, reserva.getEstudianteId());
            sentencia.setTimestamp(4, Timestamp.valueOf(reserva.getVenceEn()));
            sentencia.setString(5, reserva.getEstado().name());
            sentencia.executeUpdate();
        }
    }

    private void guardarAsistencia(Reserva reserva) throws SQLException {
        String sql = "MERGE INTO asistencia (reserva_codigo, hora) KEY(reserva_codigo) VALUES (?, ?)";
        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setString(1, reserva.getCodigo());
            sentencia.setTimestamp(2, Timestamp.valueOf(reserva.getAsistencia().getHora()));
            sentencia.executeUpdate();
        }
    }
}