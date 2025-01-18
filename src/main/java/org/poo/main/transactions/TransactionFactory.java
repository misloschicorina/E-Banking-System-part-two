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
                null, null, null, null
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
                null, null, null, null
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
                null, null, null, null
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
                null, null, null, null
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
        return new Transaction(
                timestamp,
                "Card payment",
                null, null, amount,
                null, null, null, null,
                accountIBAN, commerciant, null, null,
                null, null, null, null
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
                null, null, null, null
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
                null, null, null, null
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
                null, null, null, null
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
                null, null, null, null
        );
    }

    public static Transaction createSuccessSplitTransaction(
            final int timestamp, final double totalAmount, final List<Double> amountsForUsers,
            final String currency, final List<String> accounts, final String splitPaymentType) {
        String formattedAmount = String.format("%.2f", totalAmount);
        return new Transaction(
                timestamp,
                "Split payment of " + formattedAmount + " " + currency,
                null, null, null, currency,
                null, null, null, null,
                null, null, accounts,
                null, null,
                splitPaymentType, // Tipul split payment-ului (e.g., "equal", "custom")
                amountsForUsers   // Lista cu sumele asociate fiecărui utilizator
        );
    }

    public static Transaction createSplitErrorTransaction(
            final double totalAmount, final int timestamp,
            final List<Double> amountsForUsers, final String currency,
            final String cheapIban, final List<String> accounts,
            final String splitPaymentType) {

        String description = "Split payment of "
                + String.format("%.2f", totalAmount) + " " + currency;
        String errorMessage = "Account " + cheapIban
                + " has insufficient funds for a split payment.";

        return new Transaction(
                timestamp,
                description,
                null, null, totalAmount, currency,
                null, null, null, null,
                null, null, accounts, errorMessage,
                null,                         // Valoare pentru `plan`
                splitPaymentType,             // Tipul split payment-ului
                amountsForUsers               // Lista sumelor pentru utilizatori
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
                null, null, null, null
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
                null, null, null, null
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
                null, null, null, null
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
                newPlanType, null, null
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
                null, null, null, null
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
                null, null, null, null
        );
    }

    public static Transaction createSplitRejectTransaction(
            final double totalAmount, final int timestamp,
            final List<Double> amountsForUsers, final String currency,
            final List<String> accounts,
            final String splitPaymentType) {

        String description = "Split payment of "
                + String.format("%.2f", totalAmount) + " " + currency;
        String errorMessage = "One user rejected the payment.";

        return new Transaction(
                timestamp,
                description,
                null, null, totalAmount, currency,
                null, null, null, null,
                null, null, accounts, errorMessage,
                null,                         // Valoare pentru `plan`
                splitPaymentType,             // Tipul split payment-ului
                amountsForUsers               // Lista sumelor pentru utilizatori
        );
    }

    /**
     * Creates a transaction for transferring money from a savings account to a classic account.
     *
     * @param timestamp           The timestamp of the transaction.
     * @param amount              The amount of money transferred.
     * @param savingsAccountIBAN  The IBAN of the savings account (sender).
     * @param classicAccountIBAN  The IBAN of the classic account (receiver).
     * @return The created transaction for a "Savings withdrawal".
     */
    public static Transaction createSavingsWithdrawalTransaction(
            final int timestamp,
            final double amount,
            final String savingsAccountIBAN,
            final String classicAccountIBAN) {

        return new Transaction(
                timestamp,
                "Savings withdrawal",     // description
                savingsAccountIBAN,      // senderIBAN
                classicAccountIBAN,      // receiverIBAN
                amount,                  // amount
                null,                    // currency (null if none provided)
                null,            // transferType (arbitrary, e.g., "withdrawal")
                null,                    // cardNumber
                null,                    // cardHolder
                null,                    // accountIBAN
                null,                    // commerciant
                null,                    // exchangeRate
                null,                    // error
                null,
                null,                    // plan
                null,                    // splitPaymentType
                null                     // amountForUsers
        );
    }









}
