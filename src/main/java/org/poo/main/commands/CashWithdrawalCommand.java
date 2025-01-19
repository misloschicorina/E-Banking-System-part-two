package org.poo.main.commands;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.fileio.CommandInput;
import org.poo.main.accounts.Account;
import org.poo.main.cards.Card;
import org.poo.main.exchange_rate.ExchangeRate;
import org.poo.main.tools.Tools;
import org.poo.main.transactions.TransactionService;
import org.poo.main.user.User;

import java.util.List;

public final class CashWithdrawalCommand implements Command {
    private final List<User> users;
    private final List<ExchangeRate> exchangeRates;
    private final TransactionService transactionService;

    public CashWithdrawalCommand(final List<User> users,
                                 final List<ExchangeRate> exchangeRates,
                                 final TransactionService transactionService) {
        this.users = users;
        this.exchangeRates = exchangeRates;
        this.transactionService = transactionService;
    }

    @Override
    public void execute(final CommandInput command, final ArrayNode output) {
        int timestamp = command.getTimestamp();
        User user = Tools.findUserByEmail(command.getEmail(), users);

        if (user == null) {
            addErrorOutput("cashWithdrawal", "User not found", timestamp, output);
            return;
        }

        String cardNumber = command.getCardNumber();
        Card card = Tools.findCardByCardNumber(cardNumber, users);

        if (card == null) {
            addErrorOutput("cashWithdrawal", "Card not found", timestamp, output);
            return;
        }

        Account account = card.getAccount();

        if (account == null) {
            return;
        }

        double amountInRON = command.getAmount(); // the amount given in command input is in "RON"

        // Calculate and add commission (still in "RON")
        double commission =
                Tools.calculateComision(user, amountInRON, "RON", exchangeRates);
        amountInRON += commission;

        // Convert the amount + commission to the account's currency
        double exchangeRate =
                ExchangeRate.getExchangeRate("RON", account.getCurrency(), exchangeRates);
        double amountInAccountCurrency = amountInRON * exchangeRate;

        // Check if there are sufficient funds to perform withdrawal
        if (account.getBalance() >= amountInAccountCurrency) {
            account.spend(amountInAccountCurrency);
            transactionService.addWithdrawalTransaction(timestamp, user, amountInRON - commission);
        } else {
            transactionService.addInsufficientFundsTransaction(timestamp,
                    "Insufficient funds", user, account.getIban());
        }
    }

    private void addErrorOutput(final String command, final String description,
                                final int timestamp, final ArrayNode output) {
        ObjectNode errorOutput = output.addObject();
        errorOutput.put("command", command);

        ObjectNode errorDetails = errorOutput.putObject("output");
        errorDetails.put("description", description);
        errorDetails.put("timestamp", timestamp);

        errorOutput.put("timestamp", timestamp);
    }
}
