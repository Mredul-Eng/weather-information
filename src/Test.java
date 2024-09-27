import com.google.gson.Gson;

import java.io.*;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class Test {
    private static final String filePath = "D:\\Users\\Microservices\\Weather_Information\\src\\weather-data.txt";
    public static void main(String[] args) throws FileNotFoundException {
        Map<String, String> weatherData = readFile(filePath);
        System.out.println(weatherData);
        String jsonData = convertToJSON(weatherData);
        System.out.println(jsonData);
        saveDataToFile(weatherData);
    }
    private static void saveDataToFile(Map<String, String> data) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            // Convert map to JSON and write it to the file
            writer.write(convertToJSON(data));
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private static String convertToJSON(Map<String, String> weatherData) {
        Gson gson = new Gson();
        return gson.toJson(weatherData);

    }
    private static Map<String, String> readFile(String filePath) throws FileNotFoundException {
        Map<String, String> weatherData = new LinkedHashMap<>(); // store weather data from the local file
        //read the file line by line
        try(BufferedReader reader = new BufferedReader(new FileReader(filePath))){
            String line;
            while((line = reader.readLine()) != null){
                String[] keyValue = line.split(":", 2);
                if(keyValue.length == 2){
                    weatherData.put(keyValue[0].trim(), keyValue[1].trim()); //after ensuring that the line is split correctly into two parts
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return weatherData;
    }
}
