package coda.app;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.*;
import java.util.logging.Logger;
import javax.servlet.*;
import javax.servlet.http.*;


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
            HttpServletRequest request = readRequest(reader);
            logger.info("Request: " + request);

            // Send the response
            sendResponse(request, writer);
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
    private void sendResponse(HttpServletRequest request, OutputStream writer) {
        matchRequestToService(request, writer);
    }


    /**
     * Match the request to a service.
     *
     * @param request The request
     * @param writer The output stream
     */
    private void matchRequestToService(HttpServletRequest request, OutputStream writer) {
        if (request.getRequestURI().equals("/rendered-image")) {
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
            if (request.getRequestURI().equals("/" + service.endpoint())) {
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
    private HttpServletRequest readRequest(BufferedInputStream reader) throws IOException {
        StringBuilder requestBuilder = new StringBuilder();
        Map<String, String> headers = new HashMap<>();
        String requestURI = "";

        // Read the request
        int c;
        while ((c = reader.read()) != -1) {
            requestBuilder.append((char) c);
            if (requestBuilder.toString().endsWith("\r\n\r\n")) {
                break;
            }
        }

        String request = requestBuilder.toString();
        String[] lines = request.split("\r\n");

        // Parse the request line
        if (lines.length > 0) {
            String[] requestLineTokens = lines[0].split(" ");
            if (requestLineTokens.length > 1) {
                requestURI = requestLineTokens[1];
            }
        }

        // Parse headers
        for (int i = 1; i < lines.length; i++) {
            String line = lines[i];
            if (line.isEmpty()) {
                break;
            }
            String[] headerTokens = line.split(": ");
            if (headerTokens.length == 2) {
                headers.put(headerTokens[0], headerTokens[1]);
            }
        }

        return new CustomHttpServletRequest(requestURI, headers);
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

    private class CustomHttpServletRequest implements HttpServletRequest {
        private final Map<String, String> headers = new HashMap<>();
        private final String requestURI;

        public CustomHttpServletRequest(String requestURI, Map<String, String> headers) {
            this.requestURI = requestURI;
            this.headers.putAll(headers);
        }

        @Override
        public String getAuthType() {
            return null;
        }

        @Override
        public Cookie[] getCookies() {
            return new Cookie[0];
        }

        @Override
        public long getDateHeader(String s) {
            return 0;
        }

        @Override
        public String getHeader(String s) {
            return null;
        }

        @Override
        public Enumeration<String> getHeaders(String s) {
            return null;
        }

        @Override
        public Enumeration<String> getHeaderNames() {
            return null;
        }

        @Override
        public int getIntHeader(String s) {
            return 0;
        }

        @Override
        public String getMethod() {
            return null;
        }

        @Override
        public String getPathInfo() {
            return null;
        }

        @Override
        public String getPathTranslated() {
            return null;
        }

        @Override
        public String getContextPath() {
            return null;
        }

        @Override
        public String getQueryString() {
            return null;
        }

        @Override
        public String getRemoteUser() {
            return null;
        }

        @Override
        public boolean isUserInRole(String s) {
            return false;
        }

        @Override
        public Principal getUserPrincipal() {
            return null;
        }

        @Override
        public String getRequestedSessionId() {
            return null;
        }

        @Override
        public String getRequestURI() {
            return requestURI;
        }

        @Override
        public StringBuffer getRequestURL() {
            return null;
        }

        @Override
        public String getServletPath() {
            return null;
        }

        @Override
        public HttpSession getSession(boolean b) {
            return null;
        }

        @Override
        public HttpSession getSession() {
            return null;
        }

        @Override
        public String changeSessionId() {
            return null;
        }

        @Override
        public boolean isRequestedSessionIdValid() {
            return false;
        }

        @Override
        public boolean isRequestedSessionIdFromCookie() {
            return false;
        }

        @Override
        public boolean isRequestedSessionIdFromURL() {
            return false;
        }

        /**
         * @deprecated
         */
        @Override
        public boolean isRequestedSessionIdFromUrl() {
            return false;
        }

        @Override
        public boolean authenticate(HttpServletResponse httpServletResponse) throws IOException, ServletException {
            return false;
        }

        @Override
        public void login(String s, String s1) throws ServletException {

        }

        @Override
        public void logout() throws ServletException {

        }

        @Override
        public Collection<Part> getParts() throws IOException, ServletException {
            return null;
        }

        @Override
        public Part getPart(String s) throws IOException, ServletException {
            return null;
        }

        @Override
        public <T extends HttpUpgradeHandler> T upgrade(Class<T> aClass) throws IOException, ServletException {
            return null;
        }

        @Override
        public Object getAttribute(String s) {
            return null;
        }

        @Override
        public Enumeration<String> getAttributeNames() {
            return null;
        }

        @Override
        public String getCharacterEncoding() {
            return null;
        }

        @Override
        public void setCharacterEncoding(String s) throws UnsupportedEncodingException {

        }

        @Override
        public int getContentLength() {
            return 0;
        }

        @Override
        public long getContentLengthLong() {
            return 0;
        }

        @Override
        public String getContentType() {
            return null;
        }

        @Override
        public ServletInputStream getInputStream() throws IOException {
            return null;
        }

        @Override
        public String getParameter(String s) {
            return null;
        }

        @Override
        public Enumeration<String> getParameterNames() {
            return null;
        }

        @Override
        public String[] getParameterValues(String s) {
            return new String[0];
        }

        @Override
        public Map<String, String[]> getParameterMap() {
            return null;
        }

        @Override
        public String getProtocol() {
            return null;
        }

        @Override
        public String getScheme() {
            return null;
        }

        @Override
        public String getServerName() {
            return null;
        }

        @Override
        public int getServerPort() {
            return 0;
        }

        @Override
        public BufferedReader getReader() throws IOException {
            return null;
        }

        @Override
        public String getRemoteAddr() {
            return null;
        }

        @Override
        public String getRemoteHost() {
            return null;
        }

        @Override
        public void setAttribute(String s, Object o) {

        }

        @Override
        public void removeAttribute(String s) {

        }

        @Override
        public Locale getLocale() {
            return null;
        }

        @Override
        public Enumeration<Locale> getLocales() {
            return null;
        }

        @Override
        public boolean isSecure() {
            return false;
        }

        @Override
        public RequestDispatcher getRequestDispatcher(String s) {
            return null;
        }

        /**
         * @param s
         * @deprecated
         */
        @Override
        public String getRealPath(String s) {
            return null;
        }

        @Override
        public int getRemotePort() {
            return 0;
        }

        @Override
        public String getLocalName() {
            return null;
        }

        @Override
        public String getLocalAddr() {
            return null;
        }

        @Override
        public int getLocalPort() {
            return 0;
        }

        @Override
        public ServletContext getServletContext() {
            return null;
        }

        @Override
        public AsyncContext startAsync() throws IllegalStateException {
            return null;
        }

        @Override
        public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse) throws IllegalStateException {
            return null;
        }

        @Override
        public boolean isAsyncStarted() {
            return false;
        }

        @Override
        public boolean isAsyncSupported() {
            return false;
        }

        @Override
        public AsyncContext getAsyncContext() {
            return null;
        }

        @Override
        public DispatcherType getDispatcherType() {
            return null;
        }
    }
}
