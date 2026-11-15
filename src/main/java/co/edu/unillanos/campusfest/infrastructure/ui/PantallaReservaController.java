package co.edu.unillanos.campusfest.infrastructure.ui;

import co.edu.unillanos.campusfest.application.usecase.ReservarEntrada;
import co.edu.unillanos.campusfest.domain.Reserva;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

/** Pantalla del CU2: lee los campos, llama al caso de uso y muestra el resultado. */
public class PantallaReservaController {

    private final ReservarEntrada reservarEntrada;

    @FXML
    private TextField campoEventoId;

    @FXML
    private TextField campoEstudianteId;

    @FXML
    private TextField campoCodigoReserva;

    @FXML
    private Label etiquetaMensaje;

    public PantallaReservaController(ReservarEntrada reservarEntrada) {
        this.reservarEntrada = reservarEntrada;
    }

    @FXML
    private void solicitar() {
        try {
            Reserva reserva = reservarEntrada.solicitarReserva(
                    campoEventoId.getText().trim(), campoEstudianteId.getText().trim());
            campoCodigoReserva.setText(reserva.getCodigo());
            etiquetaMensaje.setText("Reserva creada. Confírmela antes de las " + reserva.getVenceEn());
        } catch (IllegalArgumentException | IllegalStateException error) {
            etiquetaMensaje.setText(error.getMessage());
        }
    }

    @FXML
    private void confirmar() {
        try {
            Reserva reserva = reservarEntrada.confirmarReserva(
                    campoEventoId.getText().trim(), campoCodigoReserva.getText().trim());
            etiquetaMensaje.setText("Reserva confirmada. Código QR: " + reserva.getCodigo());
        } catch (IllegalArgumentException | IllegalStateException error) {
            etiquetaMensaje.setText(error.getMessage());
        }
    }
}