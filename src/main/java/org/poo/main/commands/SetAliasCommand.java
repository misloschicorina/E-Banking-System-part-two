package org.poo.main.commands;

import com.fasterxml.jackson.databind.node.ArrayNode;
import org.poo.fileio.CommandInput;
import org.poo.main.accounts.Account;
import org.poo.main.tools.Tools;
import org.poo.main.user.User;

import java.util.List;

public final class SetAliasCommand implements Command {
    private final List<User> users;

    public SetAliasCommand(final List<User> users) {
        this.users = users;
    }

    @Override
    public void execute(final CommandInput command, final ArrayNode output) {
        String email = command.getEmail();
        String alias = command.getAlias();
        String iban = command.getAccount();

        // Find the user by email
        User user = Tools.findUserByEmail(email, users);
        if (user == null) {
            return;
        }

        // Find the account by IBAN
        Account account = Tools.findAccountByIBAN(iban, users);
        if (account == null) {
            return;
        }

        // Set the alias for the account
        account.setAlias(alias);
    }
}
