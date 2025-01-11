package org.poo.main.split;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a split payment operation.
 * Manages the accounts, amounts, and user statuses for the payment.
 */
public final class SplitPayment {
    private final String splitPaymentType; // "equal" or "custom"
    private final List<String> accounts; // List of IBANs involved
    private final List<Double> amounts; // Amounts corresponding to each IBAN
    private final String currency;
    private final int timestamp;
    private final Map<String, Boolean> ibanAcceptanceMap; // Status of each account (accept/reject)

    /**
     * Constructs a SplitPayment object.
     *
     * @param splitPaymentType the type of the split payment ("equal" or "custom")
     * @param currency the currency of the split payment
     * @param timestamp the timestamp of the split payment operation
     */
    public SplitPayment(final String splitPaymentType, final String currency, final int timestamp) {
        this.splitPaymentType = splitPaymentType;
        this.accounts = new ArrayList<>();
        this.amounts = new ArrayList<>();
        this.currency = currency;
        this.timestamp = timestamp;
        this.ibanAcceptanceMap = new HashMap<>();
    }

    /**
     * Adds an account to the split payment.
     *
     * @param account the IBAN of the account to be added
     */
    public void addAccount(final String account) {
        this.accounts.add(account);
        this.ibanAcceptanceMap.put(account, null); // Add account with a null acceptance status
    }

    /**
     * Adds an amount to the split payment.
     *
     * @param amount the amount to be added
     */
    public void addAmount(final double amount) {
        this.amounts.add(amount);
    }

    /**
     * Checks if all users have accepted the split payment.
     *
     * @return true if all users have accepted, false otherwise
     */
    public boolean allAccepted() {
        return ibanAcceptanceMap.values()
                .stream()
                .allMatch(status -> status != null && status.equals(Boolean.TRUE));
    }

    /**
     * Sets the status (accepted/rejected) for a user.
     *
     * @param account the IBAN of the user
     * @param status the status of the user (true for accepted, false for rejected)
     */
    public void setStatus(final String account, final boolean status) {
        ibanAcceptanceMap.put(account, status);
    }

    // Getters

    /**
     * Gets the type of the split payment.
     *
     * @return the split payment type
     */
    public String getSplitPaymentType() {
        return splitPaymentType;
    }

    /**
     * Gets the currency of the split payment.
     *
     * @return the currency
     */
    public String getCurrency() {
        return currency;
    }

    /**
     * Gets the timestamp of the split payment.
     *
     * @return the timestamp
     */
    public int getTimestamp() {
        return timestamp;
    }

    /**
     * Gets the acceptance statuses for each account.
     *
     * @return a map of IBANs to their acceptance statuses
     */
    public Map<String, Boolean> getIbanAcceptanceMap() {
        return new HashMap<>(ibanAcceptanceMap);
    }

    /**
     * Gets the accounts involved in the split payment.
     *
     * @return a list of IBANs
     */
    public List<String> getAccounts() {
        return new ArrayList<>(accounts);
    }

    /**
     * Gets the amounts for the split payment.
     *
     * @return a list of amounts
     */
    public List<Double> getAmounts() {
        return new ArrayList<>(amounts);
    }
}
