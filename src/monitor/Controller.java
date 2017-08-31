package monitor;

import java.util.ArrayList;

public class Controller {
    private SocketClient socketClient;

    private ArrayList<Light> lights = null;

    public void initialize() {
        // start socket client
        socketClient = new SocketClient("localhost", 44344);
        lights = socketClient.getLights();
    }

}
