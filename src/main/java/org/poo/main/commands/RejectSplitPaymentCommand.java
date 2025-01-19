package org.poo.main.commands;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.fileio.CommandInput;
import org.poo.main.split.SplitPayment;
import org.poo.main.tools.Tools;
import org.poo.main.transactions.TransactionService;
import org.poo.main.user.User;

import java.util.List;

public final class RejectSplitPaymentCommand implements Command {
    private final List<User> users;
    private final TransactionService transactionService;

    public RejectSplitPaymentCommand(final List<User> users,
                                     final TransactionService transactionService) {
        this.users = users;
        this.transactionService = transactionService;
    }

    @Override
    public void execute(final CommandInput command, final ArrayNode output) {
        String email = command.getEmail();
        int timestamp = command.getTimestamp();

        User user = Tools.findUserByEmail(email, users);
        if (user == null) {
            ObjectNode errorNode = output.addObject();

            // Create error node for response
            errorNode.put("command", "rejectSplitPayment");
            errorNode.put("timestamp", timestamp);

            ObjectNode innerOutput = errorNode.putObject("output");
            innerOutput.put("description", "User not found");
            innerOutput.put("timestamp", timestamp);

            return;
        }

        // Retrieve the oldest pending split payment
        SplitPayment splitPayment = user.getOldestPendingTransaction();

        if (splitPayment == null) {
            return;
        }

        // Process rejection for all involved users
        for (String iban : splitPayment.getAccounts()) {
            User involvedUser = Tools.findUserByAccount(iban, users);
            if (involvedUser != null) {
                transactionService.addSplitRejectTransaction(
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

        // Remove the rejected split payment from all users
        Tools.removeSplitPaymentFromUsers(splitPayment, users);
    }
}
