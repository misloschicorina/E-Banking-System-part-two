package org.poo.main.transactions;

import java.util.List;

/**
 * A factory class for creating different types of transactions.
 */
public final class TransactionFactory {

    private TransactionFactory() {
    }

    /**
     * Creates a transaction for a new account creation.
     *
     * @param timestamp   The timestamp of the transaction.
     * @param accountIBAN The IBAN of the account.
     * @return The created transaction.
     */
    public static Transaction createAccountTransaction(
            final int timestamp, final String accountIBAN) {
        return new Transaction(
                timestamp,
                "New account created",
                null, null, null, null,
                null, null, null, accountIBAN,
                null, null, null,
                null, null
        );
    }

    /**
     * Creates a transaction for a new card creation.
     *
     * @param timestamp   The timestamp of the transaction.
     * @param cardNumber  The card number.
     * @param email       The email associated with the card.
     * @param accountIBAN The IBAN of the account.
     * @return The created transaction.
     */
    public static Transaction createCardTransaction(
            final int timestamp, final String cardNumber,
            final String email, final String accountIBAN) {
        return new Transaction(
                timestamp,
                "New card created",
                null, null, null, null,
                null, cardNumber, email,
                accountIBAN, null, null, null,
                null, null
        );
    }

    /**
     * Creates a transaction for sending money from one account to another.
     *
     * @param timestamp   The timestamp of the transaction.
     * @param senderIBAN  The IBAN of the sender account.
     * @param receiverIBAN The IBAN of the receiver account.
     * @param amount      The amount of money sent.
     * @param currency    The currency of the transaction.
     * @return The created transaction.
     */
    public static Transaction createSentMoneyTransaction(
            final int timestamp, final String senderIBAN,
            final String receiverIBAN, final double amount,
            final String currency) {
        return new Transaction(
                timestamp,
                "Money sent",
                senderIBAN, receiverIBAN,
                amount, currency,
                "sent", null, null,
                null, null, null, null,
                null, null
        );
    }

    /**
     * Creates a transaction for receiving money.
     *
     * @param timestamp    The timestamp of the transaction.
     * @param senderIBAN   The IBAN of the sender.
     * @param receiverIBAN The IBAN of the receiver.
     * @param amount       The amount of money received.
     * @param exchangeRate The exchange rate used for the conversion.
     * @param currency     The currency of the transaction.
     * @param description  A description of the transaction.
     * @return The created transaction.
     */
    public static Transaction createReceivedMoneyTransaction(
            final int timestamp, final String senderIBAN,
            final String receiverIBAN, final double amount,
            final double exchangeRate, final String currency,
            final String description) {

        // Calculate the final amount using the exchange rate
        double finalAmount = amount * exchangeRate;

        return new Transaction(
                timestamp,
                description,
                senderIBAN, receiverIBAN,
                finalAmount, currency,
                "received", null, null,
                null, null, null, null,
                null, null
        );
    }

    /**
     * Creates a transaction for an online card payment.
     *
     * @param timestamp   The timestamp of the transaction.
     * @param cardNumber  The card number used for the payment.
     * @param amount      The amount paid.
     * @param commerciant The merchant involved in the transaction.
     * @param accountIBAN The IBAN of the account.
     * @return The created transaction.
     */
    public static Transaction createOnlinePaymentTransaction(
            final int timestamp, final String cardNumber,
            final double amount, final String commerciant, final String accountIBAN) {
        double roundedAmount = Math.round(amount * 100.0) / 100.0;

        return new Transaction(
                timestamp,
                "Card payment",
                null, null, roundedAmount,
                null, null, null, null,
                accountIBAN, commerciant, null, null,
                null, null
        );
    }

    /**
     * Creates a transaction indicating insufficient funds in an account.
     *
     * @param timestamp   The timestamp of the transaction.
     * @param accountIBAN The IBAN of the account.
     * @return The created transaction.
     */
    public static Transaction createInsufficientFundsTransaction(
            final int timestamp, final String accountIBAN) {
        return new Transaction(
                timestamp,
                "Insufficient funds",
                null, null, null, null,
                null, null, null, accountIBAN,
                null, null, null,
                null, null
        );
    }

    /**
     * Creates a transaction indicating a card has been deleted.
     *
     * @param timestamp   The timestamp of the transaction.
     * @param accountIBAN The IBAN of the account.
     * @param cardNumber  The card number that was deleted.
     * @param email       The email associated with the card.
     * @return The created transaction.
     */
    public static Transaction createDeletedCardTransaction(
            final int timestamp, final String accountIBAN,
            final String cardNumber, final String email) {
        return new Transaction(
                timestamp,
                "The card has been destroyed",
                null, null, null, null,
                null, cardNumber, email,
                accountIBAN, null, null, null,
                null, null
        );
    }

    /**
     * Creates a warning transaction for reaching the minimum amount of funds.
     *
     * @param timestamp   The timestamp of the transaction.
     * @param accountIBAN The IBAN of the account.
     * @return The created transaction.
     */
    public static Transaction createWarningTransaction(
            final int timestamp, final String accountIBAN) {
        return new Transaction(
                timestamp,
                "You have reached the minimum amount "
                        + "of funds, the card will be frozen",
                null, null, null, null,
                null, null, null, accountIBAN,
                null, null, null,
                null, null
        );
    }

