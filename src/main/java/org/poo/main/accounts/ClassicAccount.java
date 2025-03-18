package org.poo.main.accounts;

/**
 * Represents a classic account in the banking system.
 * This account type does not support savings-related features.
 */
public class ClassicAccount extends Account {
    public ClassicAccount(final String currency, final String ownerEmail, final String iban) {
        super(currency, "classic", ownerEmail, iban);
    }

    /**
     * Determines if this account is a savings account.
     *
     * @return false, as ClassicAccount is not a savings account
     */
    @Override
    public boolean isSavingsAccount() {
        return false;
    }
}
