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
             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

            String requestLine = in.readLine(); // read HTTP request from the client
            System.out.println(requestLine);
            if (requestLine == null) {
                out.println("HTTP/1.1 204 Request Not Found");
            }
            else{
//                String [] request = requestLine.split(" ");
//                if(request[0].equalsIgnoreCase("GET")){
//                    AggregationServer.handleGetRequest(out);
//                } else if (request[0].equalsIgnoreCase("PUT")) {
//                    AggregationServer.handlePutRequest(in, out);
//                }
                if(requestLine.startsWith("GET")){
                    AggregationServer.handleGetRequest(out);
                }
                else if(requestLine.startsWith("PUT")){
                    AggregationServer.handlePutRequest(in, out);
                }
                else {
                    out.println("HTTP/1.1 400 Bad Request");
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        finally {
            try {
                clientSocket.close(); // Close the socket when done
            } catch (IOException e) {
                System.out.println("Error closing socket: " + e.getMessage());
            }
        }

    }
}
