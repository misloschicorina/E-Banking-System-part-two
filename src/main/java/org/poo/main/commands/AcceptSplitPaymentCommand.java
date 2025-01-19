package org.poo.main.commands;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.fileio.CommandInput;
import org.poo.main.exchange_rate.ExchangeRate;
import org.poo.main.split.SplitPayment;
import org.poo.main.tools.Tools;
import org.poo.main.user.User;
import org.poo.main.accounts.Account;
import org.poo.main.transactions.TransactionService;

import java.util.List;
import java.util.Map;

public final class AcceptSplitPaymentCommand implements Command {
    private final List<User> users;
    private final TransactionService transactionService;
    private final List<ExchangeRate> exchangeRates;

    public AcceptSplitPaymentCommand(final List<User> users,
                                     final TransactionService transactionService,
                                     final List<ExchangeRate> exchangeRates) {
        this.users = users;
        this.transactionService = transactionService;
        this.exchangeRates = exchangeRates;
    }

    @Override
    public void execute(final CommandInput command, final ArrayNode output) {
        String email = command.getEmail();
        int timestamp = command.getTimestamp();

        User user = Tools.findUserByEmail(email, users);
        if (user == null) {
            ObjectNode errorNode = output.addObject();
            errorNode.put("command", "acceptSplitPayment");
            errorNode.put("timestamp", timestamp);

            ObjectNode innerOutput = errorNode.putObject("output");
            innerOutput.put("description", "User not found");
            innerOutput.put("timestamp", timestamp);

            return;
        }

        // Retrieve the oldest unaccepted split payment transaction
        Map.Entry<SplitPayment, String> paymentEntry = user.getOldestUnacceptedTransaction();
        if (paymentEntry == null) {
            return;
        }

        String userIban = paymentEntry.getValue();
        SplitPayment splitPayment = paymentEntry.getKey();

        // Mark the user's acceptance for the split payment
        splitPayment.setStatus(userIban, true);

        // Check if all users have accepted
        if (splitPayment.allAccepted()) {
            String insufficientFundsIBAN = Tools.verifyAmounts(splitPayment, exchangeRates, users);

            if (insufficientFundsIBAN != null) {
                handleErrorSplitPayment(splitPayment, insufficientFundsIBAN);
            } else {
                processSplitPayment(splitPayment);
            }
        }
    }

    private void processSplitPayment(final SplitPayment splitPayment) {
        // Create and add success transactions for all involved users
        for (String iban : splitPayment.getAccounts()) {
            User involvedUser = Tools.findUserByAccount(iban, users);
            if (involvedUser != null) {
                transactionService.addSuccessSplitTransaction(
                        splitPayment.getTimestamp(),
                        splitPayment.getAmounts().stream().mapToDouble(Double::doubleValue).sum(),
                        splitPayment.getAmounts(),
                        splitPayment.getCurrency(),
                        splitPayment.getAccounts(),
                        splitPayment.getSplitPaymentType(),
                        involvedUser
                );
            }
        }

        // Deduct the amounts from each account
        for (int i = 0; i < splitPayment.getAccounts().size(); i++) {
            String iban = splitPayment.getAccounts().get(i);
            Account account = Tools.findAccountByIBAN(iban, users);
            double amount = splitPayment.getAmounts().get(i);
            String currency = splitPayment.getCurrency();
            double convertedAmount =
                    Tools.calculateFinalAmount(account, amount, exchangeRates, currency);
            account.spend(convertedAmount);
        }

        // Remove the completed split payment from all users
        Tools.removeSplitPaymentFromUsers(splitPayment, users);
    }

    private void handleErrorSplitPayment(final SplitPayment splitPayment,
                                         final String insufficientFundsIBAN) {
        // Create and add error transactions for all involved users
        for (String iban : splitPayment.getAccounts()) {
            User involvedUser = Tools.findUserByAccount(iban, users);
            if (involvedUser != null) {
                transactionService.addSplitErrorTransaction(
                        splitPayment.getTimestamp(),
                        splitPayment.getAmounts().stream().mapToDouble(Double::doubleValue).sum(),
                        splitPayment.getAmounts(),
                        splitPayment.getCurrency(),
                        insufficientFundsIBAN,
                        splitPayment.getAccounts(),
                        splitPayment.getSplitPaymentType(),
                        involvedUser
                );
            }
        }

        // Remove the failed split payment from all users
        Tools.removeSplitPaymentFromUsers(splitPayment, users);
    }
}
