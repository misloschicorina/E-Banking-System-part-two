package org.poo.main.accounts;

import org.poo.main.transactions.TransactionDetail;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a business account in the banking system.
 * Includes additional features like employee and manager management.
 */
public final class BusinessAccount extends Account {
    private List<String> managersEmails; // List of manager emails
    private List<String> employeesEmails; // List of employee emails
    private double spendingLimit;
    private double depositLimit;
    private Map<String, List<TransactionDetail>> associateTransactions;

    private static final double INITIAL_LIMIT_IN_RON = 500.0;

    public BusinessAccount(final String currency, final String ownerEmail, final String iban) {
        super(currency, "business", ownerEmail, iban);
        this.managersEmails = new ArrayList<>();
        this.employeesEmails = new ArrayList<>();
        this.spendingLimit = INITIAL_LIMIT_IN_RON;
        this.depositLimit = INITIAL_LIMIT_IN_RON;
        this.associateTransactions = new HashMap<>();
    }

    /**
     * Gets the list of manager emails.
     *
     * @return the list of manager emails
     */
    public List<String> getManagersEmails() {
        return managersEmails;
    }

    /**
     * Gets the list of employee emails.
     *
     * @return the list of employee emails
     */
    public List<String> getEmployeesEmails() {
        return employeesEmails;
    }

    /**
     * Gets the spending limit for the account.
     *
     * @return the spending limit
     */
    @Override
    public double getSpendingLimit() {
        return spendingLimit;
    }

    /**
     * Gets the deposit limit for the account.
     *
     * @return the deposit limit
     */
    @Override
    public double getDepositLimit() {
        return depositLimit;
    }

    /**
     * Sets the spending limit if the requester is the account owner.
     *
     * @param newLimit the new spending limit
     * @param email    the requester's email
     * @throws IllegalArgumentException if the requester is not the account owner
     */
    @Override
    public void setSpendingLimit(final double newLimit, final String email) {
        if (!email.equals(getOwnerEmail())) {
            throw new IllegalArgumentException("You are not authorized to set the spending limit.");
        }
        this.spendingLimit = newLimit;
    }

    /**
     * Sets the deposit limit if the requester is the account owner.
     *
     * @param newLimit the new deposit limit
     * @param email    the requester's email
     * @throws IllegalArgumentException if the requester is not the account owner
     */
    @Override
    public void setDepositLimit(final double newLimit, final String email) {
        if (!email.equals(getOwnerEmail())) {
            throw new IllegalArgumentException("You are not authorized to set the deposit limit.");
        }
        this.depositLimit = newLimit;
    }

    /**
     * Checks if the account is a business account.
     *
     * @return true
     */
    @Override
    public boolean isBusinessAccount() {
        return true;
    }

    /**
     * Adds an employee's email to the account.
     *
     * @param employeeEmail the email of the employee
     */
    @Override
    public void addEmployeeEmail(final String employeeEmail) {
        employeesEmails.add(employeeEmail);
    }

    /**
     * Adds a manager's email to the account.
     *
     * @param managerEmail the email of the manager
     */
    @Override
    public void addManagerEmail(final String managerEmail) {
        if (!managersEmails.contains(managerEmail)) {
            managersEmails.add(managerEmail);
        }
    }

    /**
     * Checks if the provided email belongs to a manager.
     *
     * @param email the email to check
     * @return true if the email belongs to a manager, false otherwise
     */
    @Override
    public boolean isManager(final String email) {
        return managersEmails.contains(email);
    }

    /**
     * Checks if the provided email belongs to an employee.
     *
     * @param email the email to check
     * @return true if the email belongs to an employee, false otherwise
     */
    @Override
    public boolean isEmployee(final String email) {
        return employeesEmails.contains(email);
    }

    /**
     * Adds a spending transaction for an associate.
     *
     * @param email           the associate's email
     * @param amount          the amount spent
     * @param timestamp       the timestamp of the transaction
     * @param commerciantName the merchant's name
     */
    @Override
    public void addSpending(final String email, final double amount, final int timestamp,
                            final String commerciantName) {
        associateTransactions.putIfAbsent(email, new ArrayList<>());
        associateTransactions.get(email).add(new TransactionDetail("spend",
                                                    amount, timestamp, commerciantName));
    }

    /**
     * Adds a deposit transaction for an associate.
     *
     * @param email     the associate's email
     * @param amount    the amount deposited
     * @param timestamp the timestamp of the transaction
     */
    @Override
    public void addDeposit(final String email, final double amount, final int timestamp) {
        associateTransactions.putIfAbsent(email, new ArrayList<>());
        associateTransactions.get(email).add(new TransactionDetail("deposit",
                                                amount, timestamp, null));
    }

    /**
     * Gets the map of associate transactions.
     *
     * @return the map of associate transactions
     */
    public Map<String, List<TransactionDetail>> getAssociateTransactions() {
        return associateTransactions;
    }

    /**
     * Checks if the provided email is associated with the account.
     *
     * @param email the email to check
     * @return true if the email is associated, false otherwise
     */
    @Override
    public boolean isAssociate(final String email) {
        if (email == null) {
            return false;
        }
        return email.equals(getOwnerEmail())
                || managersEmails.contains(email)
                || employeesEmails.contains(email);
    }
}
