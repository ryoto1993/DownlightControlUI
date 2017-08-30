package monitor;

public class Light {
    private int id;
    private double lumPct;
    private int temperature;

    public Light() {}

    public void setStatus(int id, double pct, int temp) {
        this.id = id;
        this.lumPct = pct;
        this.temperature = temp;
    }
}
