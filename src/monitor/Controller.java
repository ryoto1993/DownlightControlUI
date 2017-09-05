package monitor;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

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
        Updater updater = new Updater();
        Timer timer = new Timer();
        timer.schedule(updater, 1000, 500);


    }

    class Updater extends TimerTask {
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
