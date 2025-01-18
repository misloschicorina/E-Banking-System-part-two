package org.poo.main.accounts;
import org.poo.main.TransactionDetail;

import java.util.HashMap;
import java.util.Map;

import java.util.ArrayList;
import java.util.List;

public final class BusinessAccount extends Account {
    private List<String> managersEmails; // Lista de manageri
    private List<String> employeesEmails; // Lista de angajați
    private double spendingLimit;
    private double depositLimit;

    private Map<String, List<TransactionDetail>> associateTransactions;

    private static final double INITIAL_LIMIT_IN_RON = 500.0;

    public BusinessAccount(final String currency, final String ownerEmail, final String iban) {
        super(currency, "business", ownerEmail, iban);
        this.managersEmails = new ArrayList<>();
        this.employeesEmails = new ArrayList<>();
        this.spendingLimit = 500.0;
        this.depositLimit = 500.0;
        this.associateTransactions = new HashMap<>();
    }

    public List<String> getManagersEmails() {
        return managersEmails;
    }

    public List<String> getEmployeesEmails() {
        return employeesEmails;
    }

    public double getSpendingLimit() {
        return spendingLimit;
    }

    public double getDepositLimit() {
        return depositLimit;
    }

    @Override
    public void setSpendingLimit(final double newLimit, final String email) {
        String ownerEmail = getOwnerEmail();
        if (!email.equals(ownerEmail)) {
            System.out.println("You are not authorized to make this transaction.");
            return;
        }
        this.spendingLimit = newLimit;
    }

    @Override
    public void setDepositLimit(final double newLimit, final String email) {
        String ownerEmail = getOwnerEmail();
        if (!email.equals(ownerEmail)) {
            System.out.println("You are not authorized to make this transaction.");
            return;
        }
            this.depositLimit = newLimit;
    }

    public boolean removeAssociate(final String email) {
        return employeesEmails.remove(email); // nu stiu daca oricine poate sterge un asociat sau doar ownerul
    }

    @Override
    public boolean isBusinessAccount() {
        return true;
    }

    @Override
    public void addEmployeeEmail(String employeeEmail) {
        employeesEmails.add(employeeEmail);
    }

    @Override
    public void addManagerEmail(String managerEmail) {
        if (!managersEmails.contains(managerEmail)) {
            managersEmails.add(managerEmail);
        }
    }

    @Override
    public boolean isManager(String email) {
        return managersEmails.contains(email);
    }

    @Override
    public boolean isEmployee(String email) {
        return employeesEmails.contains(email);
    }

    public void addSpending(String email, double amount, int timestamp, String commerciantName) {
        // Creează o listă pentru tranzacțiile utilizatorului, dacă nu există deja
        associateTransactions.putIfAbsent(email, new ArrayList<>());

        // Adaugă tranzacția în lista asociată utilizatorului
        associateTransactions.get(email).add(new TransactionDetail("spend", amount, timestamp, commerciantName));
    }

    public void addDeposit(String email, double amount, int timestamp) {
        // Creează o listă pentru tranzacțiile utilizatorului, dacă nu există deja
        associateTransactions.putIfAbsent(email, new ArrayList<>());

        // Adaugă tranzacția în lista asociată utilizatorului
        associateTransactions.get(email).add(new TransactionDetail("deposit", amount, timestamp, null));
    }

    public Map<String, List<TransactionDetail>> getAssociateTransactions() {
        return associateTransactions;
    }

    @Override
    public boolean isAssociate(String email) {
        // Te asiguri că email nu e null pentru a evita erorile
        if (email == null) {
            return false;
        }

        // Dacă e owner, manager sau angajat, îl considerăm "asociat"
        if (email.equals(getOwnerEmail())) {
            return true;
        }
        if (managersEmails.contains(email)) {
            return true;
        }
        if (employeesEmails.contains(email)) {
            return true;
        }

        return false;
    }

    @Override
    public void displayAssociateTransactions() {
        // Parcurgem fiecare email și lista de tranzacții asociate
        for (Map.Entry<String, List<TransactionDetail>> entry : associateTransactions.entrySet()) {
            String email = entry.getKey();
            List<TransactionDetail> transactionDetails = entry.getValue();

            System.out.println("Transactions for email: " + email);

            // Afișăm fiecare TransactionDetail din listă
            for (TransactionDetail detail : transactionDetails) {
                System.out.println("  - Type: " + detail.getType()
                        + ", Amount: " + detail.getAmount()
                        + ", Timestamp: " + detail.getTimestamp()
                        + ", Commerciant: " + detail.getCommerciantName());
            }
            System.out.println("--------------------------------------------------------");
        }
    }





}
