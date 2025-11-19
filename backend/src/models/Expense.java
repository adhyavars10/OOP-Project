package models;

import java.time.LocalDate;

public class Expense extends Transaction {

    private Category category;

    public Expense(double amount, String description, Category category) {
        super(amount, description);
        this.category = category;
    }

    public Expense(
        String id,
        double amount,
        LocalDate date,
        String description,
        Category category
    ) {
        super(id, amount, date, description);
        this.category = category;
    }

    @Override
    public String getType() {
        return "EXPENSE";
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    @Override
    public String toString() {
        return (
            "Expense{" +
            "id='" +
            getId() +
            '\'' +
            ", amount=" +
            getAmount() +
            ", date=" +
            getDate() +
            ", description='" +
            getDescription() +
            '\'' +
            ", category=" +
            category +
            '}'
        );
    }
}
