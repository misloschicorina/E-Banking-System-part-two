package org.poo.main.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.fileio.CommandInput;
import org.poo.main.accounts.Account;
import org.poo.main.Commerciant.Commerciant;
import org.poo.main.exchange_rate.ExchangeRate;
import org.poo.main.tools.Tools;
import org.poo.main.transactions.TransactionService;
import org.poo.main.user.User;

import java.util.List;

public final class SendMoneyCommand implements Command {
    private final List<User> users;
    private final List<ExchangeRate> exchangeRates;
    private final List<Commerciant> commerciants;
    private final TransactionService transactionService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public SendMoneyCommand(final List<User> users,
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
        // Get sender and receiver account info
        String senderIBAN = command.getAccount();
        String receiverIBAN = command.getReceiver();
        double amount = command.getAmount();
        String description = command.getDescription();
        int timestamp = command.getTimestamp();

        boolean isMerchant = Tools.isCommerciantIban(receiverIBAN, commerciants);

        Account senderAccount = Tools.findAccountByIBAN(senderIBAN, users);
        Account receiverAccount = Tools.findAccountByIBAN(receiverIBAN, users);

        if (!isMerchant) {
            if (senderAccount == null) {
                // Try with alias
                senderAccount = Tools.findAccountByAlias(senderIBAN, users);
                if (senderAccount == null) {
                    sendMoneyError("User not found", timestamp, output);
                    return;
                }
            }
            if (receiverAccount == null) {
                // Try with alias
                receiverAccount = Tools.findAccountByAlias(receiverIBAN, users);
                if (receiverAccount == null) {
                    sendMoneyError("User not found", timestamp, output);
                    return;
                }
            }
        }

        // Get sender user details
        User senderUser = Tools.findUserByEmail(command.getEmail(), users);

        // Calculate commission in sender's currency
        double commission =
                Tools.calculateComision(senderUser, amount,
                                        senderAccount.getCurrency(), exchangeRates);

        if (isMerchant) {
            Commerciant commerciant = Tools.findCommerciantByIBAN(receiverIBAN, commerciants);
            senderAccount.spend(amount + commission); // Include commission

            if (commerciant.getCashbackStrategy().equals("spendingThreshold")) {
                senderAccount.addToTotalSpendingThreshold(amount);
            }

            String senderCurrency = senderAccount.getCurrency();
            String applicableCashback =
                    senderAccount.isApplyingCashback(commerciant, senderCurrency, exchangeRates);

            if (applicableCashback != null) {
                double cashback = senderAccount.applyCashbackForTransaction(
                        commerciant,
                        amount,
                        applicableCashback,
                        senderAccount.getCurrency(),
                        senderAccount.getCurrency(),
                        exchangeRates,
                        senderUser
                );

                senderAccount.deposit(cashback);
                String senderName = senderUser.getEmail();
                senderAccount.addSpending(senderName, amount, timestamp, commerciant.getName());
            }

            transactionService.addSendMoneyToCommerciantTransaction(timestamp, senderAccount,
                    commerciant.getAccount(), amount, senderAccount.getCurrency(), description);

            return;
        }

        // Total amount including commission
        double totalAmount = amount + commission;

        // Convert the amount to the receiver's currency if necessary
        double finalAmount = amount;
        double exchangeRate = 1;

        if (!senderAccount.getCurrency().equals(receiverAccount.getCurrency())) {
            exchangeRate = ExchangeRate.getExchangeRate(
                    senderAccount.getCurrency(),
                    receiverAccount.getCurrency(),
                    exchangeRates
            );
            if (exchangeRate == 0) {
                return;
            }
            finalAmount = amount * exchangeRate;
        }

        String senderEmail = senderUser.getEmail();

        if (senderAccount.isBusinessAccount()) {
            if (senderAccount.isEmployee(senderEmail)) {
                if (totalAmount > senderAccount.getSpendingLimit()) {
                    return;
                }
            }
        }

        // Check if the sender has enough funds for the transfer including commission
        if (senderAccount.getBalance() < totalAmount) {
            // Add the transaction to the user's transaction list
            transactionService.addInsufficientFundsTransaction(timestamp,
                    "Insufficient funds", senderUser, senderIBAN);
            return;
        }

        // Perform the transfer
        senderAccount.spend(totalAmount); // Include commission
        receiverAccount.deposit(finalAmount);

        // Add transactions for both the sender and the receiver
        transactionService.addSendMoneyTransaction(timestamp, senderAccount,
                receiverAccount, amount, senderAccount.getCurrency(), description);

        transactionService.addReceivedMoneyTransaction(timestamp, senderAccount,
                receiverAccount, amount, exchangeRate, receiverAccount.getCurrency(), description);


        senderAccount.addSpending(senderEmail, amount, timestamp, null);
    }

    private void sendMoneyError(final String description, final int timestamp,
                                                            final ArrayNode output) {
        ObjectNode errorNode = objectMapper.createObjectNode();
        errorNode.put("command", "sendMoney");

        ObjectNode errorOutput = objectMapper.createObjectNode();
        errorOutput.put("timestamp", timestamp);
        errorOutput.put("description", description);

        errorNode.set("output", errorOutput);
        errorNode.put("timestamp", timestamp);

        output.add(errorNode);
    }
}
