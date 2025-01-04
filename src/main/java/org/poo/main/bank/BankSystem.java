package org.poo.main.bank;

import org.poo.main.Commerciant;
import org.poo.main.cards.Card;
import org.poo.main.cards.OneTimeCard;
import org.poo.main.accounts.Account;
import org.poo.main.accounts.ClassicAccount;
import org.poo.main.accounts.SavingsAccount;
import org.poo.main.exchange_rate.ExchangeRate;
import org.poo.main.tools.Tools;
import org.poo.main.transactions.*;
import org.poo.main.user.User;
import org.poo.utils.Utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.fileio.CommandInput;

import java.util.ArrayList;
import java.util.List;

public class BankSystem {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final List<User> users = new ArrayList<>();
    private final List<ExchangeRate> exchangeRates = new ArrayList<>();
    private final List<Commerciant> commerciants = new ArrayList<>();
    private final TransactionService transactionService;

    public void addUser(final User user) {
        users.add(user);
    }

    public void addExchangeRate(final ExchangeRate exchangeRate) {
        exchangeRates.add(exchangeRate);
    }

    public void addCommerciant(final Commerciant commerciant) {
        commerciants.add(commerciant);
    }

    public BankSystem() {
        this.transactionService = new TransactionService(users);
    }

    public void processCommands(final CommandInput[] commands, final ArrayNode output) {
        for (CommandInput command : commands) {
            switch (command.getCommand()) {
                case "printUsers" -> printUsers(command, output);
                case "addAccount" -> addAccount(command);
                case "createCard" -> createCard(command);
                case "createOneTimeCard" -> createOneTimeCard(command);
                case "addFunds" -> addFunds(command);
                case "deleteAccount" -> deleteAccount(command, output);
                case "deleteCard" -> deleteCard(command);
                case "payOnline" -> payOnline(command, output);
                case "sendMoney" -> sendMoney(command);
                case "setAlias" -> setAlias(command);
                case "printTransactions" -> printTransactions(command, output);
                case "setMinimumBalance" -> setMinimumBalance(command);
                case "checkCardStatus" -> checkCardStatus(command, output);
                // case "splitPayment" -> splitPayment(command);
                case "report" -> report(command, output);
                case "spendingsReport" -> spendingsReport(command, output);
                case "addInterest" -> addInterest(command, output);
                case "changeInterestRate" -> changeInterestRate(command, output);
                case "withdrawSavings" -> withdrawSavings(command, output);
                case "upgradePlan" -> upgradePlan(command);
                case "cashWithdrawal" -> cashWithdrawal(command, output);
                default -> {
                }
            }
        }
        Utils.resetRandom();
    }

    private void printUsers(final CommandInput command, final ArrayNode output) {
        // Create a node as the command result
        ObjectNode commandResultNode = objectMapper.createObjectNode();
        commandResultNode.put("command", "printUsers");

        // Create an array to hold the users
        ArrayNode usersArray = objectMapper.createArrayNode();

        // Iterate over all users in the bank system to print each user's info
        for (User user : users) {
            ObjectNode userNode = Tools.printUser(user);
            usersArray.add(userNode);
        }

        commandResultNode.set("output", usersArray);
        commandResultNode.put("timestamp", command.getTimestamp());

        // Add the result node to the output
        output.add(commandResultNode);
    }

    private void addAccount(final CommandInput command) {
        User user = Tools.findUserByEmail(command.getEmail(), users);

        if (user == null) {
            return;
        }

        int timestamp = command.getTimestamp();
        String accountType = command.getAccountType();
        String currency = command.getCurrency();

        Account account = null;

        String iban = Utils.generateIBAN();

        // Create the account based on the account type
        if ("classic".equals(accountType)) {
            account = new ClassicAccount(currency, command.getEmail(), iban);
        } else if ("savings".equals(accountType)) {
            double interestRate = command.getInterestRate();
            account = new SavingsAccount(currency, command.getEmail(), interestRate, iban);
        } else if ("business".equals(accountType)) {
            System.out.println("Business accounts not implemented yet.");
            return;
        }

        // Add the account to the user's list of accounts
        user.addAccount(account);

        // Add the transaction to the user's transaction list
        transactionService.addAccountTransaction(timestamp, account, user);
    }

