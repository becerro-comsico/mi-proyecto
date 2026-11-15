package co.edu.unillanos.campusfest.infrastructure.persistence;

import co.edu.unillanos.campusfest.application.port.RepositorioEventos;
import co.edu.unillanos.campusfest.domain.Asistencia;
import co.edu.unillanos.campusfest.domain.EstadoReserva;
import co.edu.unillanos.campusfest.domain.Evento;
import co.edu.unillanos.campusfest.domain.Reserva;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/** Adaptador JDBC H2. Cada guardar es una transacción atómica. */
public class RepositorioEventosH2 implements RepositorioEventos {
    private final String jdbcUrl;
    private final String usuario;
    private final String clave;

    public RepositorioEventosH2(String jdbcUrl) {
        this(jdbcUrl, "sa", "");
    }

    public RepositorioEventosH2(String jdbcUrl, String usuario, String clave) {
        this.jdbcUrl = jdbcUrl;
        this.usuario = usuario;
        this.clave = clave;
        inicializarEsquema();
    }

    private Connection conectar() throws SQLException {
        return DriverManager.getConnection(jdbcUrl, usuario, clave);
    }

    private void inicializarEsquema() {
        try (InputStream recurso = getClass().getClassLoader().getResourceAsStream("db/schema.sql")) {
            if (recurso == null) throw new IllegalStateException("No se encontró db/schema.sql");
            String contenido = new String(recurso.readAllBytes(), StandardCharsets.UTF_8);
            try (Connection conexion = conectar(); Statement sentencia = conexion.createStatement()) {
                for (String instruccion : contenido.split(";")) {
                    if (!instruccion.isBlank()) sentencia.execute(instruccion.trim());
                }
            }
        } catch (SQLException | IOException e) {
            throw new IllegalStateException("No fue posible inicializar el esquema H2", e);
        }
    }

    @Override
    public Evento buscar(String eventoId) {
        String consulta = "SELECT id, nombre, fecha, lugar, aforo, bloqueados FROM evento WHERE id = ?";
        try (Connection conexion = conectar();
             PreparedStatement sentencia = conexion.prepareStatement(consulta)) {
            sentencia.setString(1, eventoId);
            try (ResultSet resultado = sentencia.executeQuery()) {
                if (!resultado.next()) return null;
                String id = resultado.getString("id");
                String nombre = resultado.getString("nombre");
                Timestamp fecha = resultado.getTimestamp("fecha");
                String lugar = resultado.getString("lugar");
                int aforo = resultado.getInt("aforo");
                int bloqueados = resultado.getInt("bloqueados");
                List<Reserva> reservas = cargarReservas(conexion, id);
                return new Evento(id, nombre, fecha.toLocalDateTime(), lugar, aforo, bloqueados, reservas);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("No fue posible consultar el evento " + eventoId, e);
        }
    }

    private List<Reserva> cargarReservas(Connection conexion, String eventoId) throws SQLException {
        List<Reserva> reservas = new ArrayList<>();
        String sql = "SELECT r.codigo, r.estudiante_id, r.vence_en, r.estado, a.hora "
                + "FROM reserva r LEFT JOIN asistencia a ON a.reserva_codigo = r.codigo "
                + "WHERE r.evento_id = ? ORDER BY r.codigo";
        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setString(1, eventoId);
            try (ResultSet resultado = sentencia.executeQuery()) {
                while (resultado.next()) {
                    Timestamp hora = resultado.getTimestamp("hora");
                    Asistencia asistencia = hora == null ? null : new Asistencia(hora.toLocalDateTime());
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
        if (evento == null) throw new IllegalArgumentException("El evento no puede ser nulo.");
        try (Connection conexion = conectar()) {
            conexion.setAutoCommit(false);
            try {
                guardarEvento(conexion, evento);
                for (Reserva reserva : evento.getReservas()) {
                    guardarReserva(conexion, evento.getId(), reserva);
                    if (reserva.getAsistencia() != null) guardarAsistencia(conexion, reserva);
                }
                conexion.commit();
            } catch (SQLException | RuntimeException e) {
                try { conexion.rollback(); } catch (SQLException rollbackError) { e.addSuppressed(rollbackError); }
                throw new IllegalStateException("No fue posible guardar el evento y sus relaciones", e);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("No fue posible conectar con H2", e);
        }
    }

    private void guardarEvento(Connection conexion, Evento evento) throws SQLException {
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

    private void guardarReserva(Connection conexion, String eventoId, Reserva reserva) throws SQLException {
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

    private void guardarAsistencia(Connection conexion, Reserva reserva) throws SQLException {
        String sql = "MERGE INTO asistencia (reserva_codigo, hora) KEY(reserva_codigo) VALUES (?, ?)";
        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setString(1, reserva.getCodigo());
            sentencia.setTimestamp(2, Timestamp.valueOf(reserva.getAsistencia().getHora()));
            sentencia.executeUpdate();
        }
    }
}
