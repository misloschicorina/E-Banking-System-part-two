package org.poo.main.accounts;

import org.poo.main.CashbackInfo;
import org.poo.main.Commerciant.Commerciant;
import org.poo.main.TransactionDetail;
import org.poo.main.cards.Card;
import org.poo.main.exchange_rate.ExchangeRate;
import org.poo.main.user.User;
import org.poo.main.CashbackInfo;

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
    private double totalSpendingThreshold; // Suma totală cheltuită pentru strategia spendingThreshold

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

    public double getTotalSpendingThreshold() {
        return totalSpendingThreshold;
    }

    public void setTotalSpendingThreshold(final double amount) {
        totalSpendingThreshold = amount;
    }

    public void addToTotalSpendingThreshold(final double amount) {
        this.totalSpendingThreshold += amount;
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
    public void setBalance(double amount) {
        this.balance = amount;
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

    public boolean isBusinessAccount() {
        return false;
    }

    public void addManagerEmail(final String managerEmail) {
        throw new UnsupportedOperationException("setManagerEmail is not supported for this account type.");
    }

    public void addEmployeeEmail(final String employeeEmail) {
        throw new UnsupportedOperationException("addEmployeeEmail is not supported for this account type.");
    }

    public void setSpendingLimit(final double newLimit, final String email) {
        System.out.println("setSpendingLimit is not supported for this account type.");
    }

    public void setDepositLimit(final double newLimit, final String email) {
        System.out.println("setDepositLimit is not supported for this account type.");
    }

    public void changeLimitInAccountCurrency(List<ExchangeRate> exchangeRates) {
        System.out.println("changeLimitInAccountCurrency is not supported for this account type.");
    }

    public double getSpendingLimit() {
        return 0;
    }

    public double getDepositLimit() {
        return 0;
    }

    public boolean isManager(String email) {
        return false;
    }

    public boolean isEmployee(String email) {
        return false;
    }

    public void addSpending(String email, double amount, int timestamp, String commerciantNAme) {
    }

    public void addDeposit(String email, double amount, int timestamp) {
    }

    public String isApplyingCashback(final Commerciant commerciant, final String accountCurrency,
                                     final List<ExchangeRate> exchangeRates) {
        // Obținem strategia de cashback a comerciantului
        String strategy = commerciant.getCashbackStrategy();
        String category = commerciant.getType(); // Food, Clothes, Tech

        // Verificăm dacă comerciantul are strategia 'spendingThreshold'
        if ("spendingThreshold".equals(strategy)) {
            // Calculăm totalul cheltuit până acum pentru comercianții cu strategia spendingThreshold
            double totalSpentForThreshold = this.getTotalSpendingThreshold();

            // Conversie prag în moneda contului
            double threshold100 = THRESHOLD_100;

            if (!accountCurrency.equals("RON")) { // Conversie din RON în moneda contului
                double exchangeRate = ExchangeRate.getExchangeRate("RON", accountCurrency, exchangeRates); // Invers!
                threshold100 = Math.round((100.0 * exchangeRate) * 100.0) / 100.0;
            }

            System.out.println("total: " + totalSpentForThreshold);
            System.out.println("prag convertit: " + threshold100);

            // Verificăm dacă totalul cheltuit atinge pragul minim de 100 în moneda contului
            if (totalSpentForThreshold >= threshold100) {
                return "spendingThreshold";  // Se poate aplica cashback
            }
        }

        // Verificăm dacă comerciantul are strategia 'nrOfTransactions'
        if ("nrOfTransactions".equals(strategy)) {
            // Verificăm categoriile de comerciant pentru nrOfTransactions (Food, Clothes, Tech)
            if (category.equalsIgnoreCase("Food")
                    || category.equalsIgnoreCase("Clothes")
                    || category.equalsIgnoreCase("Tech")) {
                return "nrOfTransactions";  // Se poate aplica cashback
            }
        }
        return null;  // Nu se poate aplica cashback

    }

    public double applyCashbackForTransaction(final Commerciant commerciant, final double amount,
                                              final String cashbackType, final String currency, final String paymentCurrency,
                                              final List<ExchangeRate> exchangeRates, final User user){
        String plan = user.getAccountPlan();

        CashbackInfo info = cashbackInfo.getOrDefault(commerciant, new CashbackInfo());
        double cashback = 0.0;

        // Verificăm tipul de cashback și aplicăm corespunzător
        if ("nrOfTransactions".equals(cashbackType)) {
            // Aplicăm cashback pentru nrOfTransactions
            cashback = info.calculateTransactionCashback(amount, commerciant); // Calculăm cashback-ul din nrOfTransactions
            // System.out.println("Cashback aplicat pentru nrOfTransactions: " + cashback);
        } else if ("spendingThreshold".equals(cashbackType)) {
            // Aplicăm cashback pentru spendingThreshold
            cashback = info.calculateSpendingCashback(amount, plan, currency, paymentCurrency, exchangeRates, this); // Calculăm cashback-ul din spendingThreshold
            // System.out.println("Cashback aplicat pentru spendingThreshold: " + cashback);
        } else {
            System.out.println("Tipul de cashback nu este valid.");
        }

        return cashback;  // Returnăm doar cashback-ul calculat, fără a modifica suma de plată
    }

    public void addTransactionToCommerciant(final Commerciant commerciant, final double amount,
                                            final String transactionCurrency,
                                            final String paymentCurrency,
                                            final List<ExchangeRate> exchangeRates) {
        // Obținem informațiile despre cashback pentru comerciant
        CashbackInfo info = cashbackInfo.getOrDefault(commerciant, new CashbackInfo());

        // Adăugăm tranzacția la informațiile despre cashback
        info.addTransaction(amount, transactionCurrency, paymentCurrency, exchangeRates);

        // Actualizăm informațiile comerciantului în map
        cashbackInfo.put(commerciant, info); // Adăugăm comerciantul ca și cheie
    }


    public boolean isAssociate(String email) {
        // always return false
        return false;
    }

    public void displayAssociateTransactions() {
    }



}
