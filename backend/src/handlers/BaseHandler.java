package handlers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;

import java.io.*;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Base handler class with common HTTP utilities
 * All API handlers extend this class
 */
public abstract class BaseHandler {

    protected static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    /**
     * Set CORS headers for cross-origin requests
     */
    protected void setCORSHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, DELETE, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
    }

    /**
     * Send JSON response
     */
    protected void sendJSONResponse(HttpExchange exchange, int statusCode, Object data) throws IOException {
        String jsonResponse = gson.toJson(data);
        byte[] bytes = jsonResponse.getBytes(StandardCharsets.UTF_8);

        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, bytes.length);

        OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.close();
    }

    /**
     * Send plain text response
     */
    protected void sendTextResponse(HttpExchange exchange, int statusCode, String text) throws IOException {
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);

        exchange.getResponseHeaders().add("Content-Type", "text/plain");
        exchange.sendResponseHeaders(statusCode, bytes.length);

        OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.close();
    }

    /**
     * Send error response
     */
    protected void sendErrorResponse(HttpExchange exchange, int statusCode, String errorMessage) throws IOException {
        Map<String, String> error = new HashMap<>();
        error.put("error", errorMessage);
        sendJSONResponse(exchange, statusCode, error);
    }

    /**
     * Read request body
     */
    protected String readRequestBody(HttpExchange exchange) throws IOException {
        InputStream inputStream = exchange.getRequestBody();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        StringBuilder body = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            body.append(line);
        }
        return body.toString();
    }

    /**
     * Parse form data from request body
     */
    protected Map<String, String> parseFormData(String body) {
        Map<String, String> params = new HashMap<>();
        if (body == null || body.isEmpty()) {
            return params;
        }

        String[] pairs = body.split("&");
        for (String pair : pairs) {
            String[] keyValue = pair.split("=");
            if (keyValue.length == 2) {
                try {
                    String key = URLDecoder.decode(keyValue[0], "UTF-8");
                    String value = URLDecoder.decode(keyValue[1], "UTF-8");
                    params.put(key, value);
                } catch (UnsupportedEncodingException e) {
                    // Skip this pair if decoding fails
                }
            }
        }
        return params;
    }

    /**
     * Get path parameter (e.g., /api/expenses/123 -> "123")
     */
    protected String getPathParameter(HttpExchange exchange, int index) {
        String path = exchange.getRequestURI().getPath();
        String[] parts = path.split("/");
        if (parts.length > index) {
            return parts[index];
        }
        return null;
    }

    /**
     * Create success response map
     */
    protected Map<String, Object> createSuccessResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", message);
        return response;
    }

    /**
     * Create success response with data
     */
    protected Map<String, Object> createSuccessResponse(String message, Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", message);
        response.put("data", data);
        return response;
    }
}
