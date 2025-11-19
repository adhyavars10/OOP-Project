package models;

import exceptions.InvalidAmountException;
import java.time.LocalDate;
import java.util.UUID;

public abstract class Transaction {

    private String id;
    private double amount;
    private LocalDate date;
    private String description;

    public Transaction(double amount, String description) {
        this.id = UUID.randomUUID().toString();
        this.amount = amount;
        this.date = LocalDate.now();
        this.description = description;
    }

    public Transaction(
        String id,
        double amount,
        LocalDate date,
        String description
    ) {
        this.id = id;
        this.amount = amount;
        this.date = date;
        this.description = description;
    }

    public abstract String getType();

    public void validate() throws InvalidAmountException {
        if (amount <= 0) {
            throw new InvalidAmountException(
                "Amount must be greater than zero"
            );
        }
        if (amount > 1000000) {
            throw new InvalidAmountException("Amount exceeds maximum limit");
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return (
            "Transaction{" +
            "id='" +
            id +
            '\'' +
            ", amount=" +
            amount +
            ", date=" +
            date +
            ", description='" +
            description +
            '\'' +
            ", type=" +
            getType() +
            '}'
        );
    }
}
