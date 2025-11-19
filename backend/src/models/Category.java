package models;

public enum Category {
    FOOD("Food & Dining", "🍔", "#FF6384"),
    TRANSPORT("Transportation", "🚗", "#36A2EB"),
    SHOPPING("Shopping", "🛍️", "#FFCE56"),
    BILLS("Bills & Utilities", "💡", "#4BC0C0"),
    ENTERTAINMENT("Entertainment", "🎮", "#9966FF"),
    HEALTHCARE("Healthcare", "🏥", "#FF9F40"),
    EDUCATION("Education", "📚", "#FF6384"),
    OTHER("Other", "📝", "#C9CBCF");

    private final String displayName;
    private final String emoji;
    private final String color;

    Category(String displayName, String emoji, String color) {
        this.displayName = displayName;
        this.emoji = emoji;
        this.color = color;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getEmoji() {
        return emoji;
    }

    public String getColor() {
        return color;
    }

    @Override
    public String toString() {
        return emoji + " " + displayName;
    }
}
