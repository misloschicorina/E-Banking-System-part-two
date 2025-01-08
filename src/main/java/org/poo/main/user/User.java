package org.poo.main.user;

import org.poo.main.CashbackInfo;
import org.poo.main.Commerciant.Commerciant;
import org.poo.main.accounts.Account;
import org.poo.main.exchange_rate.ExchangeRate;
import org.poo.main.transactions.Transaction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.Map;

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
    Map<Commerciant, CashbackInfo> cashbackInfo;  // Key = numele comerciantului, Value = informații despre cashback

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
        this.cashbackInfo = new HashMap<>();

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

    public void addTransactionToCommerciant(final Commerciant commerciant, final double amount,
                                            final String transactionCurrency,
                                            final String targetCurrency,
                                            final List<ExchangeRate> exchangeRates) {
        // Obținem informațiile despre cashback pentru comerciant
        CashbackInfo info = cashbackInfo.getOrDefault(commerciant, new CashbackInfo());

        // Folosim metoda addTransaction pentru a adăuga suma în moneda țintă
        info.addTransaction(amount, transactionCurrency, targetCurrency, exchangeRates);

        // Actualizăm informațiile comerciantului în map
        cashbackInfo.put(commerciant, info); // Adăugăm comerciantul ca și cheie

//        // Debugging pentru verificare
//        System.out.println("Tranzacție adăugată pentru comerciantul: " + commerciant.getName());
//        System.out.println("Total cheltuit pentru acest comerciant: " + info.totalSpent + " " + targetCurrency);
    }

    public double applyCashbackForTransaction(final Commerciant commerciant, final double amount,
                                              final String cashbackType, final String currency,
                                              final List<ExchangeRate> exchangeRates) {
        CashbackInfo info = cashbackInfo.getOrDefault(commerciant, new CashbackInfo());
        double cashback = 0.0;

        // Verificăm tipul de cashback și aplicăm corespunzător
        if ("nrOfTransactions".equals(cashbackType)) {
            // Aplicăm cashback pentru nrOfTransactions
            cashback = info.calculateTransactionCashback(amount, commerciant); // Calculăm cashback-ul din nrOfTransactions
            System.out.println("Cashback aplicat pentru nrOfTransactions: " + cashback);
        } else if ("spendingThreshold".equals(cashbackType)) {
            // Aplicăm cashback pentru spendingThreshold
            cashback = info.calculateSpendingCashback(amount, this.accountPlan, currency, exchangeRates); // Calculăm cashback-ul din spendingThreshold
            System.out.println("Cashback aplicat pentru spendingThreshold: " + cashback);
        } else {
            System.out.println("Tipul de cashback nu este valid.");
        }

        // Afișăm suma de plată după aplicarea cashback-ului
        System.out.println("Cashback total calculat: " + cashback);

        return cashback;  // Returnăm doar cashback-ul calculat, fără a modifica suma de plată
    }


    public double calculateTotalSpentForSpendingThreshold() {
        double totalSpentForThreshold = 0.0;

        // Iterăm prin toate intrările din cashbackInfo
        for (Map.Entry<Commerciant, CashbackInfo> entry : cashbackInfo.entrySet()) {
            // Verificăm doar comercianții care au strategia spendingThreshold
            Commerciant currentCommerciant = entry.getKey();
            CashbackInfo entryInfo = entry.getValue();

            if ("spendingThreshold".equalsIgnoreCase(currentCommerciant.getCashbackStrategy())) {
                totalSpentForThreshold += entryInfo.totalSpent;
            }

            System.out.println("Total cheltuit până acum: " + totalSpentForThreshold);
        }

        return totalSpentForThreshold;
    }

    public String isApplyingCashback(final Commerciant commerciant, final String accountCurrency,
                                                        final List<ExchangeRate> exchangeRates) {
        // Obținem strategia de cashback a comerciantului
        String strategy = commerciant.getCashbackStrategy();
        String category = commerciant.getType(); // Food, Clothes, Tech

        // System.out.println("acccount currrency" + accountCurrency);

        // Verificăm dacă comerciantul are strategia 'spendingThreshold'
        if ("spendingThreshold".equals(strategy)) {
            // Calculăm totalul cheltuit până acum pentru comercianții cu strategia spendingThreshold
            double totalSpentForThreshold = calculateTotalSpentForSpendingThreshold();

            // Conversie praguri în moneda contului
            double threshold100 = 100.0;
            double threshold300 = 300.0;
            double threshold500 = 500.0;

            if (!accountCurrency.equals("RON")) { // Conversie din RON în moneda contului
                double exchangeRate = ExchangeRate.getExchangeRate("RON", accountCurrency, exchangeRates); // Invers!
                threshold100 = Math.round((100.0 * exchangeRate) * 100.0) / 100.0;
                threshold300 = Math.round((300.0 * exchangeRate) * 100.0) / 100.0;
                threshold500 = Math.round((500.0 * exchangeRate) * 100.0) / 100.0;
            }


            // System.out.println("(isapplyingcashback) Praguri adaptate în moneda contului:" + threshold100 + ", " + threshold300 + ", " + threshold500);
            // System.out.println("Total cheltuit în moneda contului: " + totalSpentForThreshold);

            // Verificăm dacă totalul cheltuit atinge pragul minim de 100 în moneda contului
            if (totalSpentForThreshold >= threshold100) {
                return "spendingThreshold";  // Se poate aplica cashback
            }
        }

        // Verificăm dacă comerciantul are strategia 'nrOfTransactions'
        if ("nrOfTransactions".equals(strategy)) {
            // Verificăm categoriile de comerciant pentru nrOfTransactions (Food, Clothes, Tech)
            if (category.equalsIgnoreCase("Food") || category.equalsIgnoreCase("Clothes") || category.equalsIgnoreCase("Tech")) {
                return "nrOfTransactions";  // Se poate aplica cashback
            }
        }

        return null;  // Nu se poate aplica cashback
    }
}
