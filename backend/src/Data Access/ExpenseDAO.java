package dao;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import exceptions.DatabaseException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import models.Category;
import models.Expense;
import org.bson.Document;
import utils.DatabaseConnection;
import utils.Logger;

public class ExpenseDAO {

    private MongoCollection<Document> collection;

    public ExpenseDAO() throws DatabaseException {
        MongoDatabase database = DatabaseConnection.getInstance().getDatabase();
        collection = database.getCollection("expenses");
        Logger.log("ExpenseDAO initialized");
    }

    /**
     * Add a new expense to database
     */
    public void addExpense(Expense expense) throws DatabaseException {
        try {
            // Convert Expense object to MongoDB Document
            Document doc = new Document()
                .append("id", expense.getId())
                .append("amount", expense.getAmount())
                .append("date", expense.getDate().toString())
                .append("description", expense.getDescription())
                .append("category", expense.getCategory().name());

            collection.insertOne(doc);
            Logger.log("Added expense: " + expense.getId());
        } catch (Exception e) {
            Logger.logError("Failed to add expense: " + e.getMessage());
            throw new DatabaseException("Failed to add expense", e);
        }
    }

    /**
     * Get all expenses from database
     */
    public List<Expense> getAllExpenses() throws DatabaseException {
        List<Expense> expenses = new ArrayList<>();
        try {
            for (Document doc : collection.find()) {
                Expense expense = documentToExpense(doc);
                if (expense != null) {
                    expenses.add(expense);
                }
            }
            Logger.log("Retrieved " + expenses.size() + " expenses");
        } catch (Exception e) {
            Logger.logError("Failed to retrieve expenses: " + e.getMessage());
            throw new DatabaseException("Failed to retrieve expenses", e);
        }
        return expenses;
    }

    /**
     * Delete an expense by ID
     */
    public boolean deleteExpense(String id) throws DatabaseException {
        try {
            Document query = new Document("id", id);
            long deletedCount = collection.deleteOne(query).getDeletedCount();

            if (deletedCount > 0) {
                Logger.log("Deleted expense: " + id);
                return true;
            }
            return false;
        } catch (Exception e) {
            Logger.logError("Failed to delete expense: " + e.getMessage());
            throw new DatabaseException("Failed to delete expense", e);
        }
    }

    /**
     * Get expense by ID
     */
    public Expense getExpenseById(String id) throws DatabaseException {
        try {
            Document query = new Document("id", id);
            Document doc = collection.find(query).first();
            return documentToExpense(doc);
        } catch (Exception e) {
            Logger.logError("Failed to get expense by ID: " + e.getMessage());
            throw new DatabaseException("Failed to get expense", e);
        }
    }

    /**
     * Convert MongoDB Document to Expense object
     */
    private Expense documentToExpense(Document doc) {
        if (doc == null) {
            return null;
        }
        try {
            String id = doc.getString("id");
            double amount = doc.getDouble("amount");
            LocalDate date = LocalDate.parse(doc.getString("date"));
            String description = doc.getString("description");
            Category category = Category.valueOf(doc.getString("category"));

            return new Expense(id, amount, date, description, category);
        } catch (Exception e) {
            Logger.logError(
                "Failed to convert document to Expense: " + e.getMessage()
            );
            return null;
        }
    }
}
