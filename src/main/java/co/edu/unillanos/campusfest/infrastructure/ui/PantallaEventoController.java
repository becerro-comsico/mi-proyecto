package co.edu.unillanos.campusfest.infrastructure.ui;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

import co.edu.unillanos.campusfest.application.usecase.EditarEvento;
import co.edu.unillanos.campusfest.domain.Evento;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

/** Pantalla del CU3: muestra los datos del evento y envía los cambios al caso de uso. */
public class PantallaEventoController {

    private final EditarEvento editarEvento;

    @FXML
    private TextField campoEventoId;

    @FXML
    private TextField campoNombre;

    @FXML
    private TextField campoFecha;

    @FXML
    private TextField campoLugar;

    @FXML
    private TextField campoAforo;

    @FXML
    private Label etiquetaMensaje;

    public PantallaEventoController(EditarEvento editarEvento) {
        this.editarEvento = editarEvento;
    }

    @FXML
    private void cargar() {
try {
            Evento evento = editarEvento.obtener(campoEventoId.getText().trim());
            campoNombre.setText(evento.getNombre());
            campoFecha.setText(evento.getFecha().toString());
            campoLugar.setText(evento.getLugar());
            campoAforo.setText(String.valueOf(evento.getAforo()));
            etiquetaMensaje.setText("Evento cargado.");
        } catch (IllegalArgumentException | IllegalStateException error) {
            etiquetaMensaje.setText(error.getMessage());
        }
    }

    @FXML
    private void guardar() {
        try {
            editarEvento.editar(campoEventoId.getText().trim(),
                    campoNombre.getText(),
                    LocalDateTime.parse(campoFecha.getText().trim()),
                    campoLugar.getText(),
                    Integer.parseInt(campoAforo.getText().trim()));
            etiquetaMensaje.setText("Evento actualizado.");
        } catch (DateTimeParseException error) {
            etiquetaMensaje.setText("La fecha debe tener el formato 2026-11-28T19:00.");
        } catch (NumberFormatException error) {
            etiquetaMensaje.setText("El aforo debe ser un número entero.");
        } catch (IllegalArgumentException | IllegalStateException error) {
            etiquetaMensaje.setText(error.getMessage());
        }
    }
}