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
import com.google.gson.JsonSyntaxException;


//This class mainly responsible for running the server and handling client's request
public class AggregationServer {

    //Constant
    private static final int DEFAULT_PORT = 4531; //the server will start in 4531 port by default
    private static final Long TIMEOUT = 30000L; // content servers will expire if they didn't communicate within 30 seconds.
    private static final int MAX_ENTRIES = 20; // maximum number of weather data (entries) to be stored
    private static final String FILENAME = "weather-data.txt";

    //Variables that store and track weather data
    private static final Map<String, WeatherData> storeWeatherData = new ConcurrentHashMap<>(); // Stores weather data.
    private static final Map<String, Long> lastSeenContentServers = new ConcurrentHashMap<>(); // track last communication time for each content servers to remove old data
    private static final LamportClock lamportClock = new LamportClock(); // implement a lamport clock to track the local time

    public static void main(String[] args) {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : DEFAULT_PORT;
       AggregationServer server = new AggregationServer();
       server.loadDataFromFile();
       server.start(port);
    }
    //load weather data from the file
    private void loadDataFromFile() {
        //read the file with the help of FileReader and BufferedReader
        try (BufferedReader reader = new BufferedReader(new FileReader(FILENAME))) {
            String line;
            if((line = reader.readLine()) != null){
                WeatherData weatherData = parseWeatherDataFromJSON(line); //parse weather data from JSON line by line.
                storeWeatherData.put(weatherData.getSourceId(), weatherData); //Restore weather data into map
                lastSeenContentServers.put(weatherData.getSourceId(), System.currentTimeMillis()); // Restore timestamps
            }
        } catch (IOException e) {
            System.out.println("Error to load data from the file: " + e.getMessage());
        }
    }

    //This method send appropriate data to the client
    public static void handleGetRequest(PrintWriter out) {
        lamportClock.incrementTime(); //update Lamport clock on GET request
        out.println("HTTP/1.1 200 OK");
        out.println("Content-Type:application/json");
        out.println();
        out.println(serializeToJSON()); //convert weather data to JSON format

    }
   //this method update the weather data based on client's PUT request
    public static void handlePutRequest(BufferedReader in, PrintWriter out) throws IOException {
        lamportClock.incrementTime();//update the Lamport clock on PUT request

        StringBuilder jsonContent = new StringBuilder();
        String line;
        //this loop reads data (the data should be in JSON format) line by line which is sent by client
        while((line = in.readLine()) != null && !line.isEmpty()){
            jsonContent.append(line); // continuously update each line of text of client's request data
        }
        WeatherData weatherData = parseWeatherDataFromJSON(jsonContent.toString());// convert the json texts to string
        // and pass the data to parseWeatherDataFromJSON method to convert json data to weatherData object

        if(weatherData != null){ //if the json data successfully converted to weather data
            storeWeatherData.put(weatherData.getSourceId(), weatherData); //add or update the weather data in storeWeatherData map
            lastSeenContentServers.put(weatherData.getSourceId(), System.currentTimeMillis()); //track the last time of a specific content server that send data.
            if(storeWeatherData.size() > MAX_ENTRIES){
                removeOldWeatherData();
            }
            else{
                out.println("HTTP/1.1 200 OK");
            }
            saveDataToFile();
        }
        else{
            out.println("HTTP/1.1 500 Internal Server Error");
        }

    }

    private static void saveDataToFile() {
        try(BufferedWriter writer = new BufferedWriter(new FileWriter(FILENAME, true))) {
            for(WeatherData data : storeWeatherData.values()){
                writer.write(data.toString()); // convert each weather data to JSON string and write in the file.
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error to write or save data to file: " + e.getMessage());
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
        }catch(JsonSyntaxException e){
            System.out.println("Failed to parse JSON");
            return null;
        }
    }

    private static String serializeToJSON() {
        Gson gson = new Gson();
        return gson.toJson(storeWeatherData.values()); // convert weather data to JSON string.
    }

    private void start(int port) {
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
        saveDataToFile(); // ensure data will be written in file up to date
    }
}

