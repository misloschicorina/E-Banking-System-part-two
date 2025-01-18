package org.poo.main;

public class TransactionDetail {
    private String type; // "spend" sau "deposit"
    private double amount;
    private int timestamp;
    private String commerciantName;

    /**
     * Constructorul inițial, la care adăugăm și commerciantName.
     */
    public TransactionDetail(String type, double amount, int timestamp, String commerciantName) {
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
