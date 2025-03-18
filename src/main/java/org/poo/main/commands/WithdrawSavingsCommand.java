package org.poo.main.commands;

import com.fasterxml.jackson.databind.node.ArrayNode;
import org.poo.fileio.CommandInput;
import org.poo.main.accounts.Account;
import org.poo.main.accounts.ClassicAccount;
import org.poo.main.accounts.SavingsAccount;
import org.poo.main.exchange_rate.ExchangeRate;
import org.poo.main.tools.Tools;
import org.poo.main.transactions.TransactionService;
import org.poo.main.user.User;

import java.util.List;

public final class WithdrawSavingsCommand implements Command {
    private final List<User> users;
    private final List<ExchangeRate> exchangeRates;
    private final TransactionService transactionService;

    public WithdrawSavingsCommand(final List<User> users,
                                  final List<ExchangeRate> exchangeRates,
                                  final TransactionService transactionService) {
        this.users = users;
        this.exchangeRates = exchangeRates;
        this.transactionService = transactionService;
    }

    @Override
    public void execute(final CommandInput command, final ArrayNode output) {
        String savingsIban = command.getAccount();
        double amount = command.getAmount();
        String currency = command.getCurrency();
        int timestamp = command.getTimestamp();

        Account savingsAccount = Tools.findAccountByIBAN(savingsIban, users);
        User user = Tools.findUserByAccount(savingsIban, users);

        if (savingsAccount == null || user == null) {
            String description = "Account not found";
            transactionService.addErrorTransaction(timestamp, description, user);
            return;
        }

        if (!(savingsAccount.isSavingsAccount())) {
            String description = "Account is not of type savings";
            transactionService.addErrorTransaction(timestamp, description, user);
            return;
        }

        SavingsAccount savings = (SavingsAccount) savingsAccount;

        if (!user.hasMinAge()) {
            String description = "You don't have the minimum age required.";
            transactionService.addErrorTransaction(timestamp, description, user);
            return;
        }

        // Find the first classic account in the specified currency
        Account classicAccount = null;
        for (Account acc : user.getAccounts()) {
            if (acc instanceof ClassicAccount && acc.getCurrency().equals(currency)) {
                classicAccount = acc;
                break;
            }
        }

        if (classicAccount == null) {
            String description = "You do not have a classic account.";
            transactionService.addErrorTransaction(timestamp, description, user);
            return;
        }

        // Check available funds
        double exchangeRate =
                ExchangeRate.getExchangeRate(savings.getCurrency(), currency, exchangeRates);
        double requiredAmount = amount / exchangeRate; // Convert to the savings account currency

        if (savings.getBalance() < requiredAmount) {
            transactionService.addErrorTransaction(timestamp, "Insufficient funds", user);
            return;
        }

        // Transfer funds
        savings.spend(requiredAmount);
        classicAccount.deposit(amount);

        // Add transactions for both accounts
        transactionService.addSavingsWithdrawalTransaction(timestamp,
                amount, savings.getIban(), classicAccount.getIban(), user);
        transactionService.addSavingsWithdrawalTransaction(timestamp,
                amount, savings.getIban(), classicAccount.getIban(), user);
    }
}
