package monitor;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Controller {
    private Stage parentStage;
    private SocketClient socketClient;
    private ArrayList<Light> lights = null;
    private final ObservableList<LightData> lightsList = FXCollections.observableArrayList();
    private CanvasControl canvasControl;

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
    Pane group_pane;
    @FXML
    CheckMenuItem menu_show_id, menu_show_lumPct, menu_show_cct;
    @FXML
    MenuItem menu_exit, menu_png, menu_about;
    @FXML
    Text status_line;

    public void initialize() {
        // start socket client
        InetSocketAddress endpoint = new InetSocketAddress("172.20.11.53", 44344);
        socketClient = new SocketClient(endpoint);

        // get lights data from server
        while (true) {
            try {
                lights = socketClient.getLights();
                break;
            } catch (Exception e) {
                if (e.getClass().equals(SocketTimeoutException.class)) {
                    showChangeHostDialog();
                } else {
                    showExceptionDialog(e);
                }
            }
        }

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
        timer.schedule(lightUpdater, 500, 1000);

        // make CanvasControl instance
        canvasControl = new CanvasControl();

        // set event handler to menu items
        setEventHandler();

        // edit status line
        status_line.setText("Listening...  [ Host: " + endpoint.getHostName()
                        + ", Port: " + endpoint.getPort() + " ]");
    }

    CanvasControl getCanvasControl() {
        return canvasControl;
    }

    void setParentStage(Stage stage) {
        parentStage = stage;
    }

    private void setEventHandler() {
        menu_exit.setOnAction(event -> System.exit(10));

        menu_png.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();

            //Set extension filter
            FileChooser.ExtensionFilter extFilter =
                    new FileChooser.ExtensionFilter("png files (*.png)", "*.png");
            fileChooser.getExtensionFilters().add(extFilter);

            //Show save file dialog
            File file = fileChooser.showSaveDialog(parentStage);

            if(file != null){
                try {
                    WritableImage writableImage = new WritableImage((int)group_pane.getWidth(), (int)group_pane.getHeight());
                    group_pane.snapshot(null, writableImage);
                    RenderedImage renderedImage = SwingFXUtils.fromFXImage(writableImage, null);
                    ImageIO.write(renderedImage, "png", file);
                } catch (IOException ex) {
                    Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });

        menu_about.setOnAction(event -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("KC-101 Downlight Monitor");
            alert.setHeaderText("KC-101 Downlight Monitor");
            alert.setContentText("This application is coded by Ryoto Tomioka @ISDL 20th." +
                    "If there is some question, please ask me.\n\n" +
                    "Contact: tommy.ryoto93@gmail.com");
            alert.showAndWait();
        });

        menu_show_id.setOnAction(event -> {
            Boolean selected = menu_show_id.isSelected();
            for (Text t: canvasControl.shapeID) {
                t.setVisible(selected);
            }
        });
        menu_show_cct.setOnAction(event -> {
            Boolean selected = menu_show_cct.isSelected();
            for (Text t: canvasControl.shapeTemp) {
                t.setVisible(selected);
            }
        });
        menu_show_lumPct.setOnAction(event -> {
            Boolean selected = menu_show_lumPct.isSelected();
            for (Text t: canvasControl.shapeLum) {
                t.setVisible(selected);
            }
        });

    }

    private void showExceptionDialog(Exception ex) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Exception occurred!!");
        alert.setHeaderText("Oops! Exception has been occurred.");
        alert.setContentText(ex.getLocalizedMessage());

        // Create expandable Exception.
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        String exceptionText = sw.toString();

        Label label = new Label("The exception stacktrace was:");

        TextArea textArea = new TextArea(exceptionText);
        textArea.setEditable(false);
        textArea.setWrapText(true);

        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);

        GridPane expContent = new GridPane();
        expContent.setMaxWidth(Double.MAX_VALUE);
        expContent.add(label, 0, 0);
        expContent.add(textArea, 0, 1);

        // Set expandable Exception into the dialog pane.
        alert.getDialogPane().setExpandableContent(expContent);

        alert.showAndWait();
    }

    private void showChangeHostDialog() {
        TextInputDialog dialog = new TextInputDialog("172.20.11.53");
        dialog.setTitle("Socket Timeout Exception has been occurred");
        dialog.setHeaderText("Socket connection timeout.\n" +
                "Please check server's hostname and enter the new hostname.");
        dialog.setContentText("New hostname:");

        // Traditional way to get the response value.
        Optional<String> result = dialog.showAndWait();

        if (result.isPresent()) {
            socketClient.setEndpoint(new InetSocketAddress(result.get(), 44344));
        } else {
            System.exit(500);
        }
    }

    class CanvasControl {
        // Shapes
        Rectangle shapeRoom;
        ArrayList<Ellipse> shapeLights = new ArrayList<>();
        ArrayList<VBox> vboxID = new ArrayList<>();
        ArrayList<VBox> vboxInfo = new ArrayList<>();
        ArrayList<Text> shapeID = new ArrayList<>();
        ArrayList<Text> shapeLum = new ArrayList<>();
        ArrayList<Text> shapeTemp = new ArrayList<>();

        // light selected flag
        ArrayList<Boolean> selected = new ArrayList<>();

        double x;
        double y;
        double maxPosX = 0;  // max of Light.posX
        double maxPosY = 0;  // max of Light.posY
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
            lightSizeX = 0.9 / maxPosX / 4.0;
            lightSizeY = 0.9 / maxPosY / 4.0;

            // initialize shapes
            initShape();

            // set mouse listener
            setMouseListener();
        }

        void initShape() {
            // Room shape
            shapeRoom = new Rectangle();
            shapeRoom.setStrokeWidth(0.5);
            shapeRoom.setStroke(Color.CYAN);
            shapeRoom.setFill(null);
            group_pane.getChildren().add(shapeRoom);

            // Lights shape
            for (int i=0; i<lights.size(); i++) {
                Ellipse c = new Ellipse();
                shapeLights.add(c);
                group_pane.getChildren().add(c);
            }

            // info Texts
            for (int i=0; i<lights.size(); i++) {
                // generate ID VBox
                VBox vID = new VBox();
                vID.setAlignment(Pos.CENTER);
                vboxID.add(vID);
                group_pane.getChildren().add(vID);

                // generate info VBox
                VBox vInfo = new VBox();
                vInfo.setAlignment(Pos.CENTER);
                vboxInfo.add(vInfo);
                group_pane.getChildren().add(vInfo);

                // Light IDs shape
                Text id = new Text();
                shapeID.add(id);
                id.setTextAlignment(TextAlignment.CENTER);
                id.setVisible(menu_show_id.isSelected());
                vID.getChildren().add(id);

                // Light Lum shape
                Text lum = new Text();
                shapeLum.add(lum);
                lum.setTextAlignment(TextAlignment.CENTER);
                lum.setVisible(menu_show_lumPct.isSelected());
                vInfo.getChildren().add(lum);

                // Light temp shape
                Text tmp = new Text();
                shapeTemp.add(tmp);
                tmp.setTextAlignment(TextAlignment.CENTER);
                tmp.setVisible(menu_show_cct.isSelected());
                vInfo.getChildren().add(tmp);
            }
        }

        void setMouseListener() {
            // ToDo: generate mouse listener
        }

        void canvasResize() {
            double size = canvas_pane.getHeight() > canvas_pane.getWidth()
                    ? canvas_pane.getWidth(): canvas_pane.getHeight();
            // group_pane.resize(size, size);
            group_pane.setPrefSize(size, size);
            group_pane.setLayoutX(canvas_pane.getWidth()/2 - size/2);
            group_pane.setLayoutY(canvas_pane.getHeight()/2 - size/2);

            // update canvas size variable
            this.x = size;
            this.y = size;

            // repaint
            updateCanvas();
        }

        void updateCanvas() {
            // Room layout
            updateRoom();

            // Lights
            updateLights();

            // draw ID
            updateIDs();

            // draw info
            updateInfo();
        }

        void updateRoom() {
            // set size
            shapeRoom.setX(pctToX(0.03));
            shapeRoom.setY(pctToY(0.03));
            shapeRoom.setWidth(pctToX(0.94));
            shapeRoom.setHeight(pctToY(0.94));
        }

        void updateLights() {
            for(Light l: lights) {
                // find LightShape instance by light ID
                Ellipse light = shapeLights.get(l.getId() - 1);

                // generate lights color with temp and lumPct
                Color color = ImageUtils.getRGBFromK((int)l.getTemperature());
                double opacity = l.getLumPct() > 0 ? 0.2 + l.getLumPct()/100 * 0.8 : 0;
                color = Color.color(color.getRed(), color.getGreen(), color.getBlue(), opacity);

                // set size
                light.setCenterX(pctToX(0.05 + (l.getPosX() + 0.5)*(0.9/maxPosX)));
                light.setCenterY(pctToY(0.05 + (l.getPosY() + 0.5)*(0.9/maxPosY)));
                light.setRadiusX(pctToX(lightSizeX));
                light.setRadiusY(pctToY(lightSizeY));

                // set color
                light.setFill(color);

                // set stroke
                if(selected.get(l.getId()-1)) {
                    light.setStrokeWidth(2.0);
                    light.setStroke(Color.LIMEGREEN);
                } else {
                    light.setStrokeWidth(1.0);
                    light.setStroke(Color.LIGHTGRAY);
                }
            }
        }

        void updateIDs() {
            for(Light l: lights) {
                // find Text and container by ID
                VBox vID = vboxID.get(l.getId() - 1);
                Text id = shapeID.get(l.getId() - 1);

                // change text properties
                double f = pctToX(lightSizeX/1.2);
                id.setFill(Color.LIGHTGRAY);
                id.setFont(Font.font(f));
                id.setText(String.valueOf(l.getId()));

                // resize and reposition VBox
                vID.setLayoutX(pctToX(0.05 + (l.getPosX() + 0.5)*(0.9/maxPosX) - lightSizeX));
                vID.setLayoutY(pctToY(0.05 + (l.getPosY() + 0.5)*(0.9/maxPosY) - lightSizeY));
                vID.setPrefSize(pctToX(lightSizeX*2), pctToY(lightSizeY*2));
                vID.setFillWidth(true);
            }
        }

        void updateInfo() {
            for(Light l: lights) {
                // find Text and container by ID
                VBox vInfo = vboxInfo.get(l.getId() - 1);
                Text lum = shapeLum.get(l.getId() - 1);
                Text tmp = shapeTemp.get(l.getId() - 1);

                // change text properties
                double f = pctToX(lightSizeX/2);
                lum.setFill(Color.LIGHTGRAY);
                tmp.setFill(Color.LIGHTGRAY);
                lum.setFont(Font.font(f));
                tmp.setFont(Font.font(f));
                lum.setText(String.valueOf(l.getLumPct()) + " %");
                tmp.setText(String.valueOf(l.getTemperature()) + " K");

                // resize and reposition VBox
                vInfo.setLayoutX(pctToX(0.05 + (l.getPosX() + 0.5)*(0.9/maxPosX) - lightSizeX));
                vInfo.setLayoutY(pctToY(0.05 + (l.getPosY() + 0.5)*(0.9/maxPosY) + lightSizeY));
                vInfo.setPrefSize(pctToX(lightSizeX*2), pctToY(lightSizeY));
                vInfo.setFillWidth(true);
            }
        }

        private double pctToX(double x_pct) {return x * x_pct;}

        private double pctToY(double y_pct) {return y * y_pct;}

    }

    // this timer updates light information via socket connection
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

                // update canvas
                canvasControl.updateCanvas();

            } catch (Exception ignored) {}
        }
    }

}
