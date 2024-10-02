
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class ContentServerTest {

    //verify the readFile() method read the file properly
  @Test
    public void testReadFile() throws IOException {
      String testFilePath = "C:\\Users\\SS-Computer\\OneDrive\\Desktop\\DS Assignment 02\\Weather-Information\\Test\\unitTest\\test-weather-data.txt";
      Map<String, String> weatherData = ContentServer.readFile(testFilePath);
      assertEquals("IDS60901", weatherData.get("id"));
      assertEquals("Adelaide", weatherData.get("name"));
      assertEquals("SA", weatherData.get("state"));
      assertEquals("CST", weatherData.get("time_zone"));
      assertEquals("Partly cloudy", weatherData.get("cloud"));
  }

  //The test case handle the file has no data or empty, or the file has no value for a specific key
    @Test
    public void testReadFileWithoutData() throws IOException {
      String testFilePath = "C:\\Users\\SS-Computer\\OneDrive\\Desktop\\DS Assignment 02\\Weather-Information\\Test\\unitTest\\missing-data.txt";
      Map<String, String> weatherData = ContentServer.readFile(testFilePath);
      assertTrue(weatherData.isEmpty());
      System.out.println("The file doesn't contain any data");
      assertNull(weatherData.get("name"));
      System.out.println("There is no data for name key");
    }
    //verify the convertToJSON() method to convert the weather data to JSON
  @Test
  public void testConvertToJSON() throws Exception {
    Map<String, String> weatherData = new HashMap<>();
    weatherData.put("id", "IDS60901");
    weatherData.put("name", "Adelaide");
    weatherData.put("state", "SA");

    String json = ContentServer.convertToJSON(weatherData);
    assertTrue(json.contains("\"id\":\"IDS60901\""));
    assertTrue(json.contains("\"name\":\"Adelaide\""));
    assertTrue(json.contains("\"state\":\"SA\""));
    System.out.println("Successfully Converted to JSON");
  }

  //verify if the convertToJSON() method is worked for empty data
  @Test
  public void testConvertToJSONForEmptyData() throws Exception {
    Map<String, String> weatherData = new HashMap<>();
    String json = ContentServer.convertToJSON(weatherData);
    assertEquals("{}",json);
    System.out.println("JSON is empty");
  }

}