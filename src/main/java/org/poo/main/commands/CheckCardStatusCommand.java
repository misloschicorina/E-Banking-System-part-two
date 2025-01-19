package org.poo.main.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.fileio.CommandInput;
import org.poo.main.accounts.Account;
import org.poo.main.cards.Card;
import org.poo.main.transactions.TransactionService;
import org.poo.main.user.User;

import java.util.List;

public final class CheckCardStatusCommand implements Command {
    private final List<User> users;
    private final TransactionService transactionService;
    private final ObjectMapper objectMapper;

    private static final double WARNING_THRESHOLD = 30.0;

    public CheckCardStatusCommand(final List<User> users,
                                  final TransactionService transactionService,
                                  final ObjectMapper objectMapper) {
        this.users = users;
        this.transactionService = transactionService;
        this.objectMapper = objectMapper;
    }

    @Override
    public void execute(final CommandInput command, final ArrayNode output) {
        String cardNumber = command.getCardNumber();
        int timestamp = command.getTimestamp();

        // Find the card
        Card foundCard = null;
        Account foundAccount = null;
        User foundUser = null;

        for (User user : users) {
            for (Account account : user.getAccounts()) {
                for (Card card : account.getCards()) {
                    if (card.getCardNumber().equals(cardNumber)) {
                        foundCard = card;
                        foundAccount = account;
                        foundUser = user;
                        break;
                    }
                }
            }
        }

        // If no card is found, return an error response
        if (foundCard == null) {
            cardCheckError("checkCardStatus", timestamp, output);
            return;
        }

        // If the account balance is near the minimum threshold,
        // freeze the card and add a warning transaction
        if (foundAccount.getBalance() - foundAccount.getMinBalance() <= WARNING_THRESHOLD) {
            String iban = foundAccount.getIban();
            transactionService.addWarningTransaction(timestamp, foundUser, iban);
            foundCard.freezeCard();
        }
    }

    private void cardCheckError(final String command, final int timestamp, final ArrayNode output) {
        ObjectNode resultNode = objectMapper.createObjectNode();
        resultNode.put("command", command);

        ObjectNode outputNode = objectMapper.createObjectNode();
        outputNode.put("description", "Card not found");
        outputNode.put("timestamp", timestamp);
        resultNode.set("output", outputNode);
        resultNode.put("timestamp", timestamp);

        output.add(resultNode);
    }
}
