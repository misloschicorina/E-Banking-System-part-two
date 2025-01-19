package org.poo.main.commands;

import com.fasterxml.jackson.databind.node.ArrayNode;
import org.poo.fileio.CommandInput;
import org.poo.main.accounts.Account;
import org.poo.main.cards.Card;
import org.poo.main.transactions.TransactionService;
import org.poo.main.user.User;

import java.util.List;

public final class DeleteCardCommand implements Command {

    private final List<User> users;
    private final TransactionService transactionService;

    public DeleteCardCommand(final List<User> users,
                             final TransactionService transactionService) {
        this.users = users;
        this.transactionService = transactionService;
    }

    @Override
    public void execute(final CommandInput command, final ArrayNode output) {
        String cardNumber = command.getCardNumber();
        int timestamp = command.getTimestamp();
        String email = command.getEmail();

        // Iterate through all users to find the card to delete
        for (User user : users) {
            for (Account account : user.getAccounts()) {
                for (Card card : account.getCards()) {
                    if (card.getCardNumber().equals(cardNumber)) {
                        User cardOwner = card.getCardOwner();

                        // If the card deleter is an employee,
                        // they can only delete cards they created
                        if (account.isEmployee(email)) {
                            if (!email.equals(cardOwner.getEmail())) {
                                return;
                            }
                        }

                        // Add the transaction to the user's transaction list
                        transactionService.addDeletedCardTransaction(
                                timestamp, account, card, user);

                        // Remove the card from the account
                        account.removeCard(card);
                        return;
                    }
                }
            }
        }
    }
}
