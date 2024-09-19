public class WeatherData {
    private String sourceId; // content server id that provide data
    private String info; // current weather information
    private int lamportTime; // that time when the data was received

    public WeatherData(String sourceId, String info, int lamportTime) {
        this.sourceId = sourceId;
        this.info = info;
        this.lamportTime = lamportTime;
    }

    public String getSourceId() {
        return sourceId;
    }

    public String getInfo() {
        return info;
    }

    public int getLamportTime() {
        return lamportTime;
    }

    //print weather data in JSON format
    @Override
    public String toString() {
        return "{" + "\"source\":\"" + sourceId + "\",\"info\":\"" + info + "\",\"lamportTime\":" + lamportTime + "}";
    }

}
