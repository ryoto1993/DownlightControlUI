package monitor;

import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class Controller {
    private SocketClient socketClient;
    private ArrayList<Light> lights = null;

    @FXML
    TableView<Light> table;
    @FXML
    TableColumn col_id, col_lum, col_temp;

    public void initialize() {
        // start socket client
        socketClient = new SocketClient("localhost", 44344);
        lights = socketClient.getLights();

        // setting table
        col_id.setCellValueFactory(new PropertyValueFactory<Light, Integer>("id"));
        col_lum.setCellValueFactory(new PropertyValueFactory<Light, Double>("lumPct"));
        col_temp.setCellValueFactory(new PropertyValueFactory<Light, Integer>("temperature"));
        table.getItems().addAll(lights);

        // update table item every X mill seconds
        Updater updater = new Updater();
        Timer timer = new Timer();
        timer.schedule(updater, 1000, 500);


    }

    class Updater extends TimerTask {
        public void run() {
            ArrayList<Light> update = socketClient.getLights();
            for (Light l : update) {
                Light u = lights.get(l.getId() - 1);
                u.setLumPct(l.getLumPct());
                u.setTemperature(l.getTemperature());
                u.setSignal(l.getSignal());
            }
            // ToDo: have to fix table view to update...
            table.getItems().removeAll(lights);
            table.getItems().addAll(lights);
        }
    }

}
