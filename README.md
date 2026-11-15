# CampusFest

Aplicación JavaFX para gestionar las entradas de eventos universitarios. Proyecto de semestre, Universidad de los Llanos.

## Integrantes

- Emmanuel Fernando Camacho Cruz - código estudiantil: 160004908
- David Fernando Arias Sanchez - código estudiantil: 160004902

## Cómo ejecutar

Requisitos: Java 21 y Maven.
mvn test # ejecuta las pruebas
mvn javafx:run # abre la aplicación

La aplicación guarda sus datos en una base H2 y, al iniciar, crea un evento de ejemplo (código `taller-1`, aforo 20) para poder probar los casos de uso.

## Casos de uso implementados

1. **CU1 Consultar cupos**: muestra los cupos disponibles de un evento.
2. **CU2 Reservar entrada**: solicita una reserva (plazo de 10 minutos) y la confirma.
3. **CU3 Editar evento**: cambia nombre, fecha, lugar y aforo, sin dejar el aforo por debajo de lo ocupado.
4. **CU4 Registrar ingreso**: registra la asistencia con el código de una reserva confirmada.

## Arquitectura

Arquitectura limpia con tres paquetes: `domain` (entidades con sus reglas), `application` (puertos y casos de uso) e `infrastructure` (H2, memoria, reloj y pantallas JavaFX). Solo `App` crea las implementaciones de los puertos.

## Declaración de uso de IA

Se usó **Claude (Anthropic)** como asistente para:
- Armar el `pom.xml` y la estructura de paquetes.
- Escribir las entidades del dominio (`Evento`, `Reserva`, `Asistencia`, `EstadoReserva`), los puertos, los casos de uso, `RelojDelSistema`, los FXML y los controladores de las pantallas, guiándose por `CONTRATO.md` y las pruebas entregadas.

Todo el código se revisó, se ejecutó con `mvn test` y se integró por pull requests.

**Propuesta de la IA que violaba el contrato y cómo se corrigió.** La IA generó los FXML de las pantallas con `fx:id` en los botones, sin declarar el campo `@FXML` con el mismo nombre en el controlador. `CONTRATO.md` exige que cada `fx:id` sea un campo del controlador, así que `PantallasTest` habría fallado. Se detectó al comparar con el contrato y se corrigió quitando esos `fx:id`, porque los botones ya se activan con `onAction`. Commit de la corrección: `8e4c626` (`fix(ui): quitar fx:id de los botones...`, PR `fix/pantallas-fxid`).