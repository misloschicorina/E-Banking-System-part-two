package org.poo.main.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.fileio.CommandInput;
import org.poo.main.transactions.ReportTransactionFilter;
import org.poo.main.transactions.TransactionFilter;
import org.poo.main.tools.Tools;
import org.poo.main.user.User;
import org.poo.main.exchange_rate.ExchangeRate;

import java.util.List;

public final class ReportCommand implements Command {
    private final ObjectMapper objectMapper;
    private final List<User> users;
    private final List<ExchangeRate> exchangeRates;

    public ReportCommand(final ObjectMapper objectMapper,
                         final List<User> users, final List<ExchangeRate> exchangeRates) {
        this.objectMapper = objectMapper;
        this.users = users;
        this.exchangeRates = exchangeRates;
    }

    @Override
    public void execute(final CommandInput command, final ArrayNode output) {
        ObjectNode reportNode = objectMapper.createObjectNode();
        reportNode.put("command", command.getCommand());

        // Create a filter object for filtering the transactions in the report
        TransactionFilter filter = new ReportTransactionFilter();

        // Generate the data for the report using a utility method from Tools
        ObjectNode outputNode =
                Tools.generateReportData(command, filter, false, users, exchangeRates);

        reportNode.set("output", outputNode);
        reportNode.put("timestamp", command.getTimestamp());

        output.add(reportNode);
    }
}
