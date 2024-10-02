import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.google.gson.Gson;

//This class mainly responsible for running the server and handling client's request
public class AggregationServer {

    //Constant
    private static final int DEFAULT_PORT = 4567; //the server will start in 4531 port by default
    private static final Long TIMEOUT = 30000L; // content servers will expire if they didn't communicate within 30 seconds.
    private static final int MAX_ENTRIES = 20; // maximum number of weather data (entries) to be stored
    private static final String FILENAME = "weather-data.json";

    //Maps that store and track weather data
    private static final Map<String, WeatherData> storeWeatherData = new ConcurrentHashMap<>(); // Stores weather data.
    private static final Map<String, Long> lastSeenContentServers = new ConcurrentHashMap<>(); // track last communication time for each content servers to remove old data
    private static final LamportClock lamportClock = new LamportClock(); // implement a lamport clock to track the local time

    public static void main(String[] args){
        int port = args.length > 0 ? Integer.parseInt(args[0]) : DEFAULT_PORT;
        AggregationServer server = new AggregationServer();
        server.start(port);
    }
    //This method send appropriate data to the client
    public static void handleGetRequest(PrintWriter out) {
        lamportClock.incrementTime(); // Update Lamport clock on GET request
        out.println("HTTP/1.1 200 OK");
        out.println("Content-Type: application/json");
        out.println();

        StringBuilder jsonResponse = new StringBuilder();
        File file = new File(FILENAME);
        if (file.exists()) {
            long lastModified = file.lastModified();
            long now = System.currentTimeMillis();

            // If the file is older than 30 seconds, remove the data and return an empty array
            if (now - lastModified > 30000) {
                // Optionally delete the file or modify its content to show expired status
                //file.delete(); // This will remove the file completely
                out.println("[]"); // Return an empty array for expired data
            } else {
                try (BufferedReader reader = new BufferedReader(new FileReader(FILENAME))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        jsonResponse.append(line);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    out.println("{\"error\": \"Unable to read weather data\"}");
                    return;
                }

                if (jsonResponse.length() > 0) {
                    out.println(jsonResponse);
                } else {
                    out.println("[]"); // Return an empty array if the file is empty
                }
            }
            out.flush();
        } else {
            // Return an empty array if file doesn't exist
            out.println("[]");
            out.flush();
        }
    }


    //this method update the weather data based on client's PUT request
    public static void handlePutRequest(BufferedReader in, PrintWriter out) throws IOException {
        lamportClock.incrementTime();//update the Lamport clock on PUT request
        StringBuilder jsonContent = new StringBuilder();
        String line;
        int contentLength = 0;
        //this loop reads header line by line which is sent by client
        while((line = in.readLine()) != null && !line.isEmpty()){
            if(line.startsWith("Content-Length:")){
                contentLength = Integer.parseInt(line.split(":")[1].trim()); // extract the Content-Length value from header and convert it to integer.
            }
        }

        if(contentLength > 0){
            char[] contentBody = new char[contentLength];
            int bytesRead = in.read(contentBody, 0, contentLength); // read content-length's number of characters.The content-body mainly contain the JSON content in byte[] array format
            if (bytesRead > 0) {
                jsonContent.append(contentBody, 0, bytesRead); // append the actual content (not the number of bytes read)
            }
        }
        WeatherData weatherData = parseWeatherDataFromJSON(jsonContent.toString());// convert the json texts to string
        // and pass the data to parseWeatherDataFromJSON method to convert json data to weatherData object
        if(weatherData != null){ //if the json data successfully converted to weather data
            storeWeatherData.put(weatherData.getId(), weatherData); //add or update the weather data in storeWeatherData map
            lastSeenContentServers.put(weatherData.getId(), System.currentTimeMillis()); //track the last time of a specific content server that send data.
            if(storeWeatherData.size() > MAX_ENTRIES){
                removeOldWeatherData();
            }
            out.println("HTTP/1.1 200 OK");
            saveDataToFile(storeWeatherData);
        }
        else{
            out.println("HTTP/1.1 500 Internal Server Error");
        }

    }

    private static void saveDataToFile(Map<String, WeatherData> storeWeatherData) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILENAME, false))) {
            // Print the data to be saved to the console for debugging
            if (storeWeatherData.isEmpty()) {
                System.out.println("No data to save, storeWeatherData is empty.");
            } else {
                System.out.println("Saving the following data to the file:");
                System.out.println(storeWeatherData);
            }

            // Save the data to the file
            writer.write("[");
            boolean firstJsonData = true;
            for (WeatherData data : storeWeatherData.values()) {
                if(!firstJsonData) writer.write(","); //check if the data is first or not. if the data is not first data then append data with comma.
                writer.write(serializeToJSON(data));
                firstJsonData = false;
            }
            writer.write("]");
            writer.flush(); // Ensure all data is flushed and written to the file
        } catch (IOException e) {
            System.out.println("Error writing or saving data to the file: " + e.getMessage());
        }
    }

    private static void removeOldWeatherData() {
        String oldestData = lastSeenContentServers.entrySet().stream()
                .min(Comparator.comparingLong(Map.Entry::getValue)).
                map(Map.Entry::getKey).orElse(null); // search and remove the weather data from the oldest content server which inactive for the longest time

        if(oldestData != null){
            storeWeatherData.remove(oldestData);
            lastSeenContentServers.remove(oldestData);
        }
    }

    private static WeatherData parseWeatherDataFromJSON(String jsonData) {
        try{
            Gson gson = new Gson();
            return gson.fromJson(jsonData, WeatherData.class); //convert JSON string to weather data
        }catch(Exception e){
            System.out.println("Failed to parse JSON");
            return null;
        }
    }

    private static String serializeToJSON(WeatherData weatherData) {
        Gson gson = new Gson();
        return gson.toJson(weatherData); // convert weather data to JSON string.
    }

    public void start(int port) {
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("The Aggregation Server is running in port: " + port);

            //remove expired data in every 30 second
            ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
            executorService.scheduleAtFixedRate(this::removeExpiredData, 30, 30, TimeUnit.SECONDS);

            while(true){
                try{
                    Socket clientSocket = serverSocket.accept(); // receive server socket connection in client socket
                    new Thread(new ClientHandler(clientSocket)).start(); // create a new ClientHandler in a separate Thread to handle multiple client
                }catch (IOException e){
                    throw new RuntimeException(e);
                }
            }
        } catch (IOException e) {
            System.out.println("Server error: " + e.getMessage());
        }
    }

    private void removeExpiredData() {
        long currentTime = System.currentTimeMillis();
        lastSeenContentServers.entrySet().removeIf(entry ->{
            if(currentTime - entry.getValue() > TIMEOUT){
                storeWeatherData.remove(entry.getKey()); //remove expired data
                return true;
            }
            return false;
        });
        saveDataToFile(storeWeatherData); // ensure data will be written in file up to date
    }
}