    private void createCard(final CommandInput command) {
        User user = Tools.findUserByEmail(command.getEmail(), users);

        if (user == null) {
            return;
        }

        Account account = Tools.findAccountByIBAN(command.getAccount(), users);

        if (account != null) {
            // Generate a new card number and create a new Card object
            String cardNumber = org.poo.utils.Utils.generateCardNumber();
            Card card = new Card(user, account, cardNumber);

            // Add the card to the user's account
            account.addCard(card);

            // Add the transaction to the user's transaction list
            transactionService.addCardTransaction(command.getTimestamp(), card, account, user);
        }
    }

    private void createOneTimeCard(final CommandInput command) {
        User user = Tools.findUserByEmail(command.getEmail(), users);
        int timestamp = command.getTimestamp();

        if (user == null) {
            return;
        }

        Account account = Tools.findAccountByIBAN(command.getAccount(), users);

        if (account != null) {
            // Generate a new card number and create a new OneTimeCard object
            String cardNumber = org.poo.utils.Utils.generateCardNumber();
            OneTimeCard oneTimeCard = new OneTimeCard(user, account, cardNumber);

            // Add the one-time card to the user's account
            account.addCard(oneTimeCard);

            // Add the transaction to the user's transaction list
            transactionService.addCardTransaction(timestamp, oneTimeCard, account, user);
        }
    }

    private void addFunds(final CommandInput command) {
        Account account = Tools.findAccountByIBAN(command.getAccount(), users);

        if (account != null) {
            account.deposit(command.getAmount());
        }
    }

    private void deleteAccount(final CommandInput command, final ArrayNode output) {
        User user = Tools.findUserByEmail(command.getEmail(), users);
        int timestamp = command.getTimestamp();

        if (user == null) {
            return;
        }

        String iban = command.getAccount();
        Account foundAccount = Tools.findAccountByIBAN(iban, users);

        if (foundAccount == null) {
            return;
        }

        // Check if the account balance is zero
        if (foundAccount.getBalance() == 0) {
            // Destroy all associated cards
            List<Card> associatedCards = foundAccount.getCards();
            for (Card card : associatedCards) {
                String cardNumber = card.getCardNumber();
                String email = user.getEmail();

                transactionService.addDeletedCardTransaction(timestamp, foundAccount, card, user);
            }

            // Remove all cards and the account
            foundAccount.clearCards();
            user.removeAccount(foundAccount);

            // Create success response
            ObjectNode commandResultNode = objectMapper.createObjectNode();
            commandResultNode.put("command", "deleteAccount");

            ObjectNode outputNode = objectMapper.createObjectNode();
            outputNode.put("success", "Account deleted");
            outputNode.put("timestamp", command.getTimestamp());

            commandResultNode.set("output", outputNode);
            commandResultNode.put("timestamp", command.getTimestamp());

            output.add(commandResultNode);
        } else {
            // Handle error if balance is not zero
            deleteAccountError(command, output);

            // Add the error transaction to the user's transaction list
            transactionService.addDeleteAccountErrorTransaction(timestamp, user);
        }
    }

    private void deleteAccountError(final CommandInput command, final ArrayNode output) {
        ObjectNode errorNode = objectMapper.createObjectNode();
        errorNode.put("command", "deleteAccount");

        ObjectNode errorOutput = objectMapper.createObjectNode();
        errorOutput.put("error",
                "Account couldn't be deleted - see org.poo.transactions for details");
        errorOutput.put("timestamp", command.getTimestamp());

        errorNode.set("output", errorOutput);
        errorNode.put("timestamp", command.getTimestamp());

        output.add(errorNode);
    }

    private void deleteCard(final CommandInput command) {
        String cardNumber = command.getCardNumber();
        int timestamp = command.getTimestamp();

        // Iterate through all users to find the card to delete
        for (User user : users) {
            for (Account account : user.getAccounts()) {
                for (Card card : account.getCards()) {
                    if (card.getCardNumber().equals(cardNumber)) {
                        // Add the transaction to the user's transaction list
                        transactionService.addDeletedCardTransaction(timestamp, account,
                                card, user);
                        // Remove the card from the account
                        account.removeCard(card);
                        return;
                    }
                }
            }
        }
    }

