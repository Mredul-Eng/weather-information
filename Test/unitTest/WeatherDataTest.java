import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class WeatherDataTest {
    private WeatherData weatherData;

    @Before
    public void setUp() throws Exception {
        weatherData = new WeatherData("IDS60901", "Adelaide (West Terrace / ngayirdapira)", "SA",
        "CST",
        -34.9,
        138.6,
       "15/04:00pm",
        "20230715160000",
        13.3,
        9.5,"Partly cloudy",
        5.7,
        1023.9,
        60,
        "S",
       15,
        8
);
    }

    //verify the method returns correct JSON format
    @Test
    public void testToString() {
        String expectedJson = "{ " +
                "\"id\": \"" + "IDS60901" + "\"," +
                "\"name\": \"" + "Adelaide (West Terrace / ngayirdapira)" + "\"," +
                "\"state\": \"" + "SA" + "\"," +
                "\"time_zone\": \"" + "CST" + "\"," +
                "\"lat\": " + -34.9 + "," +
                "\"lon\": " + 138.6 + "," +
                "\"local_date_time\": \"" + "15/04:00pm" + "\"," +
                "\"local_date_time_full\": \"" + "20230715160000" + "\"," +
                "\"air_temp\": " + 13.3 + "," +
                "\"apparent_t\": " + 9.5 + "," +
                "\"cloud\": \"" + "Partly cloudy" + "\"," +
                "\"dewpt\": " + 5.7 + "," +
                "\"press\": " + 1023.9 + "," +
                "\"rel_hum\": " + 60 + "," +
                "\"wind_dir\": \"" + "S" + "\"," +
                "\"wind_spd_kmh\": " + 15 + "," +
                "\"wind_spd_kt\": " + 8 +
                " }";
        assertEquals(expectedJson,weatherData.toString());
    }
}