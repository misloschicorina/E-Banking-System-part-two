package org.poo.main.accounts;

import org.poo.main.cards.Card;

import java.util.ArrayList;
import java.util.List;

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
}
