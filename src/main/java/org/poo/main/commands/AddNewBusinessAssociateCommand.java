package org.poo.main.commands;

import com.fasterxml.jackson.databind.node.ArrayNode;
import org.poo.fileio.CommandInput;
import org.poo.main.accounts.Account;
import org.poo.main.tools.Tools;
import org.poo.main.user.User;

import java.util.List;

public final class AddNewBusinessAssociateCommand implements Command {
    private final List<User> users;

    public AddNewBusinessAssociateCommand(final List<User> users) {
        this.users = users;
    }

    @Override
    public void execute(final CommandInput command, final ArrayNode output) {
        String iban = command.getAccount();
        Account businessAccount = Tools.findAccountByIBAN(iban, users);

        if (businessAccount == null || !businessAccount.getAccountType().equals("business")) {
            return;
        }

        String role = command.getRole(); // manager or employee
        String newEmail = command.getEmail();

        User user = Tools.findUserByEmail(newEmail, users);
        if (user == null) {
            return;
        }

        // Add the account to the user's list of accounts
        user.addAccount(businessAccount);

        // Synchronize all existing cards in the business account with the new user's account
        for (var card : businessAccount.getCards()) {
            Tools.addCardToAllInstances(card, iban, users);
        }

        if (role.equals("manager")) {
            businessAccount.addManagerEmail(newEmail);
        }

        if (role.equals("employee")) {
            businessAccount.addEmployeeEmail(newEmail);
        }
    }
}
