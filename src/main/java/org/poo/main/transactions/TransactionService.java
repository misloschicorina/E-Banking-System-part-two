package org.poo.main.transactions;

import org.poo.main.tools.Tools;
import org.poo.main.accounts.Account;
import org.poo.main.cards.Card;
import org.poo.main.user.User;
import java.util.List;

/**
 * This class is responsible for creating different types of transactions
 * and adding them to the user's list of transactions in the banking system.
 */
public class TransactionService {
    private List<User> users;

    /**
     * Constructs a new TransactionService with a list of users.
     *
     * @param users the list of users in the banking system.
     */
    public TransactionService(final List<User> users) {
        this.users = users;
    }

    /**
     * Creates and adds an account transaction to the user's transaction list.
     *
     * @param timestamp the timestamp of the transaction.
     * @param account the account being created.
     * @param user the user who created the account.
     */
    public void addAccountTransaction(final int timestamp, final Account account,
                                                                final User user) {
        Transaction transaction =
                TransactionFactory.createAccountTransaction(timestamp, account.getIban());
        user.addTransaction(transaction);
    }

    /**
     * Creates and adds a transaction for an error when deleting an account to the
     * user's transaction list.
     *
     * @param timestamp the timestamp of the transaction.
     * @param user the user who tried to delete the account.
     */
    public void addDeleteAccountErrorTransaction(final int timestamp, final User user) {
        Transaction transaction =
                TransactionFactory.createDeleteAccountErrorTransaction(timestamp);
        user.addTransaction(transaction);
    }

    /**
     * Creates and adds a card-related transaction to the user's transaction list.
     *
     * @param timestamp the timestamp of the transaction.
     * @param card the card involved in the transaction.
     * @param account the account associated with the card.
     * @param user the user who performed the transaction.
     */
    public void addCardTransaction(final int timestamp, final Card card,
                                   final Account account, final User user) {
        Transaction transaction =
                TransactionFactory.createCardTransaction(timestamp, card.getCardNumber(),
                user.getEmail(), account.getIban());
        user.addTransaction(transaction);
    }

    /**
     * Creates and adds an online payment transaction to the user's transaction list.
     *
     * @param timestamp the timestamp of the transaction.
     * @param card the card used for the payment.
     * @param amount the amount paid.
     * @param commerciant the name of the merchant receiving the payment.
     * @param user the user who made the payment.
     * @param iban the IBAN of the account from which the payment is made.
     */
    public void addOnlinePaymentTransaction(final int timestamp, final Card card,
                                            final double amount, final String commerciant,
                                            final User user, final String iban) {
        Transaction transaction =
                TransactionFactory.createOnlinePaymentTransaction(timestamp,
                        card.getCardNumber(), amount, commerciant, iban);
        user.addTransaction(transaction);
    }

    /**
     * Creates and adds a send money transaction from one account to another to
     * the sender's transaction list.
     *
     * @param timestamp the timestamp of the transaction.
     * @param sender the account from which the money is being sent.
     * @param receiver the account receiving the money.
     * @param amount the amount of money being sent.
     * @param currency the currency of the transaction.
     * @param description a description of the transaction.
     */
    public void addSendMoneyTransaction(final int timestamp, final Account sender,
                                        final Account receiver, final double amount,
                                        final String currency, final String description) {
        User senderUser = Tools.findUserByEmail(sender.getOwnerEmail(), users);

        Transaction sentTransaction =
                TransactionFactory.createSentMoneyTransaction(timestamp, sender.getIban(),
                receiver.getIban(), amount, currency);

        sentTransaction.setDescription(description);
        senderUser.addTransaction(sentTransaction);
    }

    /**
     * Creates and adds a received money transaction to the receiver's transaction list.
     *
     * @param timestamp the timestamp of the transaction.
     * @param sender the account sending the money.
     * @param receiver the account receiving the money.
     * @param amount the amount of money received.
     * @param rate the exchange rate applied to the transaction (if applicable).
     * @param currency the currency of the transaction.
     * @param description a description of the transaction.
     */
    public void addReceivedMoneyTransaction(final int timestamp, final Account sender,
                                            final Account receiver, final double amount,
                                            final double rate, final String currency,
                                            final String description) {
        User receiverUser = Tools.findUserByEmail(receiver.getOwnerEmail(), users);

        Transaction receivedTransaction =
                TransactionFactory.createReceivedMoneyTransaction(timestamp, sender.getIban(),
                receiver.getIban(), amount, rate, currency, description);

        receiverUser.addTransaction(receivedTransaction);
    }

    /**
     * Creates and adds an insufficient funds transaction to the user's transaction list.
     *
     * @param timestamp the timestamp of the transaction.
     * @param description a description of the error.
     * @param user the user who encountered the insufficient funds error.
     * @param iban the IBAN of the account involved in the error.
     */
    public void addInsufficientFundsTransaction(final int timestamp, final String description,
                                                final User user, final String iban) {
        Transaction failureTransaction =
                TransactionFactory.createInsufficientFundsTransaction(timestamp, iban);
        user.addTransaction(failureTransaction);
    }

