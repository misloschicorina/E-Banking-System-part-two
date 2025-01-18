package org.poo.main.user;

import org.poo.main.CashbackInfo;
import org.poo.main.Commerciant.Commerciant;
import org.poo.main.cards.Card;
import org.poo.main.split.SplitPayment;
import org.poo.main.accounts.Account;
import org.poo.main.exchange_rate.ExchangeRate;
import org.poo.main.transactions.Transaction;

import java.util.*;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;

/**
 * Represents a user in the banking system.
 */
public final class User {

    private String firstName;
    private String lastName;
    private String email;
    private String birthDate;
    private String occupation;
    private String accountPlan;
    private List<Account> accounts; // Accounts linked to the user
    private List<Transaction> transactions; // Transactions performed by the user
//    private final Map<Commerciant, CashbackInfo> cashbackInfo;
    private final List<SplitPayment> pendingTransactions;
//    private double totalSpendingThreshold; // Suma totală cheltuită pentru strategia spendingThreshold

    private static final int MIN_AGE = 21;
    private static final double THRESHOLD_100 = 100.0;

    public User(final String firstName, final String lastName, final String email,
                final String birthDate, final String occupation) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.birthDate = birthDate;
        this.occupation = occupation;
        this.accounts = new ArrayList<>();
        this.transactions = new ArrayList<>();
        this.pendingTransactions = new ArrayList<>();

        if ("student".equals(occupation))
            this.accountPlan = "student";
        else
            this.accountPlan = "standard";

//        this.totalSpendingThreshold = 0.0;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(final String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(final String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(final String email) {
        this.email = email;
    }

    public String getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(final String birthDate) {
        this.birthDate = birthDate;
    }

    public String getOccupation() {
        return occupation;
    }

    public void setOccupation(final String occupation) {
        this.occupation = occupation;
    }

    public String getAccountPlan() {
        return accountPlan;
    }

    public void setAccountPlan(final String accountPlan) {
        this.accountPlan = accountPlan;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

//    public double getTotalSpendingThreshold() {
//        return totalSpendingThreshold;
//    }
//
//    public void setTotalSpendingThreshold(final double amount) {
//        totalSpendingThreshold = amount;
//    }
//
//    public void addToTotalSpendingThreshold(final double amount) {
//        this.totalSpendingThreshold += amount;
//    }

    /**
     * Adds an account to the user's account list.
     *
     * @param account the account to add
     */
    public void addAccount(final Account account) {
        accounts.add(account);
    }

    /**
     * Removes an account from the user's account list.
     *
     * @param account the account to remove
     */
    public void removeAccount(final Account account) {
        accounts.remove(account);
    }

    public List<Account> getAccounts() {
        return accounts;
    }

    /**
     * Adds a transaction to the user's transaction list.
     *
     * @param transaction the transaction to add
     */
    public void addTransaction(final Transaction transaction) {
        if (transaction != null) {
            transactions.add(transaction);
        }
    }

    /**
     * Adds a transaction to the user's transaction list, maintaining the list sorted by timestamp.
     *
     * @param transaction the transaction to add
     */
    public void addTransactionByTimestamp(final Transaction transaction) {
        if (transaction == null) {
            return;
        }
        int index = 0;
        while (index < transactions.size() && transactions.get(index).getTimestamp() <= transaction.getTimestamp()) {
            index++;
        }
        transactions.add(index, transaction);
    }

    /**
     * Checks if the user meets the minimum age requirement.
     *
     * @return true if the user is at least 21 years old, false otherwise.
     */
    public boolean hasMinAge() {
        // Formatting the birth date
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        // Converting birthDate to LocalDate
        LocalDate birthDateLocal = LocalDate.parse(this.birthDate, formatter);

        // Current Date
        LocalDate currentDate = LocalDate.now();

        // Calculating the age
        int age = Period.between(birthDateLocal, currentDate).getYears();

        return age >= MIN_AGE;
    }

    /**
     * Adds a pending split payment to the user's list of pending transactions.
     *
     * @param splitPayment the split payment to add to the pending transactions
     */
    public void addPendingSplitPayment(final SplitPayment splitPayment) {
        if (splitPayment != null) {
            pendingTransactions.add(splitPayment);
        }
    }

    /**
     * Removes a pending split payment from the user's list of pending transactions.
     *
     * @param splitPayment the split payment to remove from the pending transactions
     */
    public void removePendingSplitPayment(final SplitPayment splitPayment) {
        pendingTransactions.remove(splitPayment);
    }

    /**
     * Retrieves the oldest pending split payment transaction based on the timestamp.
     *
     * @return the oldest SplitPayment transaction, or null if the list of pending transactions is empty
     */
    public SplitPayment getOldestPendingTransaction() {
        return pendingTransactions.stream()
                .min(Comparator.comparingInt(SplitPayment::getTimestamp))
                .orElse(null); // Return null if no pending transactions exist
    }

    /**
     * Retrieves the oldest unaccepted split payment transaction for the user.
     */
    public Map.Entry<SplitPayment, String> getOldestUnacceptedTransaction() {
        for (SplitPayment payment : pendingTransactions) {
            for (Account acc : accounts) {
                String iban = acc.getIban();
                if (payment.getIbanAcceptanceMap().containsKey(iban)
                        && payment.getIbanAcceptanceMap().get(iban) == null) {
                    // Found the first unaccepted transaction for the account
                    return Map.entry(payment, iban); // return the found split transaction and iban to accept
                }
            }
        }
        return null;
    }

    public List<Card> getAllCards() {
        List<Card> allCards = new ArrayList<>();
        for (Account account : accounts) {
            // presupunem că există account.getCards()
            allCards.addAll(account.getCards());
        }
        return allCards;
    }


}
