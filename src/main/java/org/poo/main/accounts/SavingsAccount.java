package org.poo.main.accounts;

/**
 * Represents a savings account in the banking system.
 * This account type includes an interest rate feature.
 */
public final class SavingsAccount extends Account {
    private double interestRate;

    public SavingsAccount(final String currency, final String ownerEmail,
                          final double interestRate, final String iban) {
        super(currency, "savings", ownerEmail, iban);
        this.interestRate = interestRate;
    }

    public double getInterestRate() {
        return interestRate;
    }

    public void setInterestRate(final double interestRate) {
        this.interestRate = interestRate;
    }

    /**
     * Determines if this account is a savings account.
     *
     * @return true, as this is a savings account
     */
    @Override
    public boolean isSavingsAccount() {
        return true;
    }
}
