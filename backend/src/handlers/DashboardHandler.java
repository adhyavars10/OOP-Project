package handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import exceptions.DatabaseException;
import models.Category;
import services.FinanceService;
import utils.Logger;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Handler for dashboard API endpoint
 * Returns summary statistics including totals and category breakdown
 */
public class DashboardHandler extends BaseHandler implements HttpHandler {

    private FinanceService financeService;

    public DashboardHandler() {
        try {
            this.financeService = new FinanceService();
        } catch (DatabaseException e) {
            Logger.logError("Failed to initialize FinanceService in DashboardHandler: " + e.getMessage());
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
            handleGetDashboard(exchange);
        } else {
            sendErrorResponse(exchange, 405, "Method not allowed");
        }
    }

    /**
     * Handle GET request - retrieve dashboard summary data
     */
    private void handleGetDashboard(HttpExchange exchange) throws IOException {
        try {
            // Get financial summary
            double totalIncome = financeService.getTotalIncome();
            double totalExpenses = financeService.getTotalExpenses();
            double balance = financeService.getBalance();
            Map<Category, Double> categoryData = financeService.getExpensesByCategory();

            // Build response
            Map<String, Object> dashboard = new HashMap<>();
            dashboard.put("totalIncome", totalIncome);
            dashboard.put("totalExpenses", totalExpenses);
            dashboard.put("balance", balance);

            // Convert category data to a more JSON-friendly format
            Map<String, Object> categoryMap = new HashMap<>();
            for (Map.Entry<Category, Double> entry : categoryData.entrySet()) {
                Map<String, Object> catInfo = new HashMap<>();
                catInfo.put("amount", entry.getValue());
                catInfo.put("name", entry.getKey().getDisplayName());
                catInfo.put("emoji", entry.getKey().getEmoji());
                catInfo.put("color", entry.getKey().getColor());
                categoryMap.put(entry.getKey().name(), catInfo);
            }
            dashboard.put("categoryData", categoryMap);

            sendJSONResponse(exchange, 200, dashboard);

            Logger.log("Dashboard data requested - Balance: " + balance);

        } catch (DatabaseException e) {
            Logger.logError("Failed to get dashboard data: " + e.getMessage());
            sendErrorResponse(exchange, 500, "Failed to retrieve dashboard data");
        }
    }
}