    private void payOnline(final CommandInput command, final ArrayNode output) {
        String email = command.getEmail();
        String cardNumber = command.getCardNumber();
        double amount = command.getAmount();
        String currency = command.getCurrency();
        int timestamp = command.getTimestamp();
        String description = command.getDescription();
        String commerciant = command.getCommerciant();

        User user = Tools.findUserByEmail(email, users);

        if (user == null) {
            return;
        }

        Card card = null;
        Account account = null;

        // Search for the card in the user's accounts
        for (Account acc : user.getAccounts()) {
            for (Card c : acc.getCards()) {
                if (c.getCardNumber().equals(cardNumber)) {
                    card = c;
                    account = acc;
                    break;
                }
            }
            if (card != null) {
                break;
            }
        }

        if (card == null || account == null) {
            payOnlineError("Card not found", timestamp, output);
            return;
        }

        // Check if the card is frozen
        if (card.getStatus().equals("frozen")) {
            // Add the transaction to the user's transaction list
            transactionService.addCardFrozenTransaction(timestamp, user, account.getIban());
            return;
        }

        // Perform the payment if the card is valid and not frozen
        performPayment(user, card, account, amount, currency, commerciant, timestamp, output);
    }

    private void performPayment(final User user, final Card card, final Account account,
                                final double amount, final String currency,
                                final String commerciant, final int timestamp,
                                final ArrayNode output) {
        // Convert the amount to the correct currency if necessary
        double finalAmount = Tools.calculateFinalAmount(account, amount, exchangeRates, currency);

        double balance = account.getBalance();

        // Check if the account has enough balance for the payment
        if (balance >= finalAmount) {
            // Check if the balance is below the minimum allowed
            if (balance <= account.getMinBalance()) {
                // Add the warning transaction to the user's transaction list
                transactionService.addWarningTransaction(timestamp, user, account.getIban());
                // Freeze the card due to low balance
                card.freezeCard();
                return;
            }

            account.spend(finalAmount);
            // Add the transaction to the user's transaction list
            transactionService.addOnlinePaymentTransaction(timestamp,
                    card, finalAmount, commerciant, user, account.getIban());

            if (card.isOneTimeCard()) {
                handleOneTimeCard(user, card, account, timestamp);
            }

        } else {
            // Add the transaction to the user's transaction list
            transactionService.addInsufficientFundsTransaction(timestamp,
                    "Insufficient funds", user, account.getIban());
        }
    }

    private void handleOneTimeCard(final User user, final Card card,
                                   final Account account, final int timestamp) {

        OneTimeCard oneTimeCard = (OneTimeCard) card;

        // Mark the one-time card as used if it hasn't been used yet
        if (!oneTimeCard.isUsed()) {
            oneTimeCard.markAsUsed();
        }

        // Now the card being used, remove it and create a new one
        if (oneTimeCard.isUsed() == true) {
            account.removeCard(oneTimeCard);
            // Add the transaction to the user's transaction list
            transactionService.addDeletedCardTransaction(timestamp, account, card, user);

            // Creating a new card after payment
            String newCardNumber = Utils.generateCardNumber();
            OneTimeCard newOneTimeCard = new OneTimeCard(user, account, newCardNumber);

            // Adding the new card in account
            account.addCard(newOneTimeCard);

            // Add the new card transaction to the user's transaction list
            transactionService.addCardTransaction(timestamp, newOneTimeCard, account, user);
        }
    }

    private void payOnlineError(final String description, final int timestamp,
                                final ArrayNode output) {
        ObjectNode errorNode = objectMapper.createObjectNode();
        errorNode.put("command", "payOnline");

        ObjectNode errorOutput = objectMapper.createObjectNode();
        errorOutput.put("timestamp", timestamp);
        errorOutput.put("description", description);

        errorNode.set("output", errorOutput);

        errorNode.put("timestamp", timestamp);

        output.add(errorNode);
    }

