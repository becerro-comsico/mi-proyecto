package co.edu.unillanos.campusfest.infrastructure.reloj;

import java.time.LocalDateTime;

import co.edu.unillanos.campusfest.application.port.Reloj;

/** La única clase del proyecto que lee la hora real del sistema. */
public class RelojDelSistema implements Reloj {

    @Override
    public LocalDateTime ahora() {
        return LocalDateTime.now();
    }
}