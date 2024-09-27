import com.google.gson.Gson;

import java.io.*;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

public class ContentServer {
    private static final LamportClock lamportClock = new LamportClock();

    public static void main(String[] args) {
        //check if necessary command line is provided or not. If not, then print a message by giving usage instruction and exits.
        if(args.length  < 2){
            System.out.println("The command to run the ContentServer should be:" +
                    " java ContentServer <serverURL>");
            return;
        }
        String urlString = args[0];
        String filePath = args[1];

        try{
            URL url = parseServerURL(urlString);
            String hostName = url.getHost(); // extract host name from the URL
            int port = (url.getPort() != -1) ? url.getPort() : 80;
            //send the weather data to the Aggregation Server through PUT request
            sendPutRequest(hostName,port,filePath);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }


    }
    private static void sendPutRequest(String hostName, int port, String filePath) throws IOException {
        // Read weather data from the file
        Map<String, String> weatherData = readFile(filePath);
        System.out.println("Data after reading the file " + weatherData);

        // Convert weather data to JSON
        String jsonData = convertToJSON(weatherData);
        System.out.println("Convert the weather data to JSON " + jsonData);
        try(Socket socket = new Socket(hostName,port);//open network connection to specified hostName and port
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream())); // client receive data from the server
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8)); // send HTTP Put request and data to the server
        ){
            // increase the lamport clock's time before sending the PUT request
            lamportClock.incrementTime();

           // prepare HTTP PUT request's header
            String path = "/weather.json";
            String userAgent = "ATOMClient/1.0";
            String contentType = "application/json";
            int contentLength = jsonData.length();


            // Send the PUT request
            writer.write("PUT " + path + " HTTP/1.1\r\n");
            writer.write("Host: " + hostName + ":" + port + "\r\n");
            writer.write("User-Agent: " + userAgent + "\r\n");
            writer.write("Content-Type: " + contentType + "\r\n");
            writer.write("Content-Length: " + contentLength + "\r\n");
            writer.write("Connection: close\r\n"); // Close connection after request
            writer.write("\r\n"); // End of headers, beginning of body
            writer.write(jsonData); // Write JSON body
            writer.flush();

            String responseLine;
            while((responseLine = in.readLine())!= null){
                System.out.println(responseLine); // read the response line one by one and print them to showing the client server's response.
            }
        }
    }


    //this method convert the weather data map to JSON
    private static String convertToJSON(Map<String, String> weatherData) {
        Gson gson = new Gson();
        return gson.toJson(weatherData);

    }
    //read the local file line by line and split each line into key-value pairs
    private static Map<String, String> readFile(String filePath) throws FileNotFoundException {
        Map<String, String> weatherData = new LinkedHashMap<>(); // store weather data from the local file
        //read the file line by line
        try(BufferedReader reader = new BufferedReader(new FileReader(filePath))){
            String line;
            while((line = reader.readLine()) != null){
                String[] keyValue = line.split(":", 2); // each line from the file is written in key:value format
                // and the spilt method split each line(string) into two parts based on ":". After splitting the string, stored the results in the keyValue array.
                if(keyValue.length == 2){
                    weatherData.put(keyValue[0].trim(), keyValue[1].trim()); //after ensuring that the line is split correctly into two parts
                    // and add the key-value pair into the weatherData map and remove extra spaces using trim() method.
                    // Here, keyValue[0] = key and keyValue[1] = value
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return weatherData;
    }

    private static URL parseServerURL(String serverURL) throws MalformedURLException {
        //add http:// protocol if the url doesn't start with it.
        if(!serverURL.startsWith("http://")){
            serverURL = "http://" + serverURL;
        }
        return new URL(serverURL); // the URL class validate the serverURL
        // and parse the URL into components such as host, port.
    }
}
