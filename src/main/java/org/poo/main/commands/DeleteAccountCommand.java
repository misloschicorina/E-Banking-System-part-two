package org.poo.main.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.fileio.CommandInput;
import org.poo.main.accounts.Account;
import org.poo.main.cards.Card;
import org.poo.main.transactions.TransactionService;
import org.poo.main.tools.Tools;
import org.poo.main.user.User;

import java.util.List;

public final class DeleteAccountCommand implements Command {

    private final List<User> users;
    private final TransactionService transactionService;
    private final ObjectMapper objectMapper;

    public DeleteAccountCommand(final List<User> users,
                                final TransactionService transactionService,
                                final ObjectMapper objectMapper) {
        this.users = users;
        this.transactionService = transactionService;
        this.objectMapper = objectMapper;
    }

    @Override
    public void execute(final CommandInput command, final ArrayNode output) {
        User user = Tools.findUserByEmail(command.getEmail(), users);
        int timestamp = command.getTimestamp();

        if (user == null) {
            return;
        }

        String iban = command.getAccount();
        Account foundAccount = Tools.findAccountByIBAN(iban, users);

        if (foundAccount == null) {
            return;
        }

        // Only owner can delete the account
        if (!foundAccount.getOwnerEmail().equals(user.getEmail())) {
            return;
        }

        // Check if the account balance is zero
        if (foundAccount.getBalance() == 0) {
            // Destroy all associated cards
            List<Card> associatedCards = foundAccount.getCards();
            for (Card card : associatedCards) {
                transactionService.addDeletedCardTransaction(timestamp, foundAccount, card, user);
            }

            // Remove all cards and the account
            foundAccount.clearCards();
            user.removeAccount(foundAccount);

            // Create success response
            ObjectNode commandResultNode = objectMapper.createObjectNode();
            commandResultNode.put("command", "deleteAccount");

            ObjectNode outputNode = objectMapper.createObjectNode();
            outputNode.put("success", "Account deleted");
            outputNode.put("timestamp", command.getTimestamp());

            commandResultNode.set("output", outputNode);
            commandResultNode.put("timestamp", command.getTimestamp());

            output.add(commandResultNode);
        } else {
            // Handle error if balance is not zero
            deleteAccountError(command, output);

            // Add the error transaction to the user's transaction list
            transactionService.addDeleteAccountErrorTransaction(timestamp, user);
        }
    }

    private void deleteAccountError(final CommandInput command, final ArrayNode output) {
        ObjectNode errorNode = objectMapper.createObjectNode();
        errorNode.put("command", "deleteAccount");

        ObjectNode errorOutput = objectMapper.createObjectNode();
        errorOutput.put("error",
                "Account couldn't be deleted - see org.poo.transactions for details");
        errorOutput.put("timestamp", command.getTimestamp());

        errorNode.set("output", errorOutput);
        errorNode.put("timestamp", command.getTimestamp());

        output.add(errorNode);
    }
}
