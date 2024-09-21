import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;

//This class is designed to send GET request to the AggregationServer and get response (weather data) in JSON format
public class GETClient {
    private static final LamportClock lamportClock = new LamportClock();

    public static void main(String[] args) {
        //check if necessary command line is provided or not. If not, then print a message by giving usage instruction and exits.
        if(args.length < 1){
            System.out.println("Usages: java GETClient <serverURL> [stationId]");
            return;
        }
        try{
            String serverURL = args[0]; // extract first command line arguments
            String stationId = (args.length > 1) ? args[1] : null; // extract the stationId if there is available second command line

            URL url = parseServerURL(serverURL);
            String hostName = url.getHost(); // extract host name from the URL
            int port = (url.getPort() != -1) ? url.getPort() : 80; // extract port number from the URL.
            // If no port number is given then set default port number as 80
            String path = "/weather.json"; // end point for request weather data

            if(stationId != null){
                path = path + "?id=" + stationId; // add stationId as a query parameter if it is available
            }

            // increase the lamport clock's time before sending the GET request
            lamportClock.incrementTime();

            //establish a socket connection to communicate with the server
            try(Socket socket = new Socket(hostName,port);//open network connection to specified hostName and port
                PrintWriter out = new PrintWriter(socket.getOutputStream()); // client send request to the server
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream())); // client receive data from the server
            )
            {
                //Form a valid HTTP GET request and send it to the server
                out.println("GET" + path + "HTTP/1.1"); //specify the HTTP method(GET) and the URL path of the resource.
                out.println("Host: " + hostName); // specify the IP address or domain of the server
                out.println("User-Agent: WeatherGETClient/1.0"); //specify the client that send request
                out.println("Lamport-Clock: " + lamportClock.getTime());//sending a custom header that includes current time of lamport clock
                // and keep track of the logical time for the system.
                out.println("Connection: close"); // close the connection after sending the response
                out.println(); // marks the end of the HTTP request headers.

                //Read and display the server response
                String responseLine;
                StringBuilder jsonResponse = new StringBuilder();
                boolean jsonStarted = false; // ignore the HTTP request header

                //Read the response line by line
                while((responseLine = in.readLine()) != null){
                    if(jsonStarted){
                        jsonResponse.append(responseLine); //ignore the request header
                        // and add each response line in jsonResponse
                    }
                    if(responseLine.isEmpty()){
                        jsonStarted = true; // if responseLine is empty, then JSON starts after request header.
                    }
                }
                // increase the lamport clock's time after receiving response
                lamportClock.incrementTime();

                //displaying the weather data
                if(!jsonResponse.isEmpty()){
                    displayWeatherData(jsonResponse.toString());
                }
                else {
                    System.out.println("No data is received");
                }

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void displayWeatherData(String json) {
        Gson gson = new Gson();
        //parse JSON into weather data object
        WeatherData weatherData = gson.fromJson(json, WeatherData.class);
        System.out.println(weatherData.toString()); // print the weather data in JSON format
    }

    // this method return correctly formatted server URL.
    private static URL parseServerURL(String serverURL) throws MalformedURLException {
        //add http:// protocol if the url doesn't start with it.
        if(!serverURL.startsWith("http://")){
            serverURL = "http://" + serverURL;
        }
            return new URL(serverURL); // the URL class validate the serverURL
            // and parse the URL into components such as host, port.
    }
}
