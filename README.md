# CampusFest

Aplicación JavaFX para gestionar las entradas de los eventos universitarios: consultar cupos, reservar y confirmar una entrada, editar el evento y registrar el ingreso con el código QR. Proyecto de semestre, Universidad de los Llanos.

## Integrantes

- Emmanuel Fernando Camacho Cruz - código estudiantil: 160004908
- David Fernando Arias Sanchez - código estudiantil: 160004902

## Cómo ejecutar

Requisitos: Java 21 y Maven. La primera vez Maven descarga las dependencias.

```
mvn test          # ejecuta las pruebas
mvn javafx:run    # abre la aplicación
```

Al iniciar, la aplicación abre una base H2 en la carpeta `datos/` (ignorada por Git) y crea un evento de ejemplo: código `taller-1`, aforo 20 y 2 cupos bloqueados.

## Cómo probar la aplicación

La ventana tiene una pestaña por caso de uso:

1. **Cupos**: escribe `taller-1` y pulsa Consultar. Muestra 18 cupos disponibles.
2. **Reservar**: escribe `taller-1` y un código de estudiante, y pulsa Solicitar reserva. El código de la reserva aparece en el campo de abajo. Pulsa Confirmar antes de que pasen 10 minutos.
3. **Evento**: carga `taller-1`, cambia nombre, fecha, lugar o aforo, y pulsa Guardar. Si el aforo queda por debajo de los cupos ocupados, la pantalla muestra el motivo.
4. **Ingreso**: escribe `taller-1` y el código de una reserva confirmada. Un segundo ingreso con el mismo código se rechaza.

## Casos de uso implementados

| Caso de uso | Clase | Pantalla |
| --- | --- | --- |
| CU1 Consultar cupos | `ConsultarCupos` | `pantalla-cupos.fxml` |
| CU2 Reservar y confirmar una entrada | `ReservarEntrada` | `pantalla-reserva.fxml` |
| CU3 Editar evento | `EditarEvento` | `pantalla-evento.fxml` |
| CU4 Registrar el ingreso con QR | `RegistrarIngreso` | `pantalla-ingreso.fxml` |

## Reglas de negocio

- **Disponibilidad:** cupos libres = aforo - bloqueados - confirmadas - pendientes vigentes. Las canceladas y vencidas no ocupan cupo.
- **Plazo:** una reserva pendiente vence a los 10 minutos; a los 10:00 exactos ya está vencida.
- **Estados:** PENDIENTE pasa a CONFIRMADA o VENCIDA; PENDIENTE o CONFIRMADA pasan a CANCELADA.
- **Aforo:** no puede bajar de los cupos que ya están ocupados.
- **Ingreso:** un solo ingreso por reserva confirmada, con el código QR de esa reserva.

## Arquitectura

Arquitectura limpia: las dependencias apuntan hacia adentro.

| Anillo | Paquete | Contenido |
| --- | --- | --- |
| Entidades | `domain` | `Evento`, `Reserva`, `EstadoReserva`, `Asistencia` |
| Casos de uso | `application.usecase` | los cuatro casos de uso |
| Puertos | `application.port` | `RepositorioEventos`, `Reloj` |
| Adaptadores | `infrastructure.persistence`, `infrastructure.ui`, `infrastructure.reloj` | repositorios H2 y en memoria, controladores JavaFX, `RelojDelSistema` |
| Frameworks y drivers | `co.edu.unillanos.campusfest` y `resources` | `App`, `Launcher`, FXML, CSS y `schema.sql` |

Solo `App` crea las implementaciones de los puertos. Para guardar en memoria en vez de H2 solo cambia `App` (`new RepositorioEventosEnMemoria()` en lugar de `new RepositorioEventosH2(conexion)`); las entidades y los casos de uso no se tocan.

## Pruebas

Las 90 pruebas entregadas por los docentes están en `src/test/java`, sin modificar: dominio, casos de uso, repositorio en memoria, repositorio H2, pantallas y regla de dependencia. Se ejecutan con `mvn test`.

## Declaración de uso de IA

Se usó **Claude (Anthropic)** como asistente para:
- Armar el `pom.xml` y la estructura de paquetes.
- Escribir las entidades del dominio (`Evento`, `Reserva`, `Asistencia`, `EstadoReserva`), los puertos, los casos de uso, `RelojDelSistema`, los FXML y los controladores de las pantallas, `App` y `Launcher`, guiándose por `CONTRATO.md` y las pruebas entregadas.
- Ajustar `RepositorioEventosH2` para que reciba la `Connection`, como pide el contrato.
- Revisar el repositorio contra la rúbrica y proponer mejoras (por ejemplo, la regla de vigencia de `Reserva`).

David usó **ChatGPT (OpenAI)** para la capa de infraestructura: `RepositorioEventosEnMemoria` (con un `HashMap`), `RepositorioEventosH2` (JDBC para guardar y recuperar la información) y `schema.sql` (tablas y relaciones).

Todo el código se revisó, se ejecutó con `mvn test` y se integró por pull requests.

**Propuesta de la IA que violaba el contrato y cómo se corrigió.** La IA generó los FXML de las pantallas con `fx:id` en los botones, sin declarar el campo `@FXML` con el mismo nombre en el controlador. `CONTRATO.md` exige que cada `fx:id` sea un campo del controlador, así que `PantallasTest` habría fallado. Se detectó al comparar con el contrato y se corrigió quitando esos `fx:id`, porque los botones ya se activan con `onAction`. Commit de la corrección: `8e4c626` (`fix(ui): quitar fx:id de los botones...`, PR `fix/pantallas-fxid`).

## Trabajo en equipo

Cada cambio entra a `main` por una rama y un pull request, y lo mezcla el otro integrante. Los commits describen qué cambió.