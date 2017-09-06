package monitor;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("monitor.fxml"));
        Parent root = fxmlLoader.load();
        Controller controller = fxmlLoader.getController();
        primaryStage.setTitle("KC-101 Downlight Monitor");
        primaryStage.setScene(new Scene(root, 1100, 700));
        primaryStage.setOnCloseRequest(event -> System.exit(1));
        primaryStage.widthProperty().addListener((observable, oldValue, newValue)
                -> controller.getCanvasControl().canvasResize());
        primaryStage.heightProperty().addListener((observable, oldValue, newValue)
                -> controller.getCanvasControl().canvasResize());
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
