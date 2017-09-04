package monitor;

/**
 * Created by RyotoTomioka on 2017/06/12.
 */
public class Light {
    private static int id_counter = 1;
    private int id;

    private int signalA = 0;  // 白色信号値
    private int signalB = 0;  // 昼白色信号値

    private double temperature = 4000;  // 色温度
    private double lumPct = 50.0;       // 光度パーセント

    public Light() {
        id = id_counter++;
    }

    void setSignal(int[] sigs) {
        signalA = sigs[0];
        signalB = sigs[1];
    }

    void setLumPct(double pct) {this.lumPct = pct;}

    void setTemperature(double tmp) {this.temperature = tmp;}

    int getId() { return id; }

    double getLumPct() {
        return lumPct;
    }

    double getTemperature() {
        return temperature;
    }

    int[] getSignal() {
        return new int[]{signalA, signalB};
    }
}

