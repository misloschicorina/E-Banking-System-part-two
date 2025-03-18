package org.poo.main.commands;

import org.poo.main.accounts.Account;
import org.poo.main.accounts.AccountFactory;
import org.poo.main.exchange_rate.ExchangeRate;
import org.poo.main.transactions.TransactionService;
import org.poo.main.tools.Tools;
import org.poo.main.user.User;
import org.poo.fileio.CommandInput;
import org.poo.utils.Utils;

import com.fasterxml.jackson.databind.node.ArrayNode;

import java.util.List;

public final class AddAccountCommand implements Command {
    private final List<User> users;
    private final List<ExchangeRate> exchangeRates;
    private final TransactionService transactionService;

    public AddAccountCommand(final List<User> users, final List<ExchangeRate> exchangeRates,
                                                final TransactionService transactionService) {
        this.users = users;
        this.exchangeRates = exchangeRates;
        this.transactionService = transactionService;
    }

    @Override
    public void execute(final CommandInput command, final ArrayNode output) {
        User user = Tools.findUserByEmail(command.getEmail(), users);

        if (user == null) {
            return;
        }

        int timestamp = command.getTimestamp();
        String accountType = command.getAccountType();
        String currency = command.getCurrency();
        String iban = Utils.generateIBAN();

        Account account = null;

        try {
            switch (accountType) {
                case "classic":
                    account = AccountFactory.createAccount(
                            AccountFactory.AccountType.CLASSIC,
                            currency,
                            command.getEmail(),
                            iban,
                            null);
                    break;

                case "savings":
                    double interestRate = command.getInterestRate();
                    account = AccountFactory.createAccount(
                            AccountFactory.AccountType.SAVINGS,
                            currency,
                            command.getEmail(),
                            iban,
                            interestRate);
                    break;

                case "business":
                    account = AccountFactory.createAccount(
                            AccountFactory.AccountType.BUSINESS,
                            currency,
                            command.getEmail(),
                            iban,
                            null);

                    double initialLimitInRON = account.getSpendingLimit();
                    double exchangeRate =
                            ExchangeRate.getExchangeRate("RON", currency, exchangeRates);
                    double convertedLimit = initialLimitInRON * exchangeRate;

                    account.setSpendingLimit(convertedLimit, command.getEmail());
                    account.setDepositLimit(convertedLimit, command.getEmail());
                    break;

                default:
                    throw new IllegalArgumentException("Unsupported account type: " + accountType);
            }
        } catch (IllegalArgumentException e) {
            System.err.println("Failed to create account: " + e.getMessage());
            return;
        }

        // Add the account to the user's list of accounts
        user.addAccount(account);

        // Add the transaction to the user's transaction list
        if (!account.isBusinessAccount()) {
            transactionService.addAccountTransaction(timestamp, account, user);
        }
    }
}
