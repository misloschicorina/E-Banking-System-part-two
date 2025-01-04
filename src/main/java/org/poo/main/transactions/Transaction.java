package org.poo.main.transactions;

import org.poo.main.user.User;

import java.util.List;

/**
 * Represents a financial transaction with different details, including sender, receiver, amount,
 * and additional metadata like currency, card details, and error information.
 */
public class Transaction {
    private int timestamp;
    private String description;
    private String senderIBAN;
    private String receiverIBAN;
    private Double amount;
    private String transferType;
    private String cardNumber;
    private String cardHolder;
    private String accountIBAN;
    private String commerciant;
    private String currency;
    private String email;
    private List<String> involvedAccounts;
    private String error;
    private String plan;

    public Transaction(final int timestamp, final String description, final String senderIBAN,
                       final String receiverIBAN, final Double amount, final String currency,
                       final String transferType, final String cardNumber, final String cardHolder,
                       final String accountIBAN, final String commerciant, final String email,
                       final List<String> involvedAccounts, final String error, final String plan) {
        this.timestamp = timestamp;
        this.description = description;
        this.senderIBAN = senderIBAN;
        this.receiverIBAN = receiverIBAN;
        this.amount = amount;
        this.currency = currency;
        this.transferType = transferType;
        this.cardNumber = cardNumber;
        this.cardHolder = cardHolder;
        this.accountIBAN = accountIBAN;
        this.commerciant = commerciant;
        this.email = email;
        this.involvedAccounts = involvedAccounts;
        this.error = error;
        this.plan = plan;
    }

    /**
     * @return The timestamp of the transaction.
     */
    public int getTimestamp() {
        return timestamp;
    }

    /**
     * @return A brief description of the transaction.
     */
    public String getDescription() {
        return description;
    }

    /**
     * @return The IBAN of the sender.
     */
    public String getSenderIBAN() {
        return senderIBAN;
    }

    /**
     * @return The IBAN of the receiver.
     */
    public String getReceiverIBAN() {
        return receiverIBAN;
    }

    /**
     * @return The amount of the transaction.
     */
    public Double getAmount() {
        return amount;
    }

    /**
     * @return The currency used for the transaction.
     */
    public String getCurrency() {
        return currency;
    }

    /**
     * @return The type of transfer (e.g., credit, debit).
     */
    public String getTransferType() {
        return transferType;
    }

    /**
     * @return The card number used in the transaction.
     */
    public String getCardNumber() {
        return cardNumber;
    }

    /**
     * @return The name of the cardholder.
     */
    public String getCardHolder() {
        return cardHolder;
    }

    /**
     * @return The IBAN of the account involved in the transaction.
     */
    public String getAccountIBAN() {
        return accountIBAN;
    }

    /**
     * @return The name of the merchant or business.
     */
    public String getCommerciant() {
        return commerciant;
    }

    /**
     * @return The email address associated with the transaction.
     */
    public String getEmail() {
        return email;
    }

    /**
     * @param description Sets a new description for the transaction.
     */
    public void setDescription(final String description) {
        this.description = description;
    }

    /**
     * @return A list of accounts involved in the transaction.
     */
    public List<String> getInvolvedAccounts() {
        return involvedAccounts;
    }

    /**
     * Adds this transaction to a user's transaction list.
     *
     * @param user The user to whom the transaction will be added.
     */
    public void addTransaction(final User user) {
        if (user == null) {
            return;
        }
        user.addTransaction(this);
    }

    /**
     * @return Error details if the transaction encountered an issue.
     */
    public String getError() {
        return error;
    }

    public String getPlan() {
        return plan;
    }
}
