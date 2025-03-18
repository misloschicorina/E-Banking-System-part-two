package org.poo.main.commands;

import org.poo.fileio.CommandInput;
import org.poo.main.accounts.Account;
import org.poo.main.cards.Card;
import org.poo.main.cards.CardFactory;
import org.poo.main.transactions.TransactionService;
import org.poo.main.tools.Tools;
import org.poo.main.user.User;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.util.List;

public final class CreateOneTimeCardCommand implements Command {

    private final List<User> users;
    private final TransactionService transactionService;

    public CreateOneTimeCardCommand(final List<User> users,
                                    final TransactionService transactionService) {
        this.users = users;
        this.transactionService = transactionService;
    }

    @Override
    public void execute(final CommandInput command, final ArrayNode output) {
        // Find the user by email
        User user = Tools.findUserByEmail(command.getEmail(), users);

        if (user == null) {
            return;
        }

        // Find the account by IBAN
        Account account = Tools.findAccountByIBAN(command.getAccount(), users);

        if (account != null) {
            String iban = account.getIban();

            // Generate a unique card number
            String cardNumber = org.poo.utils.Utils.generateCardNumber();

            // Use the factory to create a one-time card
            Card oneTimeCard =
                    CardFactory.createCard(CardFactory.CardType.ONE_TIME,
                                                user, account, cardNumber);

            // Add the one-time card to the user's account
            account.addCard(oneTimeCard);

            // Synchronize the card across all instances
            Tools.addCardToAllInstances(oneTimeCard, iban, users);

            // Add the transaction to the user's transaction list
            transactionService.addCardTransaction(command.getTimestamp(),
                                                    oneTimeCard, account, user);
        }
    }
}
