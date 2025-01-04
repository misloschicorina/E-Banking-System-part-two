package org.poo.main.user;

import org.poo.main.accounts.Account;
import org.poo.main.transactions.Transaction;

import java.util.ArrayList;
import java.util.List;

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

    private static final int MIN_AGE = 21;

    public User(final String firstName, final String lastName, final String email,
                final String birthDate, final String occupation) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.birthDate = birthDate;
        this.occupation = occupation;
        this.accounts = new ArrayList<>();
        this.transactions = new ArrayList<>();

        if ("student".equals(occupation))
            this.accountPlan = "student";
        else
            this.accountPlan = "standard";
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

    public List<Transaction> getTransactions() {
        return transactions;
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
}
