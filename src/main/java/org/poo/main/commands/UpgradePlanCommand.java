package org.poo.main.commands;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.fileio.CommandInput;
import org.poo.main.accounts.Account;
import org.poo.main.exchange_rate.ExchangeRate;
import org.poo.main.tools.Tools;
import org.poo.main.transactions.TransactionService;
import org.poo.main.user.User;

import java.util.List;

public final class UpgradePlanCommand implements Command {
    private final List<User> users;
    private final List<ExchangeRate> exchangeRates;
    private final TransactionService transactionService;

    private static final double STANDARD_TO_SILVER_FEE = 100.0;
    private static final double STANDARD_TO_GOLD_FEE = 350.0;
    private static final double SILVER_TO_GOLD_FEE = 250.0;

    public UpgradePlanCommand(final List<User> users, final List<ExchangeRate> exchangeRates,
                                            final TransactionService transactionService) {
        this.users = users;
        this.exchangeRates = exchangeRates;
        this.transactionService = transactionService;
    }

    @Override
    public void execute(final CommandInput command, final ArrayNode output) {
        String accountIBAN = command.getAccount();
        String newPlan = command.getNewPlanType();
        int timestamp = command.getTimestamp();

        // Find the account and user
        Account account = Tools.findAccountByIBAN(accountIBAN, users);
        if (account == null) {
            ObjectNode errorNode = output.addObject();
            errorNode.put("command", "upgradePlan");
            errorNode.put("timestamp", timestamp);

            ObjectNode innerOutput = errorNode.putObject("output");
            innerOutput.put("description", "Account not found");
            innerOutput.put("timestamp", timestamp);
            return;
        }

        User user = Tools.findUserByAccount(accountIBAN, users);
        if (user == null) {
            return;
        }

        // Only owner can adjust account plan for a business account
        if (account.isBusinessAccount()) {
            if (!user.getEmail().equals(account.getOwnerEmail())) {
                return;
            }
        }

        String currentPlan = user.getAccountPlan();

        if (newPlan.equals(currentPlan)) {
            String description = "The user already has the " + newPlan + " plan.";
            transactionService.addErrorTransaction(timestamp, description, user);
        }
        double upgradeFeeInRON = 0;

        // Find the upgrade fee needed in RON
        switch (currentPlan) {
            case "standard", "student" -> {
                if (newPlan.equals("silver")) {
                    upgradeFeeInRON = STANDARD_TO_SILVER_FEE;
                } else if (newPlan.equals("gold")) {
                    upgradeFeeInRON = STANDARD_TO_GOLD_FEE;
                } else {
                    return;
                }
            }
            case "silver" -> {
                if (newPlan.equals("gold")) {
                    upgradeFeeInRON = SILVER_TO_GOLD_FEE;
                } else {
                    return;
                }
            }
            case "gold" -> {
                return;
            }
            default -> {
            }
        }

        double upgradeFee = upgradeFeeInRON;

        // Convert the ugrade fee (known in RON) in account currency
        if (!account.getCurrency().equals("RON")) {
            double exchangeRate =
                    ExchangeRate.getExchangeRate(account.getCurrency(), "RON", exchangeRates);
            upgradeFee = ((upgradeFeeInRON / exchangeRate) * 100.0) / 100.0;
        }

        // Check if the account has enough funds to upgrade plan
        if (account.getBalance() < upgradeFee) {
            transactionService.addInsufficientFundsTransaction(timestamp,
                    "Insufficient funds for upgrade", user, accountIBAN);
            return;
        }

        // Substract the upgrade fee from account
        account.spend(upgradeFee);

        // Upgrade the plan for given account and for all other accounts of that user
        user.setAccountPlan(newPlan);
        for (Account acc : user.getAccounts()) {
            acc.setAccountPlan(newPlan);
        }

        transactionService.addUpgradePlanTransaction(timestamp, user, newPlan, accountIBAN);
    }
}
