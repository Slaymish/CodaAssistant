package Coda.App;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

class WebServer {
    String ip;
    int port;

    boolean running = false;

    CodaApplication app;

    public WebServer(String ip, int port, CodaApplication app) {
        this.ip = ip;
        this.port = port;
        this.app = app;
    }


    /**
     * Start the HTTP server on the given port
     *
     * @param port The port to start the server on
     */
    public void start(int port) {
        // Start the http server
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server started on port " + port);

            running = true;
            while (running) {
                Socket socket = serverSocket.accept();
                System.out.println("Client connected: " + socket.getInetAddress());
                handleClient(socket);
            }
        } catch (IOException e) {
            System.out.println("Server error: " + e.getMessage());
        }
    }

    private void handleClient(Socket socket) {
        // Send list of services to client
        try {
            var writer = socket.getOutputStream();
            sendServices(writer);
        } catch (IOException e) {
            System.out.println("Server error: " + e.getMessage());
        }
    }

    private void sendServices(OutputStream writer) {
        List<WebPageService> services = app.getServices();
        StringBuilder response = new StringBuilder();

        response.append("<html><body>");
        services.forEach(service -> {
            response.append("<h1>").append(service.title()).append("</h1>");
            response.append("<p>").append(service.description()).append("</p>");
            response.append("<p>").append(service.version()).append("</p>");
            response.append("<p>").append(service.author()).append("</p>");
            response.append("<p>").append(service.license()).append("</p>");
        });
        response.append("</body></html>");

        try {
            writer.write(response.toString().getBytes());
        } catch (IOException e) {
            System.out.println("Server error: " + e.getMessage());
        }
    }

    public void addServiceEndpoint(WebPageService<?,?> codaService) {
        String endpoint = codaService.endpoint();
        System.out.println("Adding endpoint: " + endpoint);
    }
}
