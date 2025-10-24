import java.io.*;
import java.net.InetSocketAddress;
import java.nio.file.*;
import java.util.*;
import com.sun.net.httpserver.*;

public class Main {
    // Simple in-memory parking data
    private static Map<String, String> parkingSlots = new LinkedHashMap<>();
    private static final int TOTAL_SLOTS = 5;

    public static void main(String[] args) throws IOException {
        // Initialize parking slots as empty
        for (int i = 1; i <= TOTAL_SLOTS; i++) {
            parkingSlots.put("Slot-" + i, "Empty");
        }

        int port = 8080;
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        // Serve static frontend files
        server.createContext("/", new FrontendHandler());

        // API routes
        server.createContext("/api/status", new StatusHandler());
        server.createContext("/api/park", new ParkHandler());
        server.createContext("/api/remove", new RemoveHandler());

        server.setExecutor(null);
        server.start();
        System.out.println("🚗 Parking Lot Server running at http://localhost:" + port);
    }

    // === Serve frontend files ===
    static class FrontendHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String uri = exchange.getRequestURI().getPath();
            if (uri.equals("/")) uri = "/index.html";

            Path filePath = Paths.get("../frontend" + uri).normalize();

            if (!Files.exists(filePath)) {
                String notFound = "404 Not Found";
                exchange.sendResponseHeaders(404, notFound.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(notFound.getBytes());
                }
                return;
            }

            String contentType = "text/plain";
            if (uri.endsWith(".html")) contentType = "text/html";
            else if (uri.endsWith(".js")) contentType = "application/javascript";
            else if (uri.endsWith(".css")) contentType = "text/css";

            exchange.getResponseHeaders().add("Content-Type", contentType);
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");

            byte[] bytes = Files.readAllBytes(filePath);
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }
    }

    // === GET /api/status ===
    static class StatusHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().add("Content-Type", "application/json");

            String json = new StringBuilder("{\"slots\": [")
                    .append(parkingSlots.entrySet().stream()
                            .map(e -> "{\"slot\":\"" + e.getKey() + "\",\"vehicle\":\"" + e.getValue() + "\"}")
                            .reduce((a, b) -> a + "," + b).orElse(""))
                    .append("]}").toString();

            exchange.sendResponseHeaders(200, json.getBytes().length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(json.getBytes());
            }
        }
    }

    // === POST /api/park ===
    static class ParkHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }

            String body = new String(exchange.getRequestBody().readAllBytes());
            String vehicle = body.replaceAll("[^a-zA-Z0-9]", "");

String message = "No empty slots available!";
            for (Map.Entry<String, String> entry : parkingSlots.entrySet()) {
                if (entry.getValue().equals("Empty")) {
                    entry.setValue(vehicle);
                    message = vehicle + " parked in " + entry.getKey();
                    break;
                }
            }

            String response = "{\"message\": \"" + message + "\"}";
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.getBytes().length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        }
    }

    // === POST /api/remove ===
    static class RemoveHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }

            String body = new String(exchange.getRequestBody().readAllBytes());
            String vehicle = body.replaceAll("[^a-zA-Z0-9]", "");

            String message = "Vehicle not found!";
            for (Map.Entry<String, String> entry : parkingSlots.entrySet()) {
                if (entry.getValue().equalsIgnoreCase(vehicle)) {
                    entry.setValue("Empty");
                    message = vehicle + " removed from " + entry.getKey();
                    break;
                }
            }

            String response = "{\"message\": \"" + message + "\"}";
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.getBytes().length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        }
    }
}