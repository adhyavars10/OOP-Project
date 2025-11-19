package handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;

/**
 * Handler for health check endpoint
 * Used by hosting services to verify server is running
 */
public class HealthCheckHandler extends BaseHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        setCORSHeaders(exchange);

        String method = exchange.getRequestMethod();

        if ("OPTIONS".equals(method)) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        if ("GET".equals(method)) {
            sendTextResponse(exchange, 200, "OK");
        } else {
            sendErrorResponse(exchange, 405, "Method not allowed");
        }
    }
}
