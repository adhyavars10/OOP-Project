package handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import exceptions.DatabaseException;
import services.FinanceService;
import utils.Logger;

import java.io.IOException;
import java.util.Map;

/**
 * Handler for data export API endpoint
 * Exports expenses to CSV format
 */
public class ExportHandler extends BaseHandler implements HttpHandler {

    private FinanceService financeService;

    public ExportHandler() {
        try {
            this.financeService = new FinanceService();
        } catch (DatabaseException e) {
            Logger.logError("Failed to initialize FinanceService in ExportHandler: " + e.getMessage());
        }
    }

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
            handleExportCSV(exchange);
        } else {
            sendErrorResponse(exchange, 405, "Method not allowed");
        }
    }

    /**
     * Handle GET request - export expenses to CSV
     */
    private void handleExportCSV(HttpExchange exchange) throws IOException {
        try {
            // Export expenses to CSV file
            String fileName = financeService.exportExpensesToCSV();

            // Send success response with filename
            Map<String, Object> response = createSuccessResponse("Data exported successfully");
            response.put("fileName", fileName);
            sendJSONResponse(exchange, 200, response);

            Logger.log("Data exported to CSV: " + fileName);

        } catch (DatabaseException e) {
            Logger.logError("Failed to export data: " + e.getMessage());
            sendErrorResponse(exchange, 500, "Failed to export data");
        } catch (IOException e) {
            Logger.logError("Failed to write CSV file: " + e.getMessage());
            sendErrorResponse(exchange, 500, "Failed to create CSV file");
        }
    }
}
