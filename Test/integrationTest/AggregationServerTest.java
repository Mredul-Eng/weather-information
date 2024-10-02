import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPut;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class AggregationServerTest {
    private AggregationServer aggregationServer;
    Thread serverThread;
    private static final int TEST_PORT = 4567;
    private static final String SERVER_URL = "http://localhost:" + TEST_PORT;
    private static final String FILE_PATH = "C:\\Users\\SS-Computer\\OneDrive\\Desktop\\DS Assignment 02\\Weather-Information\\Test\\integrationTest\\test.txt";

    @Before
    public void setUp() throws Exception {
        aggregationServer = new AggregationServer();
        //start the AggregationServer in a separate Thread
        serverThread = new Thread(() -> aggregationServer.start(TEST_PORT));
        serverThread.start();

        //wait 20 second for the server to initialize
        Thread.sleep(2000);
    }

    //close the server thread/socket if needed
    @After
    public void tearDown() throws Exception {
        serverThread.interrupt();
    }

    //send PUT request from the ContentServer
    @Test
    public void testContentServerPutRequest() {
        Map<String, String> weatherData = ContentServer.readFile(FILE_PATH);
        String jsonData = ContentServer.convertToJSON(weatherData);

        //create a new instance of CloseableHttpClient which is used to send Http request and handle Http response.
        try(CloseableHttpClient httpClient = HttpClients.createDefault()){
            //send PUT request to the AggregationServer
            HttpPut putRequest = new HttpPut(SERVER_URL + "/weather.json"); // create http put request to create or update data at the given url
            putRequest.setEntity(new StringEntity(jsonData));//set the request body for PUT request
            putRequest.setHeader("Content-Type", "application/json");
                    try(CloseableHttpResponse httpResponse = httpClient.execute(putRequest)){
                        assertEquals(200, httpResponse.getCode()); // verify if the request send successfully or not
                    }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    //test PUT request for invalid data
    @Test
    public void testInvalidPUTRequest() throws Exception{
        String invalidJsonData = "{\"id\":\"station1\",\"temperature\":25,"; // Invalid JSON (trailing comma)
        try(CloseableHttpClient httpClient = HttpClients.createDefault()){
            //send PUT request to the AggregationServer
            HttpPut putRequest = new HttpPut(SERVER_URL + "/weather.json"); // create http put request to create or update data at the given url
            putRequest.setEntity(new StringEntity(invalidJsonData));//set the request body for PUT request
            putRequest.setHeader("Content-Type", "application/json");
            try(CloseableHttpResponse httpResponse = httpClient.execute(putRequest)){
                assertEquals(500, httpResponse.getCode()); // verify if the request send successfully or not
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    //send GET request from client
    @Test
    public void testClientGetRequest() throws IOException {
        try(CloseableHttpClient httpClient = HttpClients.createDefault()){
            //send PUT request to the AggregationServer
            HttpGet getRequest = new HttpGet(SERVER_URL + "/weather.json"); // create http put request to create or update data at the given url
            try(CloseableHttpResponse response = httpClient.execute(getRequest)){
                assertEquals(200, response.getCode()); // verify if the request send successfully or not
                String responseBody = EntityUtils.toString(response.getEntity());
                System.out.println(responseBody);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testExpirationOfOldWeatherData() throws IOException, InterruptedException {
        // Prepare initial weather data and convert it to JSON
        Map<String, String> weatherData = ContentServer.readFile(FILE_PATH);
        String jsonData = ContentServer.convertToJSON(weatherData);

        // Create an instance of CloseableHttpClient to send the HTTP request and handle the response.
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {

            // First, send a PUT request to update the server with weather data.
            HttpPut putRequest = new HttpPut(SERVER_URL + "/weather.json");
            putRequest.setEntity(new StringEntity(jsonData)); // Set JSON as the body for the PUT request
            putRequest.setHeader("Content-Type", "application/json");

            // Execute the PUT request
            try (CloseableHttpResponse putResponse = httpClient.execute(putRequest)) {
                assertEquals(200, putResponse.getCode()); // Verify that the request is successful
            }

            // Wait for 35 seconds to ensure the weather data expires (since the timeout is 30 seconds)
            TimeUnit.SECONDS.sleep(35);

            // Now send a GET request to check if the expired data has been removed
            HttpGet getRequest = new HttpGet(SERVER_URL + "/weather.json");
            getRequest.setHeader("Cache-Control", "no-cache");

            // Execute the GET request
            try (CloseableHttpResponse getResponse = httpClient.execute(getRequest)) {
                assertEquals(200, getResponse.getCode()); // Check if the GET request is successful

                String responseBody = EntityUtils.toString(getResponse.getEntity()); // Get the response body
                System.out.println("Response Body after 35 seconds: " + responseBody);

                // Assert that the response is an empty JSON array, indicating the old data was removed
                assertEquals("[]", responseBody.trim());
            }

        } catch (IOException | InterruptedException | ParseException e) {
            throw new RuntimeException(e);
        }
    }




}