    private void sendMoney(final CommandInput command) {
        // Get sender and receiver account info
        String senderIBAN = command.getAccount();
        String receiverIBAN = command.getReceiver();
        double amount = command.getAmount();
        String description = command.getDescription();
        int timestamp = command.getTimestamp();

        Account senderAccount = Tools.findAccountByIBAN(senderIBAN, users);
        Account receiverAccount = Tools.findAccountByIBAN(receiverIBAN, users);

        // Check if both accounts exist
        if (senderAccount == null || receiverAccount == null) {
            return;
        }

        // Get sender user details
        User senderUser = Tools.findUserByAccount(senderIBAN, users);

        // Calculate commission in sender's currency
        double commission = calculateComision(senderUser, amount, senderAccount.getCurrency(), exchangeRates);

        // Total amount including commission
        double totalAmount = amount + commission;

        // Convert the amount to the receiver's currency if necessary
        double finalAmount = amount;
        double exchangeRate = 1;

        if (!senderAccount.getCurrency().equals(receiverAccount.getCurrency())) {
            exchangeRate = ExchangeRate.getExchangeRate(
                    senderAccount.getCurrency(),
                    receiverAccount.getCurrency(),
                    exchangeRates
            );
            if (exchangeRate == 0) {
                return;
            }
            finalAmount = amount * exchangeRate;
        }

        // Check if the sender has enough funds for the transfer including commission
        if (senderAccount.getBalance() < totalAmount) {
            // Add the transaction to the user's transaction list
            transactionService.addInsufficientFundsTransaction(timestamp,
                    "Insufficient funds", senderUser, senderIBAN);
            return;
        }

        // Perform the transfer
        senderAccount.spend(totalAmount); // Include commission
        receiverAccount.deposit(finalAmount);

        // Add transactions for both the sender and the receiver
        transactionService.addSendMoneyTransaction(timestamp, senderAccount,
                receiverAccount, amount, senderAccount.getCurrency(), description);

        transactionService.addReceivedMoneyTransaction(timestamp, senderAccount,
                receiverAccount, amount, exchangeRate, receiverAccount.getCurrency(), description);
    }


    private void setAlias(final CommandInput command) {
        String email = command.getEmail();
        String alias = command.getAlias();
        String iban = command.getAccount();

        User user = Tools.findUserByEmail(email, users);
        if (user == null) {
            return;
        }

        Account account = Tools.findAccountByIBAN(iban, users);
        if (account == null) {
            return;
        }

        account.setAlias(alias);
    }

    private void printTransactions(final CommandInput command, final ArrayNode output) {
        String email = command.getEmail();
        int timestamp = command.getTimestamp();

        User user = Tools.findUserByEmail(email, users);

        if (user == null) {
            return;
        }

        // Create the result node for the transaction output
        ObjectNode resultNode = objectMapper.createObjectNode();
        resultNode.put("command", "printTransactions");
        resultNode.put("timestamp", timestamp);

        // Get all transactions for the user
        List<Transaction> transactions = new ArrayList<>(user.getTransactions());

        // Get the nodes representing the transactions
        ArrayNode transactionsArray = Tools.getTransactions(transactions);
        resultNode.set("output", transactionsArray);

        // Add the final result node to the output array
        output.add(resultNode);
    }

    private void setMinimumBalance(final CommandInput command) {
        String iban = command.getAccount();
        double limit = command.getAmount();

        Account account = Tools.findAccountByIBAN(iban, users);

        if (account != null) {
            account.setMinBalance(limit);
        }
    }

    private void checkCardStatus(final CommandInput command, final ArrayNode output) {
        String cardNumber = command.getCardNumber();
        int timestamp = command.getTimestamp();

        // Find the card
        Card foundCard = null;
        Account foundAccount = null;
        User foundUser = null;

        for (User user : users) {
            for (Account account : user.getAccounts()) {
                for (Card card : account.getCards()) {
                    if (card.getCardNumber().equals(cardNumber)) {
                        foundCard = card;
                        foundAccount = account;
                        foundUser = user;
                        break;
                    }
                }
            }
        }

        // If no card is found, return an error response
        if (foundCard == null) {
            cardCheckError("checkCardStatus", timestamp, output);
            return;
        }

        // If the account balance is near the minimum threshold,
        // freeze the card and add a warning transaction
        if (foundAccount.getBalance() - foundAccount.getMinBalance() <= 30) {
            String iban = foundAccount.getIban();
            transactionService.addWarningTransaction(timestamp, foundUser, iban);
            foundCard.freezeCard();
        }
    }

    private void cardCheckError(final String command, final int timestamp,
                                final ArrayNode output) {
        ObjectNode resultNode = objectMapper.createObjectNode();
        resultNode.put("command", command);

        ObjectNode outputNode = objectMapper.createObjectNode();
        outputNode.put("description", "Card not found");
        outputNode.put("timestamp", timestamp);
        resultNode.set("output", outputNode);
        resultNode.put("timestamp", timestamp);

        output.add(resultNode);
    }

