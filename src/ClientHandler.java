import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

//The class handle each client request in a new thread
public class ClientHandler implements Runnable{
    private final Socket clientSocket;

    public ClientHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));// The input stream allows the server to read data which is sent by the client
             // and the buffered reader help to read the data efficiently.
             //The output stream allows the server to send data to the client and write formatted text easily
             PrintWriter out = new PrintWriter(clientSocket.getOutputStream())) {

            String request = in.readLine(); // read HTTP request from the client
            if (request == null) {
                out.println("HTTP/1.1 204 Request Not Found");
            }
            assert request != null;
            String[] requestParts = request.split(" "); //split the request line into parts based on white space and extract the HTTP methods(GET or PUT).
            String method = requestParts[0]; // Get or Put

            if ("GET".equalsIgnoreCase(method)) {
                AggregationServer.handleGetRequest(out); // send data to the client
            } else if ("PUT".equalsIgnoreCase(method)) {
                AggregationServer.handlePutRequest(in, out); //retrieve client's data and after updating send the data to client
            } else {
                out.println("HTTP/1.1 400 Bad Request");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
