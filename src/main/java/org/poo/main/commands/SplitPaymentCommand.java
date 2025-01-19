package org.poo.main.commands;

import org.poo.fileio.CommandInput;
import org.poo.main.split.SplitPayment;
import org.poo.main.split.SplitStrategy;
import org.poo.main.split.EqualSplitStrategy;
import org.poo.main.split.CustomSplitStrategy;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.poo.main.tools.Tools;
import org.poo.main.user.User;

import java.util.List;

public final class SplitPaymentCommand implements Command {
    private final List<User> users;

    public SplitPaymentCommand(final List<User> users) {
        this.users = users;
    }

    @Override
    public void execute(final CommandInput command, final ArrayNode output) {
        List<String> ibans = command.getAccounts();
        String splitType = command.getSplitPaymentType();
        double totalAmount = command.getAmount();
        String currency = command.getCurrency();
        int timestamp = command.getTimestamp();

        SplitPayment splitPayment = new SplitPayment(splitType, currency, timestamp);

        // Add IBANs to the SplitPayment
        for (String iban : ibans) {
            splitPayment.addAccount(iban);
        }

        // Choose the appropriate strategy based on the split type
        SplitStrategy strategy;
        switch (splitType) {
            case "equal" -> strategy = new EqualSplitStrategy();
            case "custom" -> strategy = new CustomSplitStrategy();
            default -> throw new IllegalArgumentException("Invalid split payment type");
        }

        // Calculate the amounts using the chosen strategy
        List<Double> amounts = strategy.calculateSplit(totalAmount, ibans, command);

        // Add the calculated amounts to the SplitPayment
        for (double amount : amounts) {
            splitPayment.addAmount(amount);
        }

        // Add the transaction to the pending list for each user
        for (String iban : ibans) {
            User user = Tools.findUserByAccount(iban, users);
            if (user != null) {
                user.addPendingSplitPayment(splitPayment);
            }
        }
    }
}
