package org.poo.main.commands;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.fileio.CommandInput;
import org.poo.main.accounts.Account;
import org.poo.main.accounts.SavingsAccount;
import org.poo.main.tools.Tools;
import org.poo.main.transactions.TransactionService;
import org.poo.main.user.User;

import java.util.List;

public final class ChangeInterestRateCommand implements Command {
    private final List<User> users;
    private final TransactionService transactionService;

    public ChangeInterestRateCommand(final List<User> users,
                                     final TransactionService transactionService) {
        this.users = users;
        this.transactionService = transactionService;
    }

    @Override
    public void execute(final CommandInput command, final ArrayNode output) {
        int timestamp = command.getTimestamp();
        String iban = command.getAccount();
        double interestRate = command.getInterestRate();

        Account account = Tools.findAccountByIBAN(iban, users);
        User user = Tools.findUserByAccount(iban, users);

        if (account == null || user == null) {
            return;
        }

        // Check if the account is a savings account
        if (account.isSavingsAccount()) {
            SavingsAccount savingsAccount = (SavingsAccount) account;
            savingsAccount.setInterestRate(interestRate);

            transactionService.addInterestRateChangeTransaction(timestamp, interestRate, user);
        } else {
            changeInterestRateError("changeInterestRate", timestamp, output);
        }
    }

    private void changeInterestRateError(final String commandName, final int timestamp,
                                                            final ArrayNode output) {
        ObjectNode result = output.addObject();
        result.put("command", commandName);
        result.put("timestamp", timestamp);

        ObjectNode errorOutput = result.putObject("output");
        errorOutput.put("description", "This is not a savings account");
        errorOutput.put("timestamp", timestamp);
    }
}
