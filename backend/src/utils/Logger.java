package utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Logger {

    private static final String LOG_FILE = "finance.log";
    private static final DateTimeFormatter formatter =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static void log(String message) {
        String timestamp = LocalDateTime.now().format(formatter);
        String logEntry = "[" + timestamp + "] " + message;

        try (
            BufferedWriter writer = new BufferedWriter(
                new FileWriter(LOG_FILE, true)
            )
        ) {
            writer.write(logEntry);
            writer.newLine();
            System.out.println(logEntry);
        } catch (IOException e) {
            System.err.println(
                "Failed to write to log file: " + e.getMessage()
            );
        }
    }

    public static void logError(String errorMessage) {
        String timestamp = LocalDateTime.now().format(formatter);
        String logEntry = "[" + timestamp + "] ERROR: " + errorMessage;

        try (
            BufferedWriter writer = new BufferedWriter(
                new FileWriter(LOG_FILE, true)
            )
        ) {
            writer.write(logEntry);
            writer.newLine();
            System.err.println(logEntry);
        } catch (IOException e) {
            System.err.println(
                "Failed to write error to log file: " + e.getMessage()
            );
        }
    }

    public static String getLogContents() {
        StringBuilder content = new StringBuilder();
        try (
            BufferedReader reader = new BufferedReader(new FileReader(LOG_FILE))
        ) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        } catch (IOException e) {
            return "Error reading log file: " + e.getMessage();
        }
        return content.toString();
    }
}
