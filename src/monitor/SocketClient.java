package monitor;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;

class SocketClient {
    static private InetSocketAddress endpoint;

    SocketClient(InetSocketAddress end) {
        endpoint = end;
    }

    // get lights by JSON
    ArrayList<Light> getLights() throws Exception {
        ArrayList<Light> lights = null;
        String json;

        // generate socket
        Socket socket = new Socket();
        try {
            socket.connect(endpoint, 500);
        } catch (SocketException ignored) {}

        // setting
        OutputStreamWriter out = new OutputStreamWriter(socket.getOutputStream());
        BufferedWriter bw = new BufferedWriter(out);

        InputStreamReader in = new InputStreamReader(socket.getInputStream());
        BufferedReader br = new BufferedReader(in);

        // send command
        bw.write("GET_LIGHTS");
        bw.newLine();
        bw.flush();

        // receive message from server
        json = br.readLine();

        // close socket
        socket.close();

        // map to ArrayList from json
        ObjectMapper mapper = new ObjectMapper();
        lights = mapper.readValue(json, new TypeReference<ArrayList<Light>>() {});

        return lights;
    }

    public void setEndpoint(InetSocketAddress end) {
        endpoint = end;
    }

    public  InetSocketAddress getEndpoint() {
        return endpoint;
    }
}
