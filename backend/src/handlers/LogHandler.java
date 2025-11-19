package handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import utils.Logger;

import java.io.IOException;

/**
 * Handler for logs API endpoint
 * Returns application log file contents
 */
public class LogHandler extends BaseHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        setCORSHeaders(exchange);

        String method = exchange.getRequestMethod();

        // Handle CORS preflight
        if ("OPTIONS".equals(method)) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        if ("GET".equals(method)) {
            handleGetLogs(exchange);
        } else {
            sendErrorResponse(exchange, 405, "Method not allowed");
        }
    }

    /**
     * Handle GET request - retrieve application logs
     */
    private void handleGetLogs(HttpExchange exchange) throws IOException {
        try {
            // Get log file contents
            String logContents = Logger.getLogContents();

            // Send as plain text
            sendTextResponse(exchange, 200, logContents);

        } catch (Exception e) {
            sendErrorResponse(exchange, 500, "Failed to read log file");
        }
    }
}
