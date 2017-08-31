package monitor;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SocketClient {
    static private int port;
    static private String hostName;
    private Socket socket;

    public SocketClient(String host, int port) {
        this.hostName = host;
        this.port = port;
    }

    // get lights by JSON
    public ArrayList<Light> getLights() {
        ArrayList<Light> lights = null;
        String json = null;

        try {
            // generate socket
            InetSocketAddress endpoint = new InetSocketAddress(hostName, port);
            socket = new Socket();
            socket.connect(endpoint);

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

            socket.close();

            // map to ArrayList from json
            ObjectMapper mapper = new ObjectMapper();
            lights = mapper.readValue(json, new TypeReference<ArrayList<Light>>() {});

            for(Light l: lights) {
                System.out.println(l.getId() + ", LumPct: " + l.getLumPct());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return lights;
    }

}