    /**
     * Creates a transaction indicating that the card is frozen.
     *
     * @param timestamp   The timestamp of the transaction.
     * @param accountIBAN The IBAN of the account.
     * @return The created transaction.
     */
    public static Transaction createCardFrozenErrorTransaction(
            final int timestamp, final String accountIBAN) {
        return new Transaction(
                timestamp,
                "The card is frozen",
                null, null, null, null,
                null, null, null, accountIBAN,
                null, null, null,
                null, null
        );
    }

    /**
     * Creates a successful split payment transaction.
     *
     * @param timestamp   The timestamp of the transaction.
     * @param amount      The total amount of the payment.
     * @param splitAmount The amount each recipient receives.
     * @param currency    The currency of the transaction.
     * @param accounts    A list of IBANs involved in the transaction.
     * @return The created transaction.
     */
    public static Transaction createSuccessSplitTransaction(
            final int timestamp, final double amount, final double splitAmount,
            final String currency, final List<String> accounts) {
        String formattedAmount = String.format("%.2f", amount);
        return new Transaction(
                timestamp,
                "Split payment of " + formattedAmount + " " + currency,
                null, null, splitAmount, currency,
                null, null, null, null,
                null, null, accounts,
                null, null
        );
    }

    /**
     * Creates an error transaction for a split payment.
     *
     * @param totalAmount The total amount of the payment.
     * @param timestamp   The timestamp of the transaction.
     * @param splitAmount The amount each recipient receives.
     * @param currency    The currency of the transaction.
     * @param cheapIban   The IBAN with insufficient funds.
     * @param accounts    A list of IBANs involved in the transaction.
     * @return The created transaction.
     */
    public static Transaction createSplitErrorTransaction(
            final double totalAmount, final int timestamp,
            final double splitAmount, final String currency,
            final String cheapIban, final List<String> accounts) {
        String description = "Split payment of "
                + String.format("%.2f", splitAmount) + " " + currency;
        String errorMessage = "Account " + cheapIban
                + " has insufficient funds for a split payment.";

        return new Transaction(
                timestamp,
                description,
                null, null, totalAmount, currency,
                null, null, null, null,
                null, null, accounts, errorMessage, null
        );
    }


    /**
     * Creates a transaction for an error when deleting an account.
     *
     * @param timestamp The timestamp of the transaction.
     * @return The created transaction.
     */
    public static Transaction createDeleteAccountErrorTransaction(
            final int timestamp) {
        return new Transaction(
                timestamp,
                "Account couldn't be deleted - there are funds remaining",
                null, null, null, null,
                null, null, null, null,
                null, null, null,
                null, null
        );
    }

    /**
     * Creates a transaction for an interest rate change.
     *
     * @param timestamp The timestamp of the transaction.
     * @param rate      The new interest rate.
     * @return The created transaction.
     */
    public static Transaction createInterestRateChangeTransaction(
            final int timestamp, final double rate) {
        return new Transaction(
                timestamp,
                "Interest rate of the account changed to " + rate,
                null, null, null, null,
                null, null, null, null,
                null, null, null,
                null, null
        );
    }

    /**
     * Creates a generic error transaction.
     *
     * @param timestamp   The timestamp of the transaction.
     * @param description The error description.
     * @return The created error transaction.
     */
    public static Transaction createErrorTransaction(
            final int timestamp, final String description) {
        return new Transaction(
                timestamp,
                description,
                null, null, null, null,
                null, null, null, null,
                null, null, null,
                null, null
        );
    }

    /**
     * Creates a transaction for upgrading a plan.
     *
     * @param timestamp   The timestamp of the transaction.
     * @param accountIBAN The IBAN of the account.
     * @param newPlanType The new plan type.
     * @return The created transaction.
     */
    public static Transaction createUpgradePlanTransaction(
            final int timestamp, final String accountIBAN,
            final String newPlanType) {
        return new Transaction(
                timestamp,
                "Upgrade plan",
                null, null, null, null,
                null, null, null, accountIBAN,
                null, null, null,
                null,
                newPlanType
        );
    }

    /**
     * Creates a transaction for cash withdrawal.
     *
     * @param timestamp The timestamp of the transaction.
     * @param amount The amount of money to be withdrawn.
     * @return The created cash withdrawal transaction.
     */
    public static Transaction createWithdrawalTransaction(
            final int timestamp, final double amount) {
        return new Transaction(
                timestamp,
                "Cash withdrawal of " + amount,
                null, null, amount, null,
                null, null, null, null,
                null, null, null,
                null, null
        );
    }

    /**
     * Creates a transaction for adding interest to a savings account.
     *
     * @param timestamp The timestamp of the transaction.
     * @param amount    The interest amount added to the account.
     * @param currency  The currency of the transaction.
     * @return The created interest transaction.
     */
    public static Transaction createInterestTransaction(
            final int timestamp, final double amount, final String currency) {
        return new Transaction(
                timestamp,
                "Interest rate income",
                null, null, amount, currency,
                null, null, null, null,
                null, null, null,
                null, null
        );
    }




}
