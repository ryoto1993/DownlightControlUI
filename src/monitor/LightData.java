package monitor;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;

/**
 * @author Ryoto Tomioka
 * This class is defined for JavaFX TableColumn data
 */
public class LightData {
    private final SimpleIntegerProperty id;
    private final SimpleDoubleProperty lumPct;
    private final SimpleIntegerProperty temperature;

    public LightData(int id, double lum, int temp) {
        this.id = new SimpleIntegerProperty(id);
        this.lumPct = new SimpleDoubleProperty(lum);
        this.temperature = new SimpleIntegerProperty(temp);
    }

    public int getId() {
        return id.get();
    }

    public SimpleIntegerProperty idProperty() {
        return id;
    }

    public void setId(int id) {
        this.id.set(id);
    }

    public double getLumPct() {
        return lumPct.get();
    }

    public SimpleDoubleProperty lumPctProperty() {
        return lumPct;
    }

    public void setLumPct(double lumPct) {
        this.lumPct.set(lumPct);
    }

    public int getTemperature() {
        return temperature.get();
    }

    public SimpleIntegerProperty temperatureProperty() {
        return temperature;
    }

    public void setTemperature(int temperature) {
        this.temperature.set(temperature);
    }
}
