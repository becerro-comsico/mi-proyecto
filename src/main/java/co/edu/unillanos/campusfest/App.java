package co.edu.unillanos.campusfest;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.time.LocalDateTime;

import co.edu.unillanos.campusfest.application.port.Reloj;
import co.edu.unillanos.campusfest.application.port.RepositorioEventos;
import co.edu.unillanos.campusfest.application.usecase.ConsultarCupos;
import co.edu.unillanos.campusfest.application.usecase.EditarEvento;
import co.edu.unillanos.campusfest.application.usecase.RegistrarIngreso;
import co.edu.unillanos.campusfest.application.usecase.ReservarEntrada;
import co.edu.unillanos.campusfest.domain.Evento;
import co.edu.unillanos.campusfest.infrastructure.persistence.RepositorioEventosH2;
import co.edu.unillanos.campusfest.infrastructure.reloj.RelojDelSistema;
import co.edu.unillanos.campusfest.infrastructure.ui.PantallaCuposController;
import co.edu.unillanos.campusfest.infrastructure.ui.PantallaEventoController;
import co.edu.unillanos.campusfest.infrastructure.ui.PantallaIngresoController;
import co.edu.unillanos.campusfest.infrastructure.ui.PantallaReservaController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;

/** Ensambla la aplicación: crea los adaptadores, los casos de uso y los controladores. */
public class App extends Application {

    private static final String ID_EVENTO_EJEMPLO = "taller-1";

    private Connection conexion;

    @Override
    public void start(Stage ventana) throws Exception {
        conexion = DriverManager.getConnection("jdbc:h2:./datos/campusfest");
        RepositorioEventos repositorio = new RepositorioEventosH2(conexion);
        Reloj reloj = new RelojDelSistema();
        crearEventoDeEjemplo(repositorio);

        ConsultarCupos consultarCupos = new ConsultarCupos(repositorio, reloj);
        ReservarEntrada reservarEntrada = new ReservarEntrada(repositorio, reloj);
        EditarEvento editarEvento = new EditarEvento(repositorio, reloj);
        RegistrarIngreso registrarIngreso = new RegistrarIngreso(repositorio, reloj);

        TabPane pestanas = new TabPane(
                pestana("Cupos", cargar("pantalla-cupos.fxml",
                        new PantallaCuposController(consultarCupos))),
                pestana("Reservar", cargar("pantalla-reserva.fxml",
                        new PantallaReservaController(reservarEntrada))),
                pestana("Evento", cargar("pantalla-evento.fxml",
                        new PantallaEventoController(editarEvento))),
                pestana("Ingreso", cargar("pantalla-ingreso.fxml",
                        new PantallaIngresoController(registrarIngreso))));

        ventana.setTitle("CampusFest");
        ventana.setScene(new Scene(pestanas, 520, 460));
        ventana.show();
    }

    @Override
    public void stop() throws Exception {
        if (conexion != null) {
            conexion.close();
        }
    }

    private Parent cargar(String vista, Object controlador) throws IOException {
        FXMLLoader cargador = new FXMLLoader(getClass().getResource("vistas/" + vista));
        cargador.setController(controlador);
        return cargador.load();
    }

    private Tab pestana(String titulo, Parent contenido) {
        Tab pestana = new Tab(titulo, contenido);
        pestana.setClosable(false);
        return pestana;
    }

    private void crearEventoDeEjemplo(RepositorioEventos repositorio) {
        if (repositorio.buscar(ID_EVENTO_EJEMPLO) == null) {
            repositorio.guardar(new Evento(ID_EVENTO_EJEMPLO, "Taller CampusFest",
                    LocalDateTime.of(2026, 11, 28, 19, 0), "Auditorio", 20, 2));
        }
    }
}