    private void splitPayment(final CommandInput command) {
        List<String> ibans = command.getAccounts();
        double totalAmount = command.getAmount();
        double splitAmount = totalAmount / ibans.size();
        String currency = command.getCurrency();
        int timestamp = command.getTimestamp();

        boolean canDoSplit = true;
        String cheapIBAN = null;

        // Check all IBANs to see if each account has enough balance for the split payment
        for (String iban : ibans) {
            Account account = Tools.findAccountByIBAN(iban, users);
            double finalSplitAmount =
                    Tools.calculateFinalAmount(account, splitAmount, exchangeRates, currency);

            if (account.getBalance() < finalSplitAmount) {
                canDoSplit = false;
                cheapIBAN = iban;
                // We will store as cheap IBAN the last IBAN found that doesn't have funds
            }
        }

        if (canDoSplit) {
            for (String iban : ibans) {
                Account account = Tools.findAccountByIBAN(iban, users);
                User currUser = Tools.findUserByAccount(iban, users);
                double finalSplitAmount =
                        Tools.calculateFinalAmount(account, splitAmount, exchangeRates, currency);
                account.spend(finalSplitAmount);

                // Add the new card transaction to the user's transaction list
                transactionService.addSuccessSplitTransaction(timestamp,
                        totalAmount, splitAmount, currency, ibans, currUser);
            }
        } else {
            // Create and add an error transaction for each user involved
            for (String iban : ibans) {
                User user = Tools.findUserByAccount(iban, users);
                if (user != null) {
                    transactionService.addSplitErrorTransaction(timestamp,
                            splitAmount, totalAmount, currency, cheapIBAN, ibans, user);
                }
            }
        }
    }

    private void report(final CommandInput command, final ArrayNode output) {
        ObjectNode reportNode = objectMapper.createObjectNode();
        reportNode.put("command", command.getCommand());

        // Create a filter object for filtering the transactions in the report
        TransactionFilter filter = new ReportTransactionFilter();

        // Generate the data for the report using a utility method from Tools
        ObjectNode outputNode = Tools.generateReportData(command, filter,
                false, users, exchangeRates);

        reportNode.set("output", outputNode);
        reportNode.put("timestamp", command.getTimestamp());

        output.add(reportNode);
    }

    private void spendingsReport(final CommandInput command, final ArrayNode output) {
        String iban = command.getAccount();
        int timestamp = command.getTimestamp();

        Account account = Tools.findAccountByIBAN(iban, users);

        // Check if the account exists and if it is a savings account
        if (account != null && account.isSavingsAccount()) {
            ObjectNode errorNode = objectMapper.createObjectNode();
            errorNode.put("command", command.getCommand());

            ObjectNode errorOutput = objectMapper.createObjectNode();
            errorOutput.put("error",
                    "This kind of report is not supported for a saving account");

            errorNode.set("output", errorOutput);
            errorNode.put("timestamp", timestamp);

            output.add(errorNode);
            return;
        }

        // If the account is not a savings account, proceed with generating the report
        ObjectNode reportNode = objectMapper.createObjectNode();
        reportNode.put("command", command.getCommand());

        // Create a specific filter for spendings transactions
        TransactionFilter filter = new SpendingsTransactionFilter();

        // Generate the report data using the filter and provided parameters
        ObjectNode outputNode = Tools.generateReportData(command, filter,
                true, users, exchangeRates);

        reportNode.set("output", outputNode);
        reportNode.put("timestamp", timestamp);

        output.add(reportNode);
    }

    private void addInterest(final CommandInput command, final ArrayNode output) {
        int timestamp = command.getTimestamp();
        String iban = command.getAccount();

        Account account = Tools.findAccountByIBAN(iban, users);

        if (account == null) {
            return;
        }

        // Check if the account is a savings account
        if (account.isSavingsAccount()) {
            SavingsAccount savingsAccount = (SavingsAccount) account;
            double interestRate = savingsAccount.getInterestRate();
            double balance = savingsAccount.getBalance();
            savingsAccount.deposit(interestRate * balance);
        } else {
            interestError("addInterest", timestamp, output);
        }
    }

