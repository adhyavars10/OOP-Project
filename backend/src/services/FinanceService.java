package services;

import dao.ExpenseDAO;
import dao.IncomeDAO;
import exceptions.DatabaseException;
import exceptions.InvalidAmountException;
import models.Category;
import models.Expense;
import models.Income;
import utils.CSVExporter;
import utils.Logger;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service class for finance operations
 * Contains business logic for managing expenses and income
 */
public class FinanceService {

    private ExpenseDAO expenseDAO;
    private IncomeDAO incomeDAO;

    public FinanceService() throws DatabaseException {
        this.expenseDAO = new ExpenseDAO();
        this.incomeDAO = new IncomeDAO();
        Logger.log("FinanceService initialized");
    }

    /**
     * Add a new expense
     */
    public void addExpense(Expense expense) throws InvalidAmountException, DatabaseException {
        // Validate before adding
        expense.validate();
        expenseDAO.addExpense(expense);
        Logger.log("Expense added: " + expense.getAmount() + " - " + expense.getCategory());
    }

    /**
     * Get all expenses
     */
    public List<Expense> getAllExpenses() throws DatabaseException {
        return expenseDAO.getAllExpenses();
    }

    /**
     * Delete an expense
     */
    public boolean deleteExpense(String id) throws DatabaseException {
        return expenseDAO.deleteExpense(id);
    }

    /**
     * Add a new income
     */
    public void addIncome(Income income) throws InvalidAmountException, DatabaseException {
        // Validate before adding
        income.validate();
        incomeDAO.addIncome(income);
        Logger.log("Income added: " + income.getAmount() + " - " + income.getSource());
    }

    /**
     * Get all income records
     */
    public List<Income> getAllIncome() throws DatabaseException {
        return incomeDAO.getAllIncome();
    }

    /**
     * Delete an income record
     */
    public boolean deleteIncome(String id) throws DatabaseException {
        return incomeDAO.deleteIncome(id);
    }

    /**
     * Calculate total expenses
     */
    public double getTotalExpenses() throws DatabaseException {
        List<Expense> expenses = expenseDAO.getAllExpenses();
        double total = 0;
        for (Expense expense : expenses) {
            total += expense.getAmount();
        }
        return total;
    }

    /**
     * Calculate total income
     */
    public double getTotalIncome() throws DatabaseException {
        List<Income> incomeList = incomeDAO.getAllIncome();
        double total = 0;
        for (Income income : incomeList) {
            total += income.getAmount();
        }
        return total;
    }

    /**
     * Calculate current balance
     */
    public double getBalance() throws DatabaseException {
        return getTotalIncome() - getTotalExpenses();
    }

    /**
     * Get expenses grouped by category
     */
    public Map<Category, Double> getExpensesByCategory() throws DatabaseException {
        List<Expense> expenses = expenseDAO.getAllExpenses();
        Map<Category, Double> categoryTotals = new HashMap<>();

        for (Expense expense : expenses) {
            Category category = expense.getCategory();
            double currentAmount = categoryTotals.getOrDefault(category, 0.0);
            categoryTotals.put(category, currentAmount + expense.getAmount());
        }

        return categoryTotals;
    }

    /**
     * Export expenses to CSV
     */
    public String exportExpensesToCSV() throws DatabaseException, IOException {
        List<Expense> expenses = expenseDAO.getAllExpenses();
        return CSVExporter.exportExpenses(expenses);
    }

    /**
     * Export income to CSV
     */
    public String exportIncomeToCSV() throws DatabaseException, IOException {
        List<Income> incomeList = incomeDAO.getAllIncome();
        return CSVExporter.exportIncome(incomeList);
    }
}
