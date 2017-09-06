package monitor;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.VPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.effect.BoxBlur;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class Controller {
    private SocketClient socketClient;
    private ArrayList<Light> lights = null;
    private final ObservableList<LightData> lightsList = FXCollections.observableArrayList();
    public CanvasControl canvasControl;

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

        // make CanvasControl instance
        canvasControl = new CanvasControl();
    }

    public CanvasControl getCanvasControl() {
        return canvasControl;
    }

    class CanvasControl {
        ArrayList<Boolean> selected = new ArrayList<>();
        GraphicsContext gc = canvas.getGraphicsContext2D();
        BoxBlur blur = new BoxBlur();  // for antialiasing
        double x = canvas.getWidth();
        double y = canvas.getHeight();
        double maxPosX = 0;  // max Light.posX
        double maxPosY = 0;  // max Light.posY
        double lightSizeX;
        double lightSizeY;

        CanvasControl() {
            for(Light l: lights) {
                // initialize of selected flags
                selected.add(false);

                // initialize min/max position
                maxPosX = l.getPosX() > maxPosX ? l.getPosX() : maxPosX;
                maxPosY = l.getPosY() > maxPosY ? l.getPosY() : maxPosY;
            }
            // normalize min/max position
            maxPosX++;
            maxPosY++;

            // determine light size
            lightSizeX = 0.9 / maxPosX / 2.5;
            lightSizeY = 0.9 / maxPosY / 2.5;

            // antialiasing
            blur.setWidth(1);
            blur.setHeight(1);
            blur.setIterations(1);
            gc.setEffect(blur);
        }

        // canvas resize
        void canvasResize() {
            double size = canvas_pane.getHeight() > canvas_pane.getWidth()
                    ? canvas_pane.getWidth(): canvas_pane.getHeight();
            canvas.setHeight(size);
            canvas.setWidth(size);
            canvas.setLayoutX(canvas_pane.getWidth()/2 - size/2);
            canvas.setLayoutY(canvas_pane.getHeight()/2 - size/2);

            // update canvas size variable
            this.x = size;
            this.y = size;

            // repaint
            repaintCanvas();
        }

        void repaintCanvas() {
            // clear
            gc.setEffect(null);
            gc.clearRect(0, 0, x, y);
            gc.setEffect(blur);

            // draw room layout
            drawRoom();

            // draw lights
            drawLights();

            // draw ID
            drawIDs();

            // draw lumPct
            drawPct();

            // draw colorTemp
            drawTemperature();
        }

        void drawRoom() {
            gc.setLineWidth(0.5);
            gc.setStroke(Color.CYAN);
            gc.strokeRect(
                    pctToX(0.05), pctToY(0.05),
                    pctToX(0.90), pctToY(0.90)
            );
        }

        void drawLights() {
            for(Light l: lights) {
                // fill lights with temp and lumPct
                Color color = ImageUtils.getRGBFromK((int)l.getTemperature());
                double opacity = l.getLumPct() > 0 ? 0.2 + l.getLumPct()/100 * 0.8 : 0;
                color = Color.color(color.getRed(), color.getGreen(), color.getBlue(), opacity);
                gc.setFill(color);
                gc.fillOval(
                        pctToX(0.05 + (l.getPosX() + 0.5)*(0.9/maxPosX) - lightSizeX/2),
                        pctToY(0.05 + (l.getPosY() + 0.5)*(0.9/maxPosY) - lightSizeY/2),
                        pctToX(lightSizeX),
                        pctToY(lightSizeY)
                );

                // draw stroke line
                if(selected.get(l.getId()-1)) {
                    gc.setLineWidth(1.0);
                    gc.setStroke(Color.LIMEGREEN);
                } else {
                    gc.setLineWidth(0.5);
                    gc.setStroke(Color.LIGHTGRAY);
                }
                gc.strokeOval(
                        pctToX(0.05 + (l.getPosX() + 0.5)*(0.9/maxPosX) - lightSizeX/2),
                        pctToY(0.05 + (l.getPosY() + 0.5)*(0.9/maxPosY) - lightSizeY/2),
                        pctToX(lightSizeX),
                        pctToY(lightSizeY)
                );
            }
        }

        void drawIDs() {
            for(Light l: lights) {
                // draw stroke line
                double f = pctToX(lightSizeX/2);
                gc.setFont(Font.font(f));
                gc.setTextAlign(TextAlignment.CENTER);
                gc.setTextBaseline(VPos.CENTER);
                gc.setFill(Color.GRAY);
                gc.fillText(
                        String.valueOf(l.getId()),
                        pctToX(0.05 + (l.getPosX() + 0.5)*(0.9/maxPosX)),
                        pctToY(0.05 + (l.getPosY() + 0.5)*(0.9/maxPosY)),
                        pctToX(lightSizeX/2));
            }
        }

        void drawPct() {
            for(Light l: lights) {
                // draw stroke line
                double f = pctToX(lightSizeX/3);
                gc.setFont(Font.font(f));
                gc.setTextAlign(TextAlignment.CENTER);
                gc.setTextBaseline(VPos.CENTER);
                gc.setFill(Color.LIGHTGRAY);
                gc.fillText(
                        String.valueOf(l.getLumPct() + " %"),
                        pctToX(0.05 + (l.getPosX() + 0.5)*(0.9/maxPosX)),
                        pctToY(0.05 + (l.getPosY() + 0.5)*(0.9/maxPosY) + lightSizeY/2 + lightSizeY/8 * 2)
                );
            }
        }

        void drawTemperature() {
            for(Light l: lights) {
                // draw stroke line
                double f = pctToX(lightSizeX/3);
                gc.setFont(Font.font(f));
                gc.setTextAlign(TextAlignment.CENTER);
                gc.setTextBaseline(VPos.CENTER);
                gc.setFill(Color.LIGHTGRAY);
                gc.fillText(
                        String.valueOf((int)l.getTemperature() + " K"),
                        pctToX(0.05 + (l.getPosX() + 0.5)*(0.9/maxPosX)),
                        pctToY(0.05 + (l.getPosY() + 0.5)*(0.9/maxPosY) + lightSizeY/2 + lightSizeY/8 * 5)
                );
            }
        }

        private double pctToX(double x_pct) {return x * x_pct;}

        private double pctToY(double y_pct) {return y * y_pct;}

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

                // repaint canvas
                canvasControl.repaintCanvas();

            } catch (Exception ignored) {}
        }
    }

}