    private void changeInterestRate(final CommandInput command, final ArrayNode output) {
        int timestamp = command.getTimestamp();
        String iban = command.getAccount();

        Account account = Tools.findAccountByIBAN(iban, users);
        User user = Tools.findUserByAccount(iban, users);

        double interestRate = command.getInterestRate();

        if (account == null || user == null) {
            return;
        }

        // Check if the account is a savings account
        if (account.isSavingsAccount()) {
            SavingsAccount savingsAccount = (SavingsAccount) account;
            savingsAccount.setInterestRate(interestRate);

            transactionService.addInterestRateChangeTransaction(timestamp, interestRate, user);
        } else {
            interestError("changeInterestRate", timestamp, output);
        }
    }

    private void interestError(final String commandName, final int timestamp,
                               final ArrayNode output) {
        ObjectNode result = output.addObject();
        result.put("command", commandName);
        result.put("timestamp", timestamp);

        ObjectNode errorOutput = result.putObject("output");
        errorOutput.put("description", "This is not a savings account");
        errorOutput.put("timestamp", timestamp);
    }

    private void withdrawSavings(final CommandInput command, final ArrayNode output) {
        String savingsIban = command.getAccount();
        double amount = command.getAmount();
        String currency = command.getCurrency();
        int timestamp = command.getTimestamp();

        // Găsește utilizatorul și contul de economii
        Account savingsAccount = Tools.findAccountByIBAN(savingsIban, users);
        User user = Tools.findUserByAccount(savingsIban, users);

        if (savingsAccount == null || user == null) {
            transactionService.addErrorTransaction(timestamp, "Account not found", user);
            return;
        }

        // Verifică dacă contul este de tip savings
        if (!(savingsAccount instanceof SavingsAccount)) {
            transactionService.addErrorTransaction(timestamp, "Account is not of type savings", user);
            return;
        }

        SavingsAccount savings = (SavingsAccount) savingsAccount;

        // Verifică vârsta utilizatorului
        if (user.hasMinAge() == false) {
            transactionService.addErrorTransaction(timestamp, "You don't have the minimum age required.", user);
            return;
        }

        // Găsește primul cont clasic în moneda specificată
        Account classicAccount = null;
        for (Account acc : user.getAccounts()) {
            if (acc instanceof ClassicAccount && acc.getCurrency().equals(currency)) {
                classicAccount = acc;
                break;
            }
        }

        if (classicAccount == null) {
            transactionService.addErrorTransaction(timestamp, "You do not have a classic account.", user);
            return;
        }

        // Verifică fondurile disponibile
        double exchangeRate = ExchangeRate.getExchangeRate(savings.getCurrency(), currency, exchangeRates);
        double requiredAmount = amount / exchangeRate; // conversie în moneda contului de economii

        if (savings.getBalance() < requiredAmount) {
            transactionService.addErrorTransaction(timestamp, "Insufficient funds", user);
            return;
        }

        // Realizează transferul fondurilor
        savings.spend(requiredAmount);
        classicAccount.deposit(amount);
    }

