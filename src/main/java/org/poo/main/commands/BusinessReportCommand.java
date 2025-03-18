package org.poo.main.commands;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.fileio.CommandInput;
import org.poo.main.accounts.Account;
import org.poo.main.accounts.BusinessAccount;
import org.poo.main.tools.Tools;
import org.poo.main.transactions.TransactionDetail;
import org.poo.main.user.User;

import java.util.*;

public final class BusinessReportCommand implements Command {
    private final List<User> users;

    public BusinessReportCommand(final List<User> users) {
        this.users = users;
    }

    @Override
    public void execute(final CommandInput command, final ArrayNode output) {
        int startTimestamp = command.getStartTimestamp();
        int endTimestamp = command.getEndTimestamp();
        String type = command.getType();
        int timestamp = command.getTimestamp();
        String iban = command.getAccount();

        // Find the requested account
        Account account = Tools.findAccountByIBAN(iban, users);
        if (account == null) {
            return;
        }

        // Only proceed if this is a BusinessAccount
        if (account.isBusinessAccount()) {
            BusinessAccount businessAccount = (BusinessAccount) account;

            // Create the primary report node
            ObjectNode report = output.objectNode();
            report.put("command", "businessReport");

            // Create the "output" node, common to both types
            ObjectNode outputNode = report.putObject("output");
            outputNode.put("IBAN", iban);
            outputNode.put("balance", businessAccount.getBalance());
            outputNode.put("currency", businessAccount.getCurrency());
            outputNode.put("spending limit", businessAccount.getSpendingLimit());
            outputNode.put("deposit limit", businessAccount.getDepositLimit());

            // Generate the specific type of report
            if ("transaction".equalsIgnoreCase(type)) {
                generateTransactionReport(businessAccount,
                        startTimestamp, endTimestamp, outputNode);
            } else if ("commerciant".equalsIgnoreCase(type)) {
                generateCommerciantReport(businessAccount,
                        startTimestamp, endTimestamp, outputNode);
            }

            // Add the timestamp
            report.put("timestamp", timestamp);

            // Add the full report to the output array
            output.add(report);
        }
    }

    private void generateTransactionReport(final BusinessAccount businessAccount,
                                           final int startTimestamp,
                                           final int endTimestamp,
                                           final ObjectNode outputNode) {
        outputNode.put("statistics type", "transaction");

        // Managers array
        ArrayNode managersNode = outputNode.putArray("managers");
        for (String email : businessAccount.getManagersEmails()) {
            ObjectNode managerNode = managersNode.addObject();
            String username = Tools.extractUsernameFromEmail(email);

            double spent = Tools.calculateSpentBetween(
                    businessAccount.getAssociateTransactions(),
                    email,
                    startTimestamp,
                    endTimestamp
            );
            double deposited = Tools.calculateDepositedBetween(
                    businessAccount.getAssociateTransactions(),
                    email,
                    startTimestamp,
                    endTimestamp
            );
            managerNode.put("username", username);
            managerNode.put("spent", spent);
            managerNode.put("deposited", deposited);
        }

        // Employees array
        ArrayNode employeesNode = outputNode.putArray("employees");
        for (String email : businessAccount.getEmployeesEmails()) {
            ObjectNode employeeNode = employeesNode.addObject();
            String username = Tools.extractUsernameFromEmail(email);

            double spent = Tools.calculateSpentBetween(
                    businessAccount.getAssociateTransactions(),
                    email,
                    startTimestamp,
                    endTimestamp
            );
            double deposited = Tools.calculateDepositedBetween(
                    businessAccount.getAssociateTransactions(),
                    email,
                    startTimestamp,
                    endTimestamp
            );
            employeeNode.put("username", username);
            employeeNode.put("spent", spent);
            employeeNode.put("deposited", deposited);
        }

        // Calculate total spent and total deposited
        double totalSpent = businessAccount.getManagersEmails().stream()
                .mapToDouble(email -> Tools.calculateSpentBetween(
                        businessAccount.getAssociateTransactions(),
                        email,
                        startTimestamp,
                        endTimestamp
                ))
                .sum()
                + businessAccount.getEmployeesEmails().stream()
                .mapToDouble(email -> Tools.calculateSpentBetween(
                        businessAccount.getAssociateTransactions(),
                        email,
                        startTimestamp,
                        endTimestamp
                ))
                .sum();

        double totalDeposited = businessAccount.getManagersEmails().stream()
                .mapToDouble(email -> Tools.calculateDepositedBetween(
                        businessAccount.getAssociateTransactions(),
                        email,
                        startTimestamp,
                        endTimestamp
                ))
                .sum()
                + businessAccount.getEmployeesEmails().stream()
                .mapToDouble(email -> Tools.calculateDepositedBetween(
                        businessAccount.getAssociateTransactions(),
                        email,
                        startTimestamp,
                        endTimestamp
                ))
                .sum();

        outputNode.put("total spent", totalSpent);
        outputNode.put("total deposited", totalDeposited);
    }

