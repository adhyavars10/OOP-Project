package handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import exceptions.DatabaseException;
import exceptions.InvalidAmountException;
import models.Category;
import models.Expense;
import services.FinanceService;
import utils.Logger;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Handler for expense-related API endpoints
 * Handles GET, POST, and DELETE operations for expenses
 */
public class ExpenseHandler extends BaseHandler implements HttpHandler {

    private FinanceService financeService;

    public ExpenseHandler() {
        try {
            this.financeService = new FinanceService();
        } catch (DatabaseException e) {
            Logger.logError("Failed to initialize FinanceService in ExpenseHandler: " + e.getMessage());
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
                    handleGetExpenses(exchange);
                    break;
                case "POST":
                    handleAddExpense(exchange);
                    break;
                case "DELETE":
                    handleDeleteExpense(exchange);
                    break;
                default:
                    sendErrorResponse(exchange, 405, "Method not allowed");
            }
        } catch (Exception e) {
            Logger.logError("Error in ExpenseHandler: " + e.getMessage());
            sendErrorResponse(exchange, 500, "Internal server error: " + e.getMessage());
        }
    }

    /**
     * Handle GET request - retrieve all expenses
     */
    private void handleGetExpenses(HttpExchange exchange) throws IOException {
        try {
            List<Expense> expenses = financeService.getAllExpenses();
            sendJSONResponse(exchange, 200, expenses);
        } catch (DatabaseException e) {
            Logger.logError("Failed to get expenses: " + e.getMessage());
            sendErrorResponse(exchange, 500, "Failed to retrieve expenses");
        }
    }

    /**
     * Handle POST request - add new expense
     */
    private void handleAddExpense(HttpExchange exchange) throws IOException {
        try {
            // Read and parse form data
            String body = readRequestBody(exchange);
            Map<String, String> params = parseFormData(body);

            // Validate required fields
            if (!params.containsKey("amount") || !params.containsKey("description") || !params.containsKey("category")) {
                sendErrorResponse(exchange, 400, "Missing required fields");
                return;
            }

            // Parse parameters
            double amount = Double.parseDouble(params.get("amount"));
            String description = params.get("description");
            Category category = Category.valueOf(params.get("category").toUpperCase());

            // Create and add expense
            Expense expense = new Expense(amount, description, category);
            financeService.addExpense(expense);

            // Send success response
            Map<String, Object> response = createSuccessResponse("Expense added successfully");
            response.put("id", expense.getId());
            sendJSONResponse(exchange, 200, response);

        } catch (NumberFormatException e) {
            sendErrorResponse(exchange, 400, "Invalid amount format");
        } catch (IllegalArgumentException e) {
            sendErrorResponse(exchange, 400, "Invalid category");
        } catch (InvalidAmountException e) {
            sendErrorResponse(exchange, 400, e.getMessage());
        } catch (DatabaseException e) {
            Logger.logError("Failed to add expense: " + e.getMessage());
            sendErrorResponse(exchange, 500, "Failed to add expense");
        }
    }

    /**
     * Handle DELETE request - delete expense by ID
     */
    private void handleDeleteExpense(HttpExchange exchange) throws IOException {
        try {
            // Get ID from path (e.g., /api/expenses/123)
            String id = getPathParameter(exchange, 3);

            if (id == null || id.isEmpty()) {
                sendErrorResponse(exchange, 400, "Expense ID is required");
                return;
            }

            // Delete expense
            boolean deleted = financeService.deleteExpense(id);

            if (deleted) {
                sendJSONResponse(exchange, 200, createSuccessResponse("Expense deleted successfully"));
            } else {
                sendErrorResponse(exchange, 404, "Expense not found");
            }

        } catch (DatabaseException e) {
            Logger.logError("Failed to delete expense: " + e.getMessage());
            sendErrorResponse(exchange, 500, "Failed to delete expense");
        }
    }
}
