package org.poo.main.commands;

import org.poo.fileio.CommandInput;
import org.poo.main.accounts.Account;
import org.poo.main.tools.Tools;
import org.poo.main.user.User;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.util.List;

public final class AddFundsCommand implements Command {

    private final List<User> users;

    public AddFundsCommand(final List<User> users) {
        this.users = users;
    }

    @Override
    public void execute(final CommandInput command, final ArrayNode output) {
        Account account = Tools.findAccountByIBAN(command.getAccount(), users);
        String email = command.getEmail();
        double amount = command.getAmount();

        if (account == null) {
            return;
        }

        // Handle business account-specific logic
        if (account.isBusinessAccount()) {
            if (!account.isAssociate(email)) {
                return; // Exit if the user is not an associate of the business account
            }

            if (account.isEmployee(email)) {
                if (amount > account.getDepositLimit()) {
                    return; // Exit if the deposit exceeds the employee's limit
                }
            }
        }

        // Add the funds to the account
        account.deposit(amount);

        // Record the deposit transaction for the user
        account.addDeposit(email, amount, command.getTimestamp());
    }
}
