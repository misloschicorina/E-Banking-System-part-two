package org.poo.main.accounts;

import org.poo.main.cashback.CashbackInfo;
import org.poo.main.Commerciant.Commerciant;
import org.poo.main.cards.Card;
import org.poo.main.exchange_rate.ExchangeRate;
import org.poo.main.user.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Abstract class representing a bank account.
 */
public abstract class Account {
    private String iban;
    private double balance;
    private String currency;
    private String accountType;
    private String ownerEmail;
    private String alias;
    private double minBalance;
    private List<Card> cards; // List of cards associated with the account
    private String accountPlan;
    private double totalSpendingThreshold;

    private final Map<Commerciant, CashbackInfo> cashbackInfo;
    private static final double THRESHOLD_100 = 100.0;

    public Account(final String currency, final String type, final String ownerEmail,
                   final String iban) {
        this.balance = 0;
        this.currency = currency;
        this.iban = iban;
        this.accountType = type;
        this.ownerEmail = ownerEmail;
        this.alias = null;
        this.minBalance = 0;
        this.cards = new ArrayList<>();
        this.accountPlan = null;

        this.cashbackInfo = new HashMap<>();
        this.totalSpendingThreshold = 0.0;
    }

    /**
     * Returns the current balance of the account.
     *
     * @return the balance
     */
    public double getBalance() {
        return balance;
    }

    /**
     * Returns the list of cards associated with the account.
     *
     * @return the list of cards
     */
    public List<Card> getCards() {
        return cards;
    }

    /**
     * Returns the currency of the account.
     *
     * @return the currency
     */
    public String getCurrency() {
        return currency;
    }

    /**
     * Returns the IBAN of the account.
     *
     * @return the IBAN
     */
    public String getIban() {
        return iban;
    }

    /**
     * Returns the type of the account.
     *
     * @return the account type
     */
    public String getAccountType() {
        return accountType;
    }

    /**
     * Returns the alias of the account.
     *
     * @return the alias
     */
    public String getAlias() {
        return alias;
    }

    /**
     * Sets the alias for the account.
     *
     * @param alias the alias to set
     */
    public void setAlias(final String alias) {
        this.alias = alias;
    }

    /**
     * Returns the owner's email address.
     *
     * @return the owner's email
     */
    public String getOwnerEmail() {
        return ownerEmail;
    }

    /**
     * Returns the minimum balance allowed for the account.
     *
     * @return the minimum balance
     */
    public double getMinBalance() {
        return minBalance;
    }

    /**
     * Sets the minimum balance for the account.
     *
     * @param minBalance the minimum balance to set
     */
    public void setMinBalance(final double minBalance) {
        this.minBalance = minBalance;
    }

    /**
     * Returns the current plan of the account.
     * Designed for extension, subclasses can override this method.
     *
     * @return the account plan
     */
    public String getAccountPlan() {
        return accountPlan;
    }

    /**
     * Sets the account plan.
     *
     * @param accountPlan the new account plan
     */
    public void setAccountPlan(final String accountPlan) {
        this.accountPlan = accountPlan;
    }

    /**
     * Sets the balance of the account.
     * Designed for extension, subclasses can override this method.
     *
     * @param amount the new balance to set
     */
    public void setBalance(final double amount) {
        this.balance = amount;
    }

    /**
     * Gets the total spending threshold for the account.
     * To be overridden or used in subclasses where applicable.
     *
     * @return the total spending threshold
     */
    public double getTotalSpendingThreshold() {
        return totalSpendingThreshold;
    }

    /**
     * Adds a specified amount to the total spending threshold.
     * To be overridden or used in subclasses where applicable.
     *
     * @param amount the amount to add
     */
    public void addToTotalSpendingThreshold(final double amount) {
        this.totalSpendingThreshold += amount;
    }

    /**
     * Adds a card to the list of cards associated with the account.
     *
     * @param card the card to add
     */
    public void addCard(final Card card) {
        if (!cards.contains(card)) {
            cards.add(card);
        }
    }

    /**
     * Removes a card from the list of cards associated with the account.
     *
     * @param card the card to remove
     */
    public void removeCard(final Card card) {
        cards.remove(card);
    }

    /**
     * Deposits an amount into the account.
     *
     * @param amount the amount to deposit
     */
    public void deposit(final double amount) {
        balance += amount;
    }

    /**
     * Spends an amount from the account.
     *
     * @param amount the amount to spend
     */
    public void spend(final double amount) {
        balance -= amount;
    }

    /**
     * Clears all cards associated with the account.
     */
    public void clearCards() {
        cards.clear();
    }

    /**
     * Checks if the account is a savings account.
     *
     * @return true if savings account, false otherwise
     */
    public boolean isSavingsAccount() {
        return false;
    }

    /**
     * Checks if the account is a business account.
     *
     * @return true if business account, false otherwise
     */
    public boolean isBusinessAccount() {
        return false;
    }

    /**
     * Adds a manager's email. To be overridden in subclasses like BusinessAccount.
     *
     * @param managerEmail the manager's email
     * @throws UnsupportedOperationException if not supported
     */
    public void addManagerEmail(final String managerEmail) {
        throw new UnsupportedOperationException("command not supported for this account type.");
    }

    /**
     * Adds an employee's email. To be overridden in subclasses like BusinessAccount.
     *
     * @param employeeEmail the employee's email
     * @throws UnsupportedOperationException if not supported
     */
    public void addEmployeeEmail(final String employeeEmail) {
        throw new UnsupportedOperationException("command not supported for this account type.");
    }

