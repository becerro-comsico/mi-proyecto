package co.edu.unillanos.campusfest.infrastructure.ui;

import co.edu.unillanos.campusfest.application.usecase.ConsultarCupos;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

/** Pantalla del CU1: solo lee el campo, llama al caso de uso y muestra el resultado. */
public class PantallaCuposController {

    private final ConsultarCupos consultarCupos;

    @FXML
    private TextField campoEventoId;

    @FXML
    private Label etiquetaCupos;

    @FXML
    private Label etiquetaMensaje;

    public PantallaCuposController(ConsultarCupos consultarCupos) {
        this.consultarCupos = consultarCupos;
    }

    @FXML
    private void consultar() {
        try {
            int cupos = consultarCupos.consultar(campoEventoId.getText().trim());
            etiquetaCupos.setText("Cupos disponibles: " + cupos);
            etiquetaMensaje.setText("");
        } catch (IllegalArgumentException | IllegalStateException error) {
            etiquetaCupos.setText("");
            etiquetaMensaje.setText(error.getMessage());
        }
    }
}