CREATE TABLE IF NOT EXISTS evento (
    id VARCHAR(255) PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL,
    fecha TIMESTAMP NOT NULL,
    lugar VARCHAR(255) NOT NULL,
    aforo INTEGER NOT NULL CHECK (aforo >= 0),
    bloqueados INTEGER NOT NULL CHECK (bloqueados >= 0 AND bloqueados <= aforo)
);

CREATE TABLE IF NOT EXISTS reserva (
    codigo VARCHAR(255) PRIMARY KEY,
    evento_id VARCHAR(255) NOT NULL,
    estudiante_id VARCHAR(255) NOT NULL,
    vence_en TIMESTAMP NOT NULL,
    estado VARCHAR(32) NOT NULL CHECK (estado IN ('PENDIENTE', 'CONFIRMADA', 'VENCIDA', 'CANCELADA')),
    CONSTRAINT fk_reserva_evento FOREIGN KEY (evento_id) REFERENCES evento(id)
);

CREATE TABLE IF NOT EXISTS asistencia (
    reserva_codigo VARCHAR(255) PRIMARY KEY,
    hora TIMESTAMP NOT NULL,
    CONSTRAINT fk_asistencia_reserva FOREIGN KEY (reserva_codigo) REFERENCES reserva(codigo)
);
