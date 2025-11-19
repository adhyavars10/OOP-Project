package models;

import java.time.LocalDate;

public class Income extends Transaction {

    private String source;

    public Income(double amount, String description, String source) {
        super(amount, description);
        this.source = source;
    }

    public Income(
        String id,
        double amount,
        LocalDate date,
        String description,
        String source
    ) {
        super(id, amount, date, description);
        this.source = source;
    }

    @Override
    public String getType() {
        return "INCOME";
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    @Override
    public String toString() {
        return (
            "Income{" +
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
            ", source='" +
            source +
            '\'' +
            '}'
        );
    }
}
