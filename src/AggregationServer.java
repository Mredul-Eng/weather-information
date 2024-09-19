import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


//This class mainly responsible for running the server and handling client's request
public class AggregationServer {

    //Constant
    private static final int DEFAULT_PORT = 4531; //the server will start in 4531 port by default
    private static final Integer TIMEOUT = 30000; // content servers will expire if they didn't communicate within 30 seconds.
    private static final int MAX_ENTRIES = 20; // maximum number of weather data (entries) to be stored

    //Variables that store and track weather data
    private Map<String, WeatherData> storeWeatherData = new ConcurrentHashMap<>(); // Stores weather data.
    private Map<String, Integer> lastSeenContentServers = new ConcurrentHashMap<>(); // track last communication time for each content servers to remove old data
    private LamportClock lamportClock = new LamportClock(); // implement a lamport clock to track the local time

    public static void main(String[] args) {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : DEFAULT_PORT;
        new AggregationServer().start(port);
    }

    private void start(int port) {
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("The Aggregation Server is running in port: " + port);
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
}

