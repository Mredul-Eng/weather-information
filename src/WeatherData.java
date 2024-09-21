public class WeatherData {
    private final String sourceId; // content server id(source) that provide data
    private final String name;
    private final String state;
    private final String time_zone;
    private final double lat;
    private final double lon;
    private final String local_date_time;
    private final String local_date_time_full;
    private final double air_temp;
    private final double apparent_t;
    private final String cloud;
    private final double dewpt;
    private final double press;
    private final int rel_hum;
    private final String wind_dir;
    private final int wind_spd_kmh;
    private final int wind_spd_kt;

    public WeatherData(String sourceId, String name, String state, String time_zone, double lat, double lon, String local_date_time, String local_date_time_full, double air_temp, double apparent_t, String cloud, double dewpt, double press, int rel_hum, String wind_dir, int wind_spd_kmh, int wind_spd_kt) {
        this.sourceId = sourceId;
        this.name = name;
        this.state = state;
        this.time_zone = time_zone;
        this.lat = lat;
        this.lon = lon;
        this.local_date_time = local_date_time;
        this.local_date_time_full = local_date_time_full;
        this.air_temp = air_temp;
        this.apparent_t = apparent_t;
        this.cloud = cloud;
        this.dewpt = dewpt;
        this.press = press;
        this.rel_hum = rel_hum;
        this.wind_dir = wind_dir;
        this.wind_spd_kmh = wind_spd_kmh;
        this.wind_spd_kt = wind_spd_kt;
    }

    public String getSourceId() {
        return sourceId;
    }

    //print weather data in JSON format
    @Override
    public String toString() {
        return
                "id='" + sourceId + '\'' +
                ", name='" + name + '\'' +
                ", state='" + state + '\'' +
                ", time_zone='" + time_zone + '\'' +
                ", lat=" + lat +
                ", lon=" + lon +
                ", local_date_time='" + local_date_time + '\'' +
                ", local_date_time_full='" + local_date_time_full + '\'' +
                ", air_temp=" + air_temp +
                ", apparent_t=" + apparent_t +
                ", cloud='" + cloud + '\'' +
                ", dewpt=" + dewpt +
                ", press=" + press +
                ", rel_hum=" + rel_hum +
                ", wind_dir='" + wind_dir + '\'' +
                ", wind_spd_kmh=" + wind_spd_kmh +
                ", wind_spd_kt=" + wind_spd_kt +
                '}';
    }

}