    private void generateCommerciantReport(final BusinessAccount businessAccount,
                                           final int startTimestamp,
                                           final int endTimestamp,
                                           final ObjectNode outputNode) {
        outputNode.put("statistics type", "commerciant");

        // Create the "commerciants" array
        ArrayNode commerciantsArray = outputNode.putArray("commerciants");

        // Get the owner's email to exclude their transactions
        String ownerEmail = businessAccount.getOwnerEmail();

        // Map of email -> list of TransactionDetail
        Map<String, List<TransactionDetail>> associateMap =
                businessAccount.getAssociateTransactions();

        Map<String, Double> totalPerCommerciant = new HashMap<>();
        Map<String, List<String>> managersPerCommerciant = new HashMap<>();
        Map<String, List<String>> employeesPerCommerciant = new HashMap<>();

        for (Map.Entry<String, List<TransactionDetail>> entry : associateMap.entrySet()) {
            String email = entry.getKey();

            // Exclude transactions made by the owner
            if (email.equals(ownerEmail)) {
                continue;
            }

            List<TransactionDetail> transactionDetails = entry.getValue();

            for (TransactionDetail detail : transactionDetails) {
                if (!"spend".equalsIgnoreCase(detail.getType())) {
                    continue;
                }

                String commName = detail.getCommerciantName();
                if (commName == null || commName.trim().isEmpty()) {
                    continue;
                }

                int ts = detail.getTimestamp();
                if (ts < startTimestamp || ts > endTimestamp) {
                    continue;
                }

                totalPerCommerciant.put(commName,
                        totalPerCommerciant.getOrDefault(commName, 0.0)
                                + detail.getAmount());

                String userName = Tools.extractUsernameFromEmail(email);
                if (userName.isEmpty()) {
                    continue;
                }

                if (businessAccount.isManager(email)) {
                    managersPerCommerciant.putIfAbsent(commName, new ArrayList<>());
                    managersPerCommerciant.get(commName).add(userName);
                } else if (businessAccount.isEmployee(email)) {
                    employeesPerCommerciant.putIfAbsent(commName, new ArrayList<>());
                    employeesPerCommerciant.get(commName).add(userName);
                }
            }
        }

        List<String> sortedCommerciantNames = new ArrayList<>(totalPerCommerciant.keySet());
        sortedCommerciantNames.sort(String::compareToIgnoreCase);

        for (String commName : sortedCommerciantNames) {
            double totalReceived = totalPerCommerciant.get(commName);

            List<String> mgrNames =
                    managersPerCommerciant.getOrDefault(commName, new ArrayList<>());
            mgrNames.sort(String::compareToIgnoreCase);

            List<String> empNames =
                    employeesPerCommerciant.getOrDefault(commName, new ArrayList<>());
            empNames.sort(String::compareToIgnoreCase);

            ObjectNode commNode = commerciantsArray.addObject();
            commNode.put("commerciant", commName);
            commNode.put("total received", totalReceived);

            ArrayNode managersNode = commNode.putArray("managers");
            for (String mgrName : mgrNames) {
                managersNode.add(mgrName);
            }

            ArrayNode employeesNode = commNode.putArray("employees");
            for (String empName : empNames) {
                employeesNode.add(empName);
            }
        }
    }
}
