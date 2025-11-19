import com.sun.net.httpserver.HttpServer;
import handlers.*;
import java.io.IOException;
import java.net.InetSocketAddress;
import utils.DatabaseConnection;
import utils.Logger;

public class Main {

    public static void main(String[] args) {
        try {
            String portStr = System.getenv("PORT");
            int port = (portStr != null) ? Integer.parseInt(portStr) : 8080;

            DatabaseConnection.initialize();
            Logger.log("Database connection initialized");

            HttpServer server = HttpServer.create(
                new InetSocketAddress(port),
                0
            );

            server.createContext("/health", new HealthCheckHandler());
            server.createContext("/api/expenses", new ExpenseHandler());
            server.createContext("/api/income", new IncomeHandler());
            server.createContext("/api/dashboard", new DashboardHandler());
            server.createContext("/api/export", new ExportHandler());
            server.createContext("/api/logs", new LogHandler());

            server.setExecutor(null);
            server.start();

            System.out.println("===========================================");
            System.out.println("💰 Finance Tracker Backend Started!");
            System.out.println("🌐 Server running on port: " + port);
            System.out.println("===========================================");

            Logger.log("Server started successfully on port " + port);
        } catch (IOException e) {
            System.err.println("Failed to start server: " + e.getMessage());
            Logger.logError("Server startup failed: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            Logger.logError("Unexpected error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
