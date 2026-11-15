package co.edu.unillanos.campusfest;

import javafx.application.Application;

/** Punto de entrada. Está separado de App para poder ejecutarse desde un jar. */
public class Launcher {

    public static void main(String[] args) {
        Application.launch(App.class, args);
    }
}