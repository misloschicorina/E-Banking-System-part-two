package org.poo.main.transactions;

/**
 * Represents the details of a transaction.
 * A transaction can be of type "spend" or "deposit" and includes additional metadata.
 */
public final class TransactionDetail {
    private String type; // "spend" sau "deposit"
    private double amount;
    private int timestamp;
    private String commerciantName;

    public TransactionDetail(final String type, final double amount, final int timestamp,
                                                        final String commerciantName) {
        this.type = type;
        this.amount = amount;
        this.timestamp = timestamp;
        this.commerciantName = commerciantName;
    }

    public String getType() {
        return type;
    }

    public double getAmount() {
        return amount;
    }

    public int getTimestamp() {
        return timestamp;
    }

    public String getCommerciantName() {
        return commerciantName;
    }
}
