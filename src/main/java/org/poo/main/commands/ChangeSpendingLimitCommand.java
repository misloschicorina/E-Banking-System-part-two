package org.poo.main.commands;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.fileio.CommandInput;
import org.poo.main.accounts.Account;
import org.poo.main.tools.Tools;
import org.poo.main.user.User;

import java.util.List;

public final class ChangeSpendingLimitCommand implements Command {
    private final List<User> users;

    public ChangeSpendingLimitCommand(final List<User> users) {
        this.users = users;
    }

    @Override
    public void execute(final CommandInput command, final ArrayNode output) {
        int timestamp = command.getTimestamp();
        String email = command.getEmail();
        String iban = command.getAccount();
        double newLimit = command.getAmount();

        Account account = Tools.findAccountByIBAN(iban, users);
        if (account == null || !account.getAccountType().equals("business")) {
            return;
        }

        if (!account.getOwnerEmail().equals(email)) {
            String description = "You must be owner in order to change spending limit.";

            ObjectNode errorNode = output.addObject();
            errorNode.put("command", "changeSpendingLimit");

            ObjectNode outputNode = errorNode.putObject("output");
            outputNode.put("description", description);
            outputNode.put("timestamp", timestamp);

            errorNode.put("timestamp", timestamp);
            return;
        }

        account.setSpendingLimit(newLimit, email);
    }
}
