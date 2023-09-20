package coda.app;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.ObjectStreamException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

class WebServer {
    String ip;
    int port;

    boolean running = false;

    CodaApplication app;

    Logger logger = Logger.getLogger(WebServer.class.getName());

    private static WebPageService pageLastOn = null;

    private static final Charset UTF_8 = StandardCharsets.UTF_8;

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
            logger.info("Server started on port " + port);

            running = true;
            while (running) {
                Socket socket = serverSocket.accept();
                logger.info("Client connected: " + socket.getInetAddress());
                handleClient(socket);
            }
        } catch (IOException e) {
            logger.severe("Server error: " + e.getMessage());
        }
    }

    /**
     * Handle a client.
     *
     * @param socket The client socket
     */
    private void handleClient(Socket socket) {
        // Get the request
        try{
            BufferedInputStream reader = new BufferedInputStream(socket.getInputStream());
            OutputStream writer = socket.getOutputStream();

            // Read the request
            String request = readRequest(reader);
            logger.info("Request: " + request);

            // Send the response
            sendResponse(writer, request);
        } catch (IOException e){
            logger.severe("Server error: " + e.getMessage());
        }
    }

    /**
     * Send a response to the client.
     *
     * @param writer The output stream
     * @param request The request
     */
    private void sendResponse(OutputStream writer, String request) {
        matchRequestToService(request, writer);
    }

    /**
     * Match the request to a service.
     *
     * @param request The request
     * @param writer The output stream
     */
    private void matchRequestToService(String request, OutputStream writer) {
        if (request.contains("/runService")) {
            if (pageLastOn == null) {
                send404(writer);
                return;
            }

            try {
                Object input = pageLastOn.parseInput(request, pageLastOn);

                logger.info("Input: " + input);

                Object output = pageLastOn.runService(input);

                sendResult(writer, output);
                return;
            } catch (Exception e) {
                logger.severe("Server error: " + e.getMessage());
                send404(writer);
                return;
            }
        }

        List<WebPageService> services = app.getServices();
        List<WebPageService> matchingServices = new ArrayList<>();

        // Find matching services
        services.forEach(service -> {
            if (request.contains(service.endpoint())) {
                matchingServices.add(service);
            }
        });

        // Send the response
        if (matchingServices.size() == 1) {
            pageLastOn = matchingServices.get(0);
            sendService(writer, matchingServices.get(0));
        } else if (matchingServices.size() > 1) {
            send404(writer);
        } else {
            sendAllServices(writer);
        }
    }

    /**
     * Send the result on running the service to the client.
     *
     * @param writer The output stream
     * @param output The output of the service
     */
    private void sendResult(OutputStream writer, Object output) {
        try {
            // Send to /rendered-image
            writer.write("POST /rendered-image HTTP/1.1\r\n\r\n".getBytes(UTF_8));
            writer.write(output.toString().getBytes(UTF_8));
        } catch (IOException e) {
            logger.severe("Server error: " + e.getMessage());
        }
    }

    private void sendService(OutputStream writer, WebPageService service) {
        try {
            writer.write(service.render().getBytes(UTF_8));
        } catch (IOException e) {
            logger.severe("Server error: " + e.getMessage());
        }
    }

    /**
     * Send a 404 response.
     *
     * @param writer The output stream
     */
    private void send404(OutputStream writer) {
        try {
            writer.write("HTTP/1.1 404 Not Found\r\n\r\n".getBytes(UTF_8));
        } catch (IOException e) {
            logger.severe("Server error: " + e.getMessage());
        }
    }

    /**
     * Read the request.
     *
     * @param reader The input stream
     * @return The request
     * @throws IOException
     */
    private String readRequest(BufferedInputStream reader) throws IOException {
        StringBuilder request = new StringBuilder();

        // Read the request
        int c;
        while ((c = reader.read()) != -1) {
            request.append((char) c);
            if (request.toString().endsWith("\r\n\r\n")) {
                break;
            }
        }

        return request.toString();
    }

    private void sendAllServices(OutputStream writer) {
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
            writer.write(response.toString().getBytes(UTF_8));
        } catch (IOException e) {
            logger.severe("Server error: " + e.getMessage());
        }
    }

    public void addServiceEndpoint(WebPageService<?,?> codaService) {
        String endpoint = codaService.endpoint();
        logger.info("Adding service endpoint: " + endpoint);
    }
}
