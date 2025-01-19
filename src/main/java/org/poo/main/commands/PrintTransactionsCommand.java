package org.poo.main.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.fileio.CommandInput;
import org.poo.main.tools.Tools;
import org.poo.main.transactions.Transaction;
import org.poo.main.user.User;

import java.util.ArrayList;
import java.util.List;

public final class PrintTransactionsCommand implements Command {
    private final ObjectMapper objectMapper;
    private final List<User> users;

    public PrintTransactionsCommand(final ObjectMapper objectMapper,
                                    final List<User> users) {
        this.objectMapper = objectMapper;
        this.users = users;
    }

    @Override
    public void execute(final CommandInput command, final ArrayNode output) {
        String email = command.getEmail();
        int timestamp = command.getTimestamp();

        // Find the user by email
        User user = Tools.findUserByEmail(email, users);
        if (user == null) {
            return;
        }

        // Create the result node for the transaction output
        ObjectNode resultNode = objectMapper.createObjectNode();
        resultNode.put("command", "printTransactions");
        resultNode.put("timestamp", timestamp);

        // Get all transactions for the user
        List<Transaction> transactions = new ArrayList<>(user.getTransactions());

        // Generate the nodes representing the transactions
        ArrayNode transactionsArray = Tools.getTransactions(transactions);
        resultNode.set("output", transactionsArray);

        // Add the final result node to the output array
        output.add(resultNode);
    }
}
