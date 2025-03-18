package org.poo.main.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.fileio.CommandInput;
import org.poo.main.accounts.Account;
import org.poo.main.cards.Card;
import org.poo.main.cards.OneTimeCard;
import org.poo.main.exchange_rate.ExchangeRate;
import org.poo.main.transactions.TransactionService;
import org.poo.main.tools.Tools;
import org.poo.main.user.User;
import org.poo.main.commerciant.Commerciant;
import org.poo.utils.Utils;

import java.util.List;

public final class PayOnlineCommand implements Command {
    private final List<User> users;
    private final List<ExchangeRate> exchangeRates;
    private final List<Commerciant> commerciants;
    private final TransactionService transactionService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public PayOnlineCommand(final List<User> users,
                            final List<ExchangeRate> exchangeRates,
                            final List<Commerciant> commerciants,
                            final TransactionService transactionService) {
        this.users = users;
        this.exchangeRates = exchangeRates;
        this.commerciants = commerciants;
        this.transactionService = transactionService;
    }

    @Override
    public void execute(final CommandInput command, final ArrayNode output) {

        String email = command.getEmail();
        String cardNumber = command.getCardNumber();
        double amount = command.getAmount();
        if (amount <= 0) {
            return;
        }
        String currency = command.getCurrency();
        int timestamp = command.getTimestamp();
        String commerciantName = command.getCommerciant();

        User user = Tools.findUserByEmail(email, users);

        if (user == null) {
            return;
        }

        Card card = null;
        Account account = null;

        // Search for the card in the user's accounts
        for (Account acc : user.getAccounts()) {
            for (Card c : acc.getCards()) {
                if (c.getCardNumber().equals(cardNumber)) {
                    card = c;
                    account = acc;
                    break;
                }
            }
            if (card != null) {
                break;
            }
        }

        if (card == null || account == null) {
            payOnlineError("Card not found", timestamp, output);
            return;
        }

        // Check if the card is frozen
        if (card.getStatus().equals("frozen")) {
            // Add the transaction to the user's transaction list
            transactionService.addCardFrozenTransaction(timestamp, user, account.getIban());
            return;
        }

        // Find the commercant based on the name provided
        Commerciant commerciant = Tools.findCommerciantByName(commerciantName, commerciants);
        if (commerciant == null) {
            return;
        }

        // Perform the payment if the card is valid and not frozen
        performPayment(user, card, account, amount, currency, commerciant,
                timestamp, output, email, command.getCurrency());
    }

    private void performPayment(final User user, final Card card, final Account account,
                                final double amount, final String currency,
                                final Commerciant commerciant, final int timestamp,
                                final ArrayNode output, final String email,
                                final String commandCurrency) {
        // Convert the amount to the correct currency if necessary
        double finalAmount = Tools.calculateFinalAmount(account, amount, exchangeRates, currency);

        double balance = account.getBalance();

        // Check if the account has enough balance for the payment
        if (balance >= finalAmount) {
            // Check if the balance is below the minimum allowed
            if (balance <= account.getMinBalance()) {
                // Add the warning transaction to the user's transaction list
                transactionService.addWarningTransaction(timestamp, user, account.getIban());
                // Freeze the card due to low balance
                card.freezeCard();
                return;
            }

            // Employees cannot spend bigger amount than spending limit
            if (account.isBusinessAccount()) {
                if (account.isEmployee(email)) {
                    if (amount > account.getSpendingLimit()) {
                        return;
                    }
                }
            }

            account.spend(finalAmount);
            if (commerciant.getCashbackStrategy().equals("spendingThreshold")) {
                account.addToTotalSpendingThreshold(finalAmount);
            }
            double commission = Tools.calculateComision(user, finalAmount, currency, exchangeRates);

            // Substract the calculated commision from the account
            account.spend((commission));

            // Add the transaction to the user's transaction list
            transactionService.addOnlinePaymentTransaction(timestamp,
                    card, finalAmount, commerciant.getName(), user, account.getIban());

            // strict pr raport la business account
            account.addSpending(email, finalAmount, timestamp, commerciant.getName());

            String accountCurrency = account.getCurrency(); // Moneda contului

            if (account.isApplyingCashback(commerciant, accountCurrency, exchangeRates) != null) {
                double cashback = account.applyCashbackForTransaction(commerciant, amount,
                        account.isApplyingCashback(commerciant, accountCurrency, exchangeRates),
                        accountCurrency, commandCurrency, exchangeRates, user);

                account.deposit(cashback);
            }


            if (card.isOneTimeCard()) {
                handleOneTimeCard(user, card, account, timestamp);
            }

        } else {
            // Add the transaction to the user's transaction list
            transactionService.addInsufficientFundsTransaction(timestamp,
                    "Insufficient funds", user, account.getIban());
        }

    }

    private void handleOneTimeCard(final User user, final Card card,
                                   final Account account, final int timestamp) {

        OneTimeCard oneTimeCard = (OneTimeCard) card;

        // Mark the one-time card as used if it hasn't been used yet
        if (!oneTimeCard.isUsed()) {
            oneTimeCard.markAsUsed();
        }

        // Now the card being used, remove it and create a new one
        if (oneTimeCard.isUsed()) {
            account.removeCard(oneTimeCard);
            // Add the transaction to the user's transaction list
            transactionService.addDeletedCardTransaction(timestamp, account, card, user);

            // Creating a new card after payment
            String newCardNumber = Utils.generateCardNumber();
            OneTimeCard newOneTimeCard = new OneTimeCard(user, account, newCardNumber);

            // Adding the new card in account
            account.addCard(newOneTimeCard);

            // Add the new card transaction to the user's transaction list
            transactionService.addCardTransaction(timestamp, newOneTimeCard, account, user);
        }
    }

    private void payOnlineError(final String description, final int timestamp,
                                final ArrayNode output) {
        ObjectNode errorNode = objectMapper.createObjectNode();
        errorNode.put("command", "payOnline");

        ObjectNode errorOutput = objectMapper.createObjectNode();
        errorOutput.put("timestamp", timestamp);
        errorOutput.put("description", description);

        errorNode.set("output", errorOutput);

        errorNode.put("timestamp", timestamp);

        output.add(errorNode);
    }
}
