package monitor;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("monitor.fxml"));
        Parent root = fxmlLoader.load();
        primaryStage.setTitle("KC-101 Downlight Monitor");
        primaryStage.setScene(new Scene(root, 1100, 700));
        primaryStage.setOnCloseRequest(event -> System.exit(1));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
