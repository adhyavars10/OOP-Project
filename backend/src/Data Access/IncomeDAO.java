package dao;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import exceptions.DatabaseException;
import models.Income;
import org.bson.Document;
import utils.DatabaseConnection;
import utils.Logger;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Income operations
 * Handles all database operations for income records
 */
public class IncomeDAO {

    private MongoCollection<Document> collection;

    public IncomeDAO() throws DatabaseException {
        MongoDatabase database = DatabaseConnection.getInstance().getDatabase();
        collection = database.getCollection("income");
        Logger.log("IncomeDAO initialized");
    }

    /**
     * Add a new income record to database
     */
    public void addIncome(Income income) throws DatabaseException {
        try {
            // Convert Income object to MongoDB Document
            Document doc = new Document()
                    .append("id", income.getId())
                    .append("amount", income.getAmount())
                    .append("date", income.getDate().toString())
                    .append("description", income.getDescription())
                    .append("source", income.getSource());

            collection.insertOne(doc);
            Logger.log("Added income: " + income.getId());
        } catch (Exception e) {
            Logger.logError("Failed to add income: " + e.getMessage());
            throw new DatabaseException("Failed to add income", e);
        }
    }

    /**
     * Get all income records from database
     */
    public List<Income> getAllIncome() throws DatabaseException {
        List<Income> incomeList = new ArrayList<>();
        try {
            for (Document doc : collection.find()) {
                Income income = documentToIncome(doc);
                if (income != null) {
                    incomeList.add(income);
                }
            }
            Logger.log("Retrieved " + incomeList.size() + " income records");
        } catch (Exception e) {
            Logger.logError("Failed to retrieve income: " + e.getMessage());
            throw new DatabaseException("Failed to retrieve income", e);
        }
        return incomeList;
    }

    /**
     * Delete an income record by ID
     */
    public boolean deleteIncome(String id) throws DatabaseException {
        try {
            Document query = new Document("id", id);
            long deletedCount = collection.deleteOne(query).getDeletedCount();

            if (deletedCount > 0) {
                Logger.log("Deleted income: " + id);
                return true;
            }
            return false;
        } catch (Exception e) {
            Logger.logError("Failed to delete income: " + e.getMessage());
            throw new DatabaseException("Failed to delete income", e);
        }
    }

    /**
     * Get income by ID
     */
    public Income getIncomeById(String id) throws DatabaseException {
        try {
            Document query = new Document("id", id);
            Document doc = collection.find(query).first();
            return documentToIncome(doc);
        } catch (Exception e) {
            Logger.logError("Failed to get income by ID: " + e.getMessage());
            throw new DatabaseException("Failed to get income", e);
        }
    }

    /**
     * Convert MongoDB Document to Income object
     */
    private Income documentToIncome(Document doc) {
        if (doc == null) {
            return null;
        }
        try {
            String id = doc.getString("id");
            double amount = doc.getDouble("amount");
            LocalDate date = LocalDate.parse(doc.getString("date"));
            String description = doc.getString("description");
            String source = doc.getString("source");

            return new Income(id, amount, date, description, source);
        } catch (Exception e) {
            Logger.logError("Failed to convert document to Income: " + e.getMessage());
            return null;
        }
    }
}
