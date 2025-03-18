package org.poo.main.accounts;

/**
 * Factory class for creating accounts.
 */
public final class AccountFactory {

    /**
     * Private constructor to prevent instantiation.
     */
    private AccountFactory() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated.");
    }

    public enum AccountType {
        CLASSIC, SAVINGS, BUSINESS
    }

    /**
     * Creates an account of the specified type.
     *
     * @param accountType  the type of account to create
     * @param currency     the currency of the account
     * @param email        the owner's email
     * @param iban         the unique IBAN for the account
     * @param interestRate the interest rate (only for savings accounts)
     * @return a new instance of an Account
     */
    public static Account createAccount(
            final AccountType accountType,
            final String currency,
            final String email,
            final String iban,
            final Double interestRate) {

        switch (accountType) {
            case CLASSIC:
                return new ClassicAccount(currency, email, iban);

            case SAVINGS:
                if (interestRate == null) {
                    throw new IllegalArgumentException(
                            "Interest rate is required for a savings account.");
                }
                return new SavingsAccount(currency, email, interestRate, iban);

            case BUSINESS:
                return new BusinessAccount(currency, email, iban);

            default:
                throw new IllegalArgumentException("Unknown account type: " + accountType);
        }
    }
}
