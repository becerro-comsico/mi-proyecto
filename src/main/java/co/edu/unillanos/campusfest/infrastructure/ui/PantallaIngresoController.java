package co.edu.unillanos.campusfest.infrastructure.ui;

import co.edu.unillanos.campusfest.application.usecase.RegistrarIngreso;
import co.edu.unillanos.campusfest.domain.Asistencia;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

/** Pantalla del CU4: lee el código QR, llama al caso de uso y muestra el resultado. */
public class PantallaIngresoController {

    private final RegistrarIngreso registrarIngreso;

    @FXML
    private TextField campoEventoId;

    @FXML
    private TextField campoCodigoQr;

    @FXML
    private Label etiquetaMensaje;

    public PantallaIngresoController(RegistrarIngreso registrarIngreso) {
        this.registrarIngreso = registrarIngreso;
    }

    @FXML
    private void registrar() {
        try {
            Asistencia asistencia = registrarIngreso.registrar(
                    campoEventoId.getText().trim(), campoCodigoQr.getText().trim());
            etiquetaMensaje.setText("Ingreso registrado a las " + asistencia.getHora());
        } catch (IllegalArgumentException | IllegalStateException error) {
            etiquetaMensaje.setText(error.getMessage());
        }
    }
}