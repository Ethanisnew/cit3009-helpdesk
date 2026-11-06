package edu.utech.helpdesk.client;

import edu.utech.helpdesk.client.util.Async;
import edu.utech.helpdesk.client.util.Navigator;
import javafx.application.Application;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) {
        Navigator.init(stage);
        Navigator.showLogin();
        stage.show();
    }

    @Override
    public void stop() {
        Navigator.shutdown();
        Async.shutdown();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