    /**
     * Creates and adds a deleted card transaction to the user's transaction list.
     *
     * @param timestamp the timestamp of the transaction.
     * @param account the account from which the card is being deleted.
     * @param card the card being deleted.
     * @param user the user who deleted the card.
     */
    public void addDeletedCardTransaction(final int timestamp, final Account account,
                                          final Card card, final User user) {
        Transaction transaction = TransactionFactory.createDeletedCardTransaction(
                timestamp,
                account.getIban(),
                card.getCardNumber(),
                user.getEmail()
        );
        user.addTransaction(transaction);
    }

    /**
     * Creates and adds a warning transaction to the user's transaction list.
     *
     * @param timestamp the timestamp of the transaction.
     * @param user the user to whom the warning is issued.
     * @param iban the IBAN of the account involved in the warning.
     */
    public void addWarningTransaction(final int timestamp, final User user, final String iban) {
        Transaction warningTransaction =
                TransactionFactory.createWarningTransaction(timestamp, iban);
        user.addTransaction(warningTransaction);
    }

    /**
     * Creates and adds a card frozen error transaction to the user's transaction list.
     *
     * @param timestamp the timestamp of the transaction.
     * @param user the user whose card was frozen.
     * @param iban the IBAN of the account associated with the frozen card.
     */
    public void addCardFrozenTransaction(final int timestamp, final User user,
                                                                final String iban) {
        Transaction frozenTransaction =
                TransactionFactory.createCardFrozenErrorTransaction(timestamp, iban);
        user.addTransaction(frozenTransaction);
    }

    /**
     * Creates and adds a successful split transaction to the user's transaction list.
     *
     * @param timestamp the timestamp of the transaction.
     * @param amount the total amount of the payment.
     * @param splitAmount the amount that each recipient receives.
     * @param currency the currency of the transaction.
     * @param accounts the list of accounts that the payment is split between.
     * @param user the user who initiated the split payment.
     */
    public void addSuccessSplitTransaction(final int timestamp, final double amount,
                                           final double splitAmount, final String currency,
                                           final List<String> accounts, final User user) {
        Transaction splitTransaction =
                TransactionFactory.createSuccessSplitTransaction(timestamp,
                        amount, splitAmount, currency, accounts);
        user.addTransaction(splitTransaction);
    }

    /**
     * Creates and adds a transaction for changing the interest rate on an account.
     *
     * @param timestamp the timestamp of the transaction.
     * @param rate the new interest rate.
     * @param user the user whose account has the interest rate changed.
     */
    public void addInterestRateChangeTransaction(final int timestamp, final double rate,
                                                 final User user) {
        Transaction interestRateChangeTransaction =
                TransactionFactory.createInterestRateChangeTransaction(timestamp, rate);
        user.addTransaction(interestRateChangeTransaction);
    }

    /**
     * Creates and adds a split payment error transaction to the user's transaction list.
     *
     * @param timestamp the timestamp of the transaction.
     * @param splitAmount the split amount for the error.
     * @param totalAmount the total amount of the payment.
     * @param currency the currency of the transaction.
     * @param cheapIBAN the IBAN involved in the error.
     * @param accounts the list of accounts the payment was supposed to be split between.
     * @param user the user who encountered the error.
     */
    public void addSplitErrorTransaction(final int timestamp, final double splitAmount,
                                         final double totalAmount, final String currency,
                                         final String cheapIBAN, final List<String> accounts,
                                         final User user) {
        Transaction splitTransaction =
                TransactionFactory.createSplitErrorTransaction(splitAmount,
                        timestamp, totalAmount, currency, cheapIBAN, accounts);
        user.addTransaction(splitTransaction);
    }

    /**
     * Creates and adds a generic error transaction to the user's transaction list.
     *
     * @param timestamp the timestamp of when the error occurred.
     * @param description a brief description of the error.
     * @param user the user associated with the error.
     */
    public void addErrorTransaction(final int timestamp, final String description,
                                                                final User user) {
        Transaction errorTransaction =
                TransactionFactory.createErrorTransaction(timestamp, description);
        user.addTransaction(errorTransaction);
    }

    /**
     * Creates and adds a transaction for upgrading the user's plan.
     *
     * @param timestamp the timestamp of the transaction.
     * @param user the user upgrading the plan.
     * @param newPlan the new plan type.
     * @param accountIBAN the IBAN of the account used for payment.
     */
    public void addUpgradePlanTransaction(final int timestamp, final User user,
                                          final String newPlan,
                                          final String accountIBAN) {
        Transaction transaction = TransactionFactory.createUpgradePlanTransaction(
                timestamp, accountIBAN, newPlan);
        user.addTransaction(transaction);
    }

    public void addWithdrawalTransaction(final int timestamp, final User user,
                                                            final double amount) {
        Transaction transaction =
                TransactionFactory.createWithdrawalTransaction(timestamp, amount);
        user.addTransaction(transaction);
    }

}