    /**
     * Sets the spending limit. To be overridden in subclasses like BusinessAccount.
     *
     * @param newLimit the spending limit
     * @param email the requester email
     * @throws UnsupportedOperationException if not supported
     */
    public void setSpendingLimit(final double newLimit, final String email) {
        throw new UnsupportedOperationException("command not supported for this account type.");
    }

    /**
     * Sets the deposit limit. To be overridden in subclasses like BusinessAccount.
     *
     * @param newLimit the deposit limit
     * @param email the requester email
     * @throws UnsupportedOperationException if not supported
     */
    public void setDepositLimit(final double newLimit, final String email) {
        throw new UnsupportedOperationException("command not supported for this account type.");
    }

    /**
     * Gets the spending limit. Default is 0.
     *
     * @return the spending limit
     */
    public double getSpendingLimit() {
        return 0;
    }

    /**
     * Gets the deposit limit. Default is 0.
     *
     * @return the deposit limit
     */
    public double getDepositLimit() {
        return 0;
    }

    /**
     * Checks if the provided email belongs to a manager.
     * To be overridden in subclasses like BusinessAccount.
     *
     * @param email the email to check
     * @return false by default
     */
    public boolean isManager(final String email) {
        return false;
    }

    /**
     * Checks if the provided email belongs to an employee.
     * To be overridden in subclasses like BusinessAccount.
     *
     * @param email the email to check
     * @return false by default
     */
    public boolean isEmployee(final String email) {
        return false;
    }

    /**
     * Checks if the provided email is associated with the account.
     * To be overridden in subclasses like BusinessAccount.
     *
     * @param email the email to check
     * @return false by default
     */
    public boolean isAssociate(final String email) {
        return false;
    }

    /**
     * Adds spending information.
     * To be overridden in subclasses like BusinessAccount.
     *
     * @param email the email associated with the spending
     * @param amount the amount spent
     * @param timestamp the timestamp of the spending
     * @param commerciantName the name of the merchant
     */
    public void addSpending(final String email, final double amount, final int timestamp,
                            final String commerciantName) {
    }

    /**
     * Adds deposit information.
     * To be overridden in subclasses like BusinessAccount.
     *
     * @param email the email associated with the deposit
     * @param amount the amount deposited
     * @param timestamp the timestamp of the deposit
     */
    public void addDeposit(final String email, final double amount, final int timestamp) {
    }

    /**
     * Determines if cashback can be applied based on the merchant's strategy.
     *
     * @param commerciant the merchant involved in the transaction
     * @param accCurrency the currency of the account
     * @param exchangeRates the list of exchange rates for currency conversion
     * @return the cashback strategy if applicable, otherwise null
     * @throws IllegalArgumentException if the merchant or exchange rates are null
     */
    public String isApplyingCashback(final Commerciant commerciant, final String accCurrency,
                                     final List<ExchangeRate> exchangeRates) {
        if (commerciant == null || exchangeRates == null) {
            throw new IllegalArgumentException("Merchant and exchange rates cannot be null.");
        }

        // Retrieve the merchant's cashback strategy and category
        String strategy = commerciant.getCashbackStrategy();
        String category = commerciant.getType(); // E.g., Food, Clothes, Tech

        // Check for 'spendingThreshold' strategy
        if ("spendingThreshold".equals(strategy)) {
            double totalSpentForThreshold = this.getTotalSpendingThreshold();

            // Convert threshold to account currency
            double threshold100 = THRESHOLD_100;
            if (!accCurrency.equals("RON")) {
                double exchangeRate = ExchangeRate.getExchangeRate("RON",
                                                            accCurrency, exchangeRates);
                threshold100 = threshold100 * exchangeRate;
            }

            // Check if the spending threshold is met
            if (totalSpentForThreshold >= threshold100) {
                return "spendingThreshold";
            }
        }

        // Check for 'nrOfTransactions' strategy
        if ("nrOfTransactions".equals(strategy)) {
            // Validate merchant categories for transaction-based cashback
            if (category.equalsIgnoreCase("Food")
                    || category.equalsIgnoreCase("Clothes")
                    || category.equalsIgnoreCase("Tech")) {
                return "nrOfTransactions";
            }
        }

        return null; // No applicable cashback strategy
    }

    /**
     * Applies cashback to a transaction based on the provided strategy.
     *
     * @param commerciant the merchant involved in the transaction
     * @param amount the transaction amount
     * @param cashbackType the cashback strategy (e.g., "nrOfTransactions", "spendingThreshold")
     * @param transactionCurrency the currency of the transaction
     * @param paymentCurrency the payment currency
     * @param exchangeRates the list of exchange rates for currency conversion
     * @param user the user associated with the account
     * @return the calculated cashback amount
     * @throws IllegalArgumentException if the merchant or user are null
     */
    public double applyCashbackForTransaction(final Commerciant commerciant,
                                              final double amount,
                                              final String cashbackType,
                                              final String transactionCurrency,
                                              final String paymentCurrency,
                                              final List<ExchangeRate> exchangeRates,
                                              final User user) {
        if (commerciant == null || user == null) {
            throw new IllegalArgumentException("Merchant and user cannot be null.");
        }

        // Retrieve the user's account plan and merchant cashback info
        String plan = user.getAccountPlan();
        CashbackInfo info = cashbackInfo.getOrDefault(commerciant, new CashbackInfo());
        double cashback = 0.0;

        // Apply the appropriate cashback strategy
        if ("nrOfTransactions".equals(cashbackType)) {
            cashback = info.calculateTransactionCashback(amount, commerciant);
        } else if ("spendingThreshold".equals(cashbackType)) {
            cashback = info.calculateSpendingCashback(amount, plan,
                    transactionCurrency, paymentCurrency, exchangeRates, this);
        }

        return cashback;
    }

}
