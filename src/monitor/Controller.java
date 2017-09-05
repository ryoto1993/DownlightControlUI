package monitor;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class Controller {
    private SocketClient socketClient;
    private ArrayList<Light> lights = null;
    private final ObservableList<LightData> lightsList = FXCollections.observableArrayList();

    @FXML
    TableView<LightData> table;
    @FXML
    TableColumn<Object, Object> col_id;
    @FXML
    TableColumn<Object, Object> col_lum;
    @FXML
    TableColumn<Object, Object> col_temp;
    @FXML
    Pane canvas_pane;
    @FXML
    Canvas canvas;

    public void initialize() {
        // start socket client
        socketClient = new SocketClient(
                new InetSocketAddress("localhost", 44344));

        // get lights data from server
        lights = socketClient.getLights();
        for(Light l: lights) {
            lightsList.add(new LightData(l.getId(), l.getLumPct(), l.getTemperature()));
        }

        // setting table
        col_id.setCellValueFactory(new PropertyValueFactory<>("id"));
        col_lum.setCellValueFactory(new PropertyValueFactory<>("lumPct"));
        col_temp.setCellValueFactory(new PropertyValueFactory<>("temperature"));
        table.setItems(lightsList);

        // update table item every X mill seconds
        LightUpdater lightUpdater = new LightUpdater();
        Timer timer = new Timer();
        timer.schedule(lightUpdater, 1000, 500);

    }

    // canvas resize
    void canvasResize() {
        double size = canvas_pane.getHeight() > canvas_pane.getWidth()
                ? canvas_pane.getWidth(): canvas_pane.getHeight();
        canvas.setHeight(size);
        canvas.setWidth(size);
        canvas.setLayoutX(canvas_pane.getWidth()/2 - size/2);
        canvas.setLayoutY(canvas_pane.getHeight()/2 - size/2);

        // for debug
        canvas_pane.setStyle("-fx-background-color: #9b95ff");
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setFill(Color.RED);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }

    class LightUpdater extends TimerTask {
        public void run() {
            try {
                ArrayList<Light> update = socketClient.getLights();
                for (Light l : update) {
                    Light u = lights.get(l.getId() - 1);
                    LightData ud = lightsList.get(l.getId() - 1);
                    u.setLumPct(l.getLumPct());
                    u.setTemperature(l.getTemperature());
                    u.setSignals(l.getSignals());
                    ud.setLumPct(l.getLumPct());
                    ud.setTemperature(l.getTemperature());
                }
            } catch (Exception ignored) {}
        }
    }

}
