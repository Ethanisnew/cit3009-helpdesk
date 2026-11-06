package edu.utech.helpdesk.client;

/**
 * Plain main class so the app can be started from an IDE or a fat jar without JavaFX module-path flags.
 */
public final class Launcher {
    private Launcher() {
    }

    public static void main(String[] args) {
        MainApp.main(args);
    }
}
