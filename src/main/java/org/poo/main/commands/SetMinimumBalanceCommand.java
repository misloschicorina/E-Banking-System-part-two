package org.poo.main.commands;

import com.fasterxml.jackson.databind.node.ArrayNode;
import org.poo.fileio.CommandInput;
import org.poo.main.accounts.Account;
import org.poo.main.tools.Tools;
import org.poo.main.user.User;

import java.util.List;

public final class SetMinimumBalanceCommand implements Command {
    private final List<User> users;

    public SetMinimumBalanceCommand(final List<User> users) {
        this.users = users;
    }

    @Override
    public void execute(final CommandInput command, final ArrayNode output) {
        String iban = command.getAccount();
        double limit = command.getAmount();

        // Find the user by account
        User user = Tools.findUserByAccount(iban, users);
        if (user == null) {
            return;
        }

        // Find the account by IBAN
        Account account = Tools.findAccountByIBAN(iban, users);

        // Only the account owner can set the minimum balance
        if (account != null && account.getOwnerEmail().equals(user.getEmail())) {
            account.setMinBalance(limit);
        }
    }
}
