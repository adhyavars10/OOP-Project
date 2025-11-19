package handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import exceptions.DatabaseException;
import exceptions.InvalidAmountException;
import models.Income;
import services.FinanceService;
import utils.Logger;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Handler for income-related API endpoints
 * Handles GET, POST, and DELETE operations for income records
 */
public class IncomeHandler extends BaseHandler implements HttpHandler {

    private FinanceService financeService;

    public IncomeHandler() {
        try {
            this.financeService = new FinanceService();
        } catch (DatabaseException e) {
            Logger.logError("Failed to initialize FinanceService in IncomeHandler: " + e.getMessage());
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

        try {
            switch (method) {
                case "GET":
                    handleGetIncome(exchange);
                    break;
                case "POST":
                    handleAddIncome(exchange);
                    break;
                case "DELETE":
                    handleDeleteIncome(exchange);
                    break;
                default:
                    sendErrorResponse(exchange, 405, "Method not allowed");
            }
        } catch (Exception e) {
            Logger.logError("Error in IncomeHandler: " + e.getMessage());
            sendErrorResponse(exchange, 500, "Internal server error: " + e.getMessage());
        }
    }

    /**
     * Handle GET request - retrieve all income records
     */
    private void handleGetIncome(HttpExchange exchange) throws IOException {
        try {
            List<Income> incomeList = financeService.getAllIncome();
            sendJSONResponse(exchange, 200, incomeList);
        } catch (DatabaseException e) {
            Logger.logError("Failed to get income: " + e.getMessage());
            sendErrorResponse(exchange, 500, "Failed to retrieve income");
        }
    }

    /**
     * Handle POST request - add new income
     */
    private void handleAddIncome(HttpExchange exchange) throws IOException {
        try {
            // Read and parse form data
            String body = readRequestBody(exchange);
            Map<String, String> params = parseFormData(body);

            // Validate required fields
            if (!params.containsKey("amount") || !params.containsKey("description") || !params.containsKey("source")) {
                sendErrorResponse(exchange, 400, "Missing required fields");
                return;
            }

            // Parse parameters
            double amount = Double.parseDouble(params.get("amount"));
            String description = params.get("description");
            String source = params.get("source");

            // Create and add income
            Income income = new Income(amount, description, source);
            financeService.addIncome(income);

            // Send success response
            Map<String, Object> response = createSuccessResponse("Income added successfully");
            response.put("id", income.getId());
            sendJSONResponse(exchange, 200, response);

        } catch (NumberFormatException e) {
            sendErrorResponse(exchange, 400, "Invalid amount format");
        } catch (InvalidAmountException e) {
            sendErrorResponse(exchange, 400, e.getMessage());
        } catch (DatabaseException e) {
            Logger.logError("Failed to add income: " + e.getMessage());
            sendErrorResponse(exchange, 500, "Failed to add income");
        }
    }

    /**
     * Handle DELETE request - delete income by ID
     */
    private void handleDeleteIncome(HttpExchange exchange) throws IOException {
        try {
            // Get ID from path (e.g., /api/income/123)
            String id = getPathParameter(exchange, 3);

            if (id == null || id.isEmpty()) {
                sendErrorResponse(exchange, 400, "Income ID is required");
                return;
            }

            // Delete income
            boolean deleted = financeService.deleteIncome(id);

            if (deleted) {
                sendJSONResponse(exchange, 200, createSuccessResponse("Income deleted successfully"));
            } else {
                sendErrorResponse(exchange, 404, "Income not found");
            }

        } catch (DatabaseException e) {
            Logger.logError("Failed to delete income: " + e.getMessage());
            sendErrorResponse(exchange, 500, "Failed to delete income");
        }
    }
}
