package utils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import models.Expense;
import models.Income;

public class CSVExporter {

    public static String exportExpenses(List<Expense> expenses)
        throws IOException {
        String timestamp = LocalDateTime.now().format(
            DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")
        );
        String fileName = "expenses_export_" + timestamp + ".csv";

        try (
            BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))
        ) {
            writer.write("ID,Date,Amount,Category,Description");
            writer.newLine();

            for (Expense expense : expenses) {
                writer.write(expense.getId() + ",");
                writer.write(expense.getDate().toString() + ",");
                writer.write(expense.getAmount() + ",");
                writer.write(expense.getCategory().getDisplayName() + ",");
                writer.write("\"" + expense.getDescription() + "\"");
                writer.newLine();
            }

            Logger.log(
                "Exported " + expenses.size() + " expenses to " + fileName
            );
        } catch (IOException e) {
            Logger.logError("Failed to export expenses: " + e.getMessage());
            throw e;
        }

        return fileName;
    }

    public static String exportIncome(List<Income> incomeList)
        throws IOException {
        String timestamp = LocalDateTime.now().format(
            DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")
        );
        String fileName = "income_export_" + timestamp + ".csv";

        try (
            BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))
        ) {
            writer.write("ID,Date,Amount,Source,Description");
            writer.newLine();

            for (Income income : incomeList) {
                writer.write(income.getId() + ",");
                writer.write(income.getDate().toString() + ",");
                writer.write(income.getAmount() + ",");
                writer.write("\"" + income.getSource() + "\",");
                writer.write("\"" + income.getDescription() + "\"");
                writer.newLine();
            }

            Logger.log(
                "Exported " +
                    incomeList.size() +
                    " income records to " +
                    fileName
            );
        } catch (IOException e) {
            Logger.logError("Failed to export income: " + e.getMessage());
            throw e;
        }

        return fileName;
    }
}
