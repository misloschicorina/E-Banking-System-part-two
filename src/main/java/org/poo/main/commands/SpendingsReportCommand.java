package org.poo.main.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.fileio.CommandInput;
import org.poo.main.accounts.Account;
import org.poo.main.transactions.SpendingsTransactionFilter;
import org.poo.main.transactions.TransactionFilter;
import org.poo.main.tools.Tools;
import org.poo.main.user.User;
import org.poo.main.exchange_rate.ExchangeRate;

import java.util.List;

public final class SpendingsReportCommand implements Command {
    private final ObjectMapper objectMapper;
    private final List<User> users;
    private final List<ExchangeRate> exchangeRates;

    public SpendingsReportCommand(final ObjectMapper objectMapper,
                                  final List<User> users, final List<ExchangeRate> exchangeRates) {
        this.objectMapper = objectMapper;
        this.users = users;
        this.exchangeRates = exchangeRates;
    }

    @Override
    public void execute(final CommandInput command, final ArrayNode output) {
        String iban = command.getAccount();
        int timestamp = command.getTimestamp();

        Account account = Tools.findAccountByIBAN(iban, users);

        // Check if the account exists and if it is a savings account
        if (account != null && account.isSavingsAccount()) {
            ObjectNode errorNode = objectMapper.createObjectNode();
            errorNode.put("command", command.getCommand());

            ObjectNode errorOutput = objectMapper.createObjectNode();
            String description = "This kind of report is not supported for a saving account";
            errorOutput.put("error", description);

            errorNode.set("output", errorOutput);
            errorNode.put("timestamp", timestamp);

            output.add(errorNode);
            return;
        }

        // If the account is not a savings account, proceed with generating the report
        ObjectNode reportNode = objectMapper.createObjectNode();
        reportNode.put("command", command.getCommand());

        // Create a specific filter for spendings transactions
        TransactionFilter filter = new SpendingsTransactionFilter();

        // Generate the report data using the filter and provided parameters
        ObjectNode outputNode =
                Tools.generateReportData(command, filter, true, users, exchangeRates);

        reportNode.set("output", outputNode);
        reportNode.put("timestamp", timestamp);

        output.add(reportNode);
    }
}