    public void upgradePlan(final CommandInput command) {
        String accountIBAN = command.getAccount();
        String newPlan = command.getNewPlanType();
        int timestamp = command.getTimestamp();

        // Găsește contul și utilizatorul
        Account account = Tools.findAccountByIBAN(accountIBAN, users);
        if (account == null) {
            System.out.println("ERROR: Account not found.");
            return;
        }

        User user = Tools.findUserByAccount(accountIBAN, users);
        if (user == null) {
            System.out.println("ERROR: User not found.");
            return;
        }

        String currentPlan = user.getAccountPlan();
        double upgradeFeeInRON = 0; // Taxa în RON

        // Calcularea taxei de upgrade în RON
        switch (currentPlan) {
            case "standard", "student" -> {
                if (newPlan.equals("silver")) {
                    upgradeFeeInRON = 100;
                } else if (newPlan.equals("gold")) {
                    upgradeFeeInRON = 350;
                } else {
                    System.out.println("ERROR: Invalid upgrade plan.");
                    return;
                }
            }
            case "silver" -> {
                if (newPlan.equals("gold")) {
                    upgradeFeeInRON = 250;
                } else {
                    System.out.println("ERROR: You cannot downgrade your plan.");
                    return;
                }
            }
            case "gold" -> {
                System.out.println("ERROR: The user already has the gold plan.");
                return;
            }
            default -> {
                System.out.println("ERROR: Invalid current plan.");
                return;
            }
        }

        // Convertim taxa în moneda contului specificat
        double upgradeFee = upgradeFeeInRON;

        if (!account.getCurrency().equals("RON")) {
            double exchangeRate = ExchangeRate.getExchangeRate(account.getCurrency(), "RON", exchangeRates);

            // Divizăm la rata de schimb, nu înmulțim
            upgradeFee = Math.round((upgradeFeeInRON / exchangeRate) * 100.0) / 100.0;
        }

        // Verificăm fondurile disponibile în contul dat
        if (account.getBalance() < upgradeFee) {
            System.out.println("ERROR: Insufficient funds.");
            transactionService.addInsufficientFundsTransaction(timestamp,
                    "Insufficient funds for upgrade", user, accountIBAN);
            return;
        }

        // Scădem taxa din contul specificat
        account.spend(upgradeFee);

        // Aplicăm upgrade-ul pentru utilizator și toate conturile sale
        user.setAccountPlan(newPlan);
        for (Account acc : user.getAccounts()) {
            acc.setAccountPlan(newPlan); // Aplică noul plan la toate conturile
        }

        // Adăugăm tranzacția pentru upgrade
        transactionService.addUpgradePlanTransaction(timestamp, user, newPlan, accountIBAN);
    }


    public static double calculateComision(final User user, final double amount,
                                           final String currency,
                                           final List<ExchangeRate> exchangeRates) {
        // Preluăm planul utilizatorului
        String plan = user.getAccountPlan();
        double feeInRON = 0;

        // Convertim suma în RON pentru comparare
        double amountInRON = ExchangeRate.convertToRON(amount, currency, exchangeRates);

        // Calculăm comisionul în RON
        switch (plan) {
            case "standard":
                // 0.2% comision
                feeInRON = amountInRON * 0.002;
                break;

            case "student":
            case "gold":
                // Fără comision
                feeInRON = 0;
                break;

            case "silver":
                // Fără comision pentru tranzacții < 500 RON, altfel 0.1%
                if (amountInRON >= 500) {
                    feeInRON = amountInRON * 0.001;
                }
                break;

            default:
                // Plan necunoscut -> fără comision
                feeInRON = 0;
        }

        // Convertim comisionul înapoi în moneda inițială
        double fee = feeInRON;
        if (!currency.equals("RON")) {
            double conversionRate = ExchangeRate.getExchangeRate("RON", currency, exchangeRates);
            fee = feeInRON / conversionRate;
        }

        // Rotunjim comisionul la 2 zecimale în currency-ul dat
        return fee;
    }


    public void cashWithdrawal(final CommandInput command, final ArrayNode output) {
        int timestamp = command.getTimestamp();
        User user = Tools.findUserByEmail(command.getEmail(), users);

        if (user == null)
            return;

        String cardNumber = command.getCardNumber();
        Card card = Tools.findCardByCardNumber(cardNumber, users);

        // Handle the "Card not found" case directly in the output array
        if (card == null) {
            ObjectNode errorOutput = objectMapper.createObjectNode();
            errorOutput.put("command", "cashWithdrawal");

            ObjectNode errorDetails = objectMapper.createObjectNode();
            errorDetails.put("description", "Card not found");
            errorDetails.put("timestamp", timestamp);

            errorOutput.set("output", errorDetails);
            errorOutput.put("timestamp", timestamp);

            output.add(errorOutput); // Add error directly to the output
            return; // Exit the function
        }

        Account account = card.getAccount();

        if (account == null) {
            return;
        }

        double amountInRON = command.getAmount(); // Suma este dată în RON

        // Calculate and add commission
        double commission = calculateComision(user, amountInRON, "RON", exchangeRates);
        amountInRON += commission;

        double exchangeRate = ExchangeRate.getExchangeRate("RON", account.getCurrency(), exchangeRates);
        double amountInAccountCurrency = amountInRON / exchangeRate;

        // Check if sufficient balance is available
        if (account.getBalance() >= amountInAccountCurrency) {
            account.spend(amountInAccountCurrency);
            transactionService.addWithdrawalTransaction(timestamp, user, amountInRON - commission);
        } else {
            return; // Insufficient balance
        }
    }



}




