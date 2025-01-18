package org.poo.main.bank;

import org.poo.main.Command;
import org.poo.main.Commerciant.Commerciant;
import org.poo.main.PrintUsersCommand;
import org.poo.main.accounts.BusinessAccount;
import org.poo.main.split.SplitPayment;
import org.poo.main.cards.*;
import org.poo.main.accounts.Account;
import org.poo.main.accounts.AccountFactory;
import org.poo.main.accounts.ClassicAccount;
import org.poo.main.accounts.SavingsAccount;
import org.poo.main.exchange_rate.ExchangeRate;
import org.poo.main.tools.Tools;
import org.poo.main.transactions.*;
import org.poo.main.user.User;
import org.poo.utils.Utils;
import org.poo.main.TransactionDetail;
import org.poo.main.split.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.fileio.CommandInput;

import java.util.*;

public class BankSystem {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final List<User> users = new ArrayList<>();
    private final List<ExchangeRate> exchangeRates = new ArrayList<>();
    private final List<Commerciant> commerciants = new ArrayList<>();
    private final TransactionService transactionService;

    private final Command printUsersCommand;

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
        this.printUsersCommand = new PrintUsersCommand(objectMapper, users);
    }

    public void processCommands(final CommandInput[] commands, final ArrayNode output) {
        for (CommandInput command : commands) {
            switch (command.getCommand()) {
                case "printUsers" -> printUsersCommand.execute(command, output);
                case "addAccount" -> addAccount(command);
                case "createCard" -> createCard(command);
                case "createOneTimeCard" -> createOneTimeCard(command);
                case "addFunds" -> addFunds(command);
                case "deleteAccount" -> deleteAccount(command, output);
                case "deleteCard" -> deleteCard(command);
                case "payOnline" -> payOnline(command, output);
                case "sendMoney" -> sendMoney(command, output);
                case "setAlias" -> setAlias(command);
                case "printTransactions" -> printTransactions(command, output);
                case "setMinimumBalance" -> setMinimumBalance(command);
                case "checkCardStatus" -> checkCardStatus(command, output);
                case "splitPayment" -> splitPayment(command);
                case "acceptSplitPayment" -> acceptSplitPayment(command, output);
                case "rejectSplitPayment" -> rejectSplitPayment(command, output);
                case "report" -> report(command, output);
                case "spendingsReport" -> spendingsReport(command, output);
                case "addInterest" -> addInterest(command, output);
                case "changeInterestRate" -> changeInterestRate(command, output);
                case "withdrawSavings" -> withdrawSavings(command, output);
                case "upgradePlan" -> upgradePlan(command, output);
                case "cashWithdrawal" -> cashWithdrawal(command, output);
                case "addNewBusinessAssociate" -> addNewBusinessAssociate(command);
                case "changeSpendingLimit" -> changeSpendingLimit(command, output);
                case "changeDepositLimit" -> changeDepositLimit(command, output);
                case "businessReport" -> businessReport(command, output);
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
        String iban = Utils.generateIBAN();

        Account account = null;

        try {
            switch (accountType) {
                case "classic":
                    account = AccountFactory.createAccount(
                            AccountFactory.AccountType.CLASSIC,
                            currency,
                            command.getEmail(),
                            iban,
                            null);
                    break;

                case "savings":
                    double interestRate = command.getInterestRate();
                    account = AccountFactory.createAccount(
                            AccountFactory.AccountType.SAVINGS,
                            currency,
                            command.getEmail(),
                            iban,
                            interestRate);
                    break;

                case "business":
                    account = AccountFactory.createAccount(
                            AccountFactory.AccountType.BUSINESS,
                            currency,
                            command.getEmail(),
                            iban,
                            null);

                    double initialLimitInRON = account.getSpendingLimit();
                    double exchangeRate = ExchangeRate.getExchangeRate("RON", currency, exchangeRates);
                    double convertedLimit = initialLimitInRON * exchangeRate;

                    account.setSpendingLimit(convertedLimit, command.getEmail());
                    account.setDepositLimit(convertedLimit, command.getEmail());
                    break;

                default:
                    throw new IllegalArgumentException("Unsupported account type: " + accountType);
            }
        } catch (IllegalArgumentException e) {
            System.err.println("Failed to create account: " + e.getMessage());
            return;
        }

        // Add the account to the user's list of accounts
        user.addAccount(account);

        // Add the transaction to the user's transaction list
        if (account.isBusinessAccount() == false) {
            transactionService.addAccountTransaction(timestamp, account, user);
        }
    }

    private void createCard(final CommandInput command) {
        User user = Tools.findUserByEmail(command.getEmail(), users);

        if (user == null) {
            return;
        }

        Account account = Tools.findAccountByIBAN(command.getAccount(), users);

        if (account != null) {
            String iban = account.getIban();

            // Generate a unique card number
            String cardNumber = org.poo.utils.Utils.generateCardNumber();

            // Use the factory to create a standard card
            Card card = CardFactory.createCard(CardFactory.CardType.STANDARD, user, account, cardNumber);

            // Add the card to the user's account
            account.addCard(card);

            Tools.addCardToAllInstances(card, iban, users);

            // Add the transaction to the user's transaction list
            transactionService.addCardTransaction(command.getTimestamp(), card, account, user);
        }
    }

    private void createOneTimeCard(final CommandInput command) {
        User user = Tools.findUserByEmail(command.getEmail(), users);

        if (user == null) {
            return;
        }

        Account account = Tools.findAccountByIBAN(command.getAccount(), users);

        if (account != null) {
            String iban = account.getIban();

            // Generate a unique card number
            String cardNumber = org.poo.utils.Utils.generateCardNumber();

            // Use the factory to create a one-time card
            Card oneTimeCard = CardFactory.createCard(CardFactory.CardType.ONE_TIME, user, account, cardNumber);

            // Add the one-time card to the user's account
            account.addCard(oneTimeCard);

            Tools.addCardToAllInstances(oneTimeCard, iban, users);

            // Add the transaction to the user's transaction list
            transactionService.addCardTransaction(command.getTimestamp(), oneTimeCard, account, user);
        }
    }

    private void addFunds(final CommandInput command) {
        Account account = Tools.findAccountByIBAN(command.getAccount(), users);
        String email = command.getEmail();

        double amount = command.getAmount();

        if (account == null) {
            return;
        }

        if (account.isBusinessAccount()) {
            if (!account.isAssociate(email)) {
                return;
            }

            if (account.isEmployee(email)) {
                if (amount > account.getDepositLimit()) {
                    System.out.println("business errror: vrea sa depoziteze mai mult decat are voie " + amount);
                    return;
                }
            }
        }

        account.deposit(amount);
        account.addDeposit(email, amount, command.getTimestamp());
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

        // only owner can delete the account
        if (!foundAccount.getOwnerEmail().equals(user.getEmail())) {
            System.out.println("You are not authorized to make this transaction.");
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
        String email = command.getEmail();

        // Iterate through all users to find the card to delete
        for (User user : users) {
            for (Account account : user.getAccounts()) {
                for (Card card : account.getCards()) {
                    if (card.getCardNumber().equals(cardNumber)) {
                        User cardOwner = card.getCardOwner();
                        // daca cel ce vrea sa stearga cardul e angajat, poate sterge doar cardurile facute de el
                        if (account.isEmployee(email)) {
                            if (!email.equals(cardOwner.getEmail())) {
                                System.out.println("You are not authorized to delete this card.");
                                return;
                            }
                        }
                    }

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

    private void payOnline(final CommandInput command, final ArrayNode output) {
        String email = command.getEmail();
        String cardNumber = command.getCardNumber();
        double amount = command.getAmount();
        if (amount <= 0)
            return;
        String currency = command.getCurrency();
        int timestamp = command.getTimestamp();
        String description = command.getDescription();
        String commerciantName = command.getCommerciant();

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

        // Find the commercant based on the name provided
        Commerciant commerciant = Tools.findCommerciantByName(commerciantName, commerciants);
        if (commerciant == null) {
            return;
        }

        // Perform the payment if the card is valid and not frozen
        performPayment(user, card, account, amount, currency, commerciant, timestamp, output, email, command.getCurrency());
    }

    private void performPayment(final User user, final Card card, final Account account,
                                final double amount, final String currency,
                                final Commerciant commerciant, final int timestamp,
                                final ArrayNode output, String email, String commandCurrency) {
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

            if (account.isBusinessAccount()) {
                if (account.isEmployee(email)) {
                    if (amount > account.getSpendingLimit()) {
                        System.out.println("esti doar angajat nu poti plati " + amount + "  .Limita pt tine de de " + account.getSpendingLimit());
                        return;
                    }
                }
            }

            account.spend(finalAmount);
            if (commerciant.getCashbackStrategy().equals("spendingThreshold")) {
                account.addToTotalSpendingThreshold(finalAmount);
            }
            double commission = calculateComision(user, finalAmount, currency, exchangeRates);

            // Substract the calculated commision from the account
            account.spend((commission));

//            // Adaug tranzactia in mapul userului cu comercianti ca sa stiu nr de tranzactii si totalspend
//            user.addTransactionToCommerciant(commerciant, amount, currency, account.getCurrency(), exchangeRates);

            // Add the transaction to the user's transaction list
            transactionService.addOnlinePaymentTransaction(timestamp,
                    card, finalAmount, commerciant.getName(), user, account.getIban());

            // strict pr raport la business account
            account.addSpending(email, finalAmount, timestamp, commerciant.getName());
            account.displayAssociateTransactions();

            String accountCurrency = account.getCurrency(); // Moneda contului

            if (account.isApplyingCashback(commerciant, accountCurrency, exchangeRates) != null) {

                // Calculăm cashback-ul în moneda contului
                double cashback = account.applyCashbackForTransaction(commerciant, amount,
                        account.isApplyingCashback(commerciant, accountCurrency, exchangeRates),
                        accountCurrency, commandCurrency, exchangeRates, user);

                account.deposit(cashback);  // Depunem cashback-ul în cont
            }


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

    private void sendMoney(final CommandInput command, final ArrayNode output) {
        // Get sender and receiver account info
        String senderIBAN = command.getAccount();
        String receiverIBAN = command.getReceiver();
        double amount = command.getAmount();
        String description = command.getDescription();
        int timestamp = command.getTimestamp();

        boolean isMerchant = Tools.isCommerciantIban(receiverIBAN, commerciants);

        Account senderAccount = Tools.findAccountByIBAN(senderIBAN, users);
        Account receiverAccount = Tools.findAccountByIBAN(receiverIBAN, users);

        if (isMerchant == false) {
            if (senderAccount == null) {
                // try with alias
                senderAccount = Tools.findAccountByAlias(senderIBAN, users);
                if (senderAccount == null) {
                    sendMoneyError("User not found", timestamp, output);
                    return;
                }
            }
            if (receiverAccount == null) {
                // try with alias
                receiverAccount = Tools.findAccountByAlias(receiverIBAN, users);
                if (receiverAccount == null) {
                    sendMoneyError("User not found", timestamp, output);
                return;
                }
            }
        }

        // Get sender user details
        User senderUser = Tools.findUserByEmail(command.getEmail(), users);

        // Calculate commission in sender's currency
        double commission = calculateComision(senderUser, amount, senderAccount.getCurrency(), exchangeRates);
        if (isMerchant == true) {
            Commerciant commerciant = Tools.findCommerciantByIBAN(receiverIBAN, commerciants);
            senderAccount.spend(amount + commission); // Include commission
            if (commerciant.getCashbackStrategy().equals("spendingThreshold")) {
                senderAccount.addToTotalSpendingThreshold(amount);
            }
            if (senderAccount.isApplyingCashback(commerciant, senderAccount.getCurrency(), exchangeRates) != null) {

                // Calculăm cashback-ul în moneda contului
                double cashback = senderAccount.applyCashbackForTransaction(commerciant, amount,
                        senderAccount.isApplyingCashback(commerciant, senderAccount.getCurrency(), exchangeRates),
                        senderAccount.getCurrency(), senderAccount.getCurrency(), exchangeRates, senderUser);

                senderAccount.deposit(cashback);  // Depunem cashback-ul în cont
                // trebuie facuta si tranzactie si adaugata in lista userului
                senderAccount.addSpending(senderUser.getEmail(), amount, timestamp, commerciant.getName());
            }

            transactionService.addSendMoneyToCommerciantTransaction(timestamp, senderAccount,
                    commerciant.getAccount(), amount, senderAccount.getCurrency(), description);

            return;
        }

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

        String senderEmail = senderUser.getEmail();

        if (senderAccount.isBusinessAccount()) {
            if (senderAccount.isEmployee(senderEmail)) {
                if (totalAmount > senderAccount.getSpendingLimit()) {
                    return;
                }
            }
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


        senderAccount.addSpending(senderEmail, amount, timestamp, null);
    }

    private void sendMoneyError(final String description, final int timestamp, final ArrayNode output) {
        ObjectNode errorNode = objectMapper.createObjectNode();
        errorNode.put("command", "sendMoney");

        ObjectNode errorOutput = objectMapper.createObjectNode();
        errorOutput.put("timestamp", timestamp);
        errorOutput.put("description", description);

        errorNode.set("output", errorOutput);
        errorNode.put("timestamp", timestamp);

        output.add(errorNode);
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

        User user = Tools.findUserByAccount(iban, users);
        if (user == null) {
            return;
        }

        Account account = Tools.findAccountByIBAN(iban, users);

        // Only owner can adjust minimum balance
        if (account != null && account.getOwnerEmail().equals(user.getEmail())) {
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

    private void acceptSplitPayment(final CommandInput command, final ArrayNode output) {
        String email = command.getEmail();
        int timestamp = command.getTimestamp();

        User user = Tools.findUserByEmail(email, users);
        if (user == null) {
            ObjectNode errorNode = output.addObject();

            // Setează valorile pentru nodul de nivel superior
            errorNode.put("command", "acceptSplitPayment");
            errorNode.put("timestamp", timestamp);

            // Creează un nod "output" în interiorul lui errorNode
            ObjectNode innerOutput = errorNode.putObject("output");
            innerOutput.put("description", "User not found");
            innerOutput.put("timestamp", timestamp);

            return;
        }

        // Retrieve the oldest unaccepted split payment transaction
        Map.Entry<SplitPayment, String> paymentEntry = user.getOldestUnacceptedTransaction();
        if (paymentEntry == null) {
            return;
        }

        String userIban = paymentEntry.getValue();
        SplitPayment splitPayment = paymentEntry.getKey();

        // Mark the user's acceptance for the split payment
        splitPayment.setStatus(userIban, true);

        // Check if all users have accepted
        if (splitPayment.allAccepted()) {
            String insufficientFundsIBAN = Tools.verifyAmounts(splitPayment, exchangeRates, users);

            if (insufficientFundsIBAN != null) {
                handleErrorSplitPayment(splitPayment, insufficientFundsIBAN);
            } else {
                processSplitPayment(splitPayment);
            }
        }
    }

    private void processSplitPayment(final SplitPayment splitPayment) {
        // Create and add success transactions for all involved users
        for (String iban : splitPayment.getAccounts()) {
            User involvedUser = Tools.findUserByAccount(iban, users);
            if (involvedUser != null) {
                transactionService.addSuccessSplitTransaction(
                        splitPayment.getTimestamp(),
                        splitPayment.getAmounts().stream().mapToDouble(Double::doubleValue).sum(),
                        splitPayment.getAmounts(),
                        splitPayment.getCurrency(),
                        splitPayment.getAccounts(),
                        splitPayment.getSplitPaymentType(),
                        involvedUser
                );
            }
        }

        // Deduct the amounts from each account
        for (int i = 0; i < splitPayment.getAccounts().size(); i++) {
            String iban = splitPayment.getAccounts().get(i);
            Account account = Tools.findAccountByIBAN(iban, users);
            double amount = splitPayment.getAmounts().get(i);
            String currency = splitPayment.getCurrency();
            double convertedAmount = Tools.calculateFinalAmount(account, amount, exchangeRates, currency);
            account.spend(convertedAmount);
        }

        // Remove the completed split payment from all users
        Tools.removeSplitPaymentFromUsers(splitPayment, users);
    }

    private void handleErrorSplitPayment(final SplitPayment splitPayment, final String insufficientFundsIBAN) {
        // Create and add error transactions for all involved users
        for (String iban : splitPayment.getAccounts()) {
            User involvedUser = Tools.findUserByAccount(iban, users);
            if (involvedUser != null) {
                transactionService.addSplitErrorTransaction(
                        splitPayment.getTimestamp(),
                        splitPayment.getAmounts().stream().mapToDouble(Double::doubleValue).sum(),
                        splitPayment.getAmounts(),
                        splitPayment.getCurrency(),
                        insufficientFundsIBAN,
                        splitPayment.getAccounts(),
                        splitPayment.getSplitPaymentType(),
                        involvedUser
                );
            }
        }

        // Remove the failed split payment from all users
        Tools.removeSplitPaymentFromUsers(splitPayment, users);
    }

    private void rejectSplitPayment(final CommandInput command, final ArrayNode output) {
        String email = command.getEmail();
        int timestamp = command.getTimestamp();

        User user = Tools.findUserByEmail(email, users);
        if (user == null) {
            ObjectNode errorNode = output.addObject();

            // Setează valorile pentru nodul de nivel superior
            errorNode.put("command", "rejectSplitPayment");
            errorNode.put("timestamp", timestamp);

            // Creează un nod "output" în interiorul lui errorNode
            ObjectNode innerOutput = errorNode.putObject("output");
            innerOutput.put("description", "User not found");
            innerOutput.put("timestamp", timestamp);

            return;
        }

        // Retrieve the oldest pending split payment
        SplitPayment splitPayment = user.getOldestPendingTransaction();

        if (splitPayment == null) {
            return;
        }

        for (String iban : splitPayment.getAccounts()) {
            User involvedUser = Tools.findUserByAccount(iban, users);
            if (involvedUser != null) {
                transactionService.addSplitRejectTransaction(
                        splitPayment.getTimestamp(),
                        splitPayment.getAmounts().stream().mapToDouble(Double::doubleValue).sum(),
                        splitPayment.getAmounts(),
                        splitPayment.getCurrency(),
                        splitPayment.getAccounts(),
                        splitPayment.getSplitPaymentType(),
                        involvedUser
                );
            }
        }


        Tools.removeSplitPaymentFromUsers(splitPayment, users);

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

        User user = Tools.findUserByAccount(iban, users);

        if (user == null) {
            return;
        }


        // Check if the account is a savings account
        if (account.isSavingsAccount()) {
            SavingsAccount savingsAccount = (SavingsAccount) account;
            double interestRate = savingsAccount.getInterestRate();
            double balance = savingsAccount.getBalance();
            savingsAccount.deposit(interestRate * balance);

            transactionService.addInterestTransaction(timestamp, interestRate * balance,
                    savingsAccount.getCurrency(), user);

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

        Account savingsAccount = Tools.findAccountByIBAN(savingsIban, users);
        User user = Tools.findUserByAccount(savingsIban, users);

        if (savingsAccount == null || user == null) {
            transactionService.addErrorTransaction(timestamp, "Account not found", user);
            return;
        }

        if (!(savingsAccount instanceof SavingsAccount)) {
            transactionService.addErrorTransaction(timestamp, "Account is not of type savings", user);
            return;
        }

        SavingsAccount savings = (SavingsAccount) savingsAccount;

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

        transactionService.addSavingsWithdrawalTransaction(timestamp, amount, savings.getIban(),
                classicAccount.getIban(), user);
        transactionService.addSavingsWithdrawalTransaction(timestamp, amount, savings.getIban(),
                classicAccount.getIban(), user);
        // am adaugat de doua ori si pt savings account si pt classic account ale userului
    }

    public void upgradePlan(final CommandInput command, final ArrayNode output) {
        String accountIBAN = command.getAccount();
        String newPlan = command.getNewPlanType();
        int timestamp = command.getTimestamp();

        // Find the account and user
        Account account = Tools.findAccountByIBAN(accountIBAN, users);
        if (account == null) {
            // Creează un nod JSON pentru eroare și îl adaugă în array-ul output
            ObjectNode errorNode = output.addObject();

            // Setează valorile pentru nodul de nivel superior
            errorNode.put("command", "upgradePlan");
            errorNode.put("timestamp", timestamp);

            // Creează un nod "output" în interiorul lui errorNode
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
                    upgradeFeeInRON = 100;
                } else if (newPlan.equals("gold")) {
                    upgradeFeeInRON = 350;
                } else {
                    return;
                }
            }
            case "silver" -> {
                if (newPlan.equals("gold")) {
                    upgradeFeeInRON = 250;
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
            double exchangeRate = ExchangeRate.getExchangeRate(account.getCurrency(), "RON", exchangeRates);
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

    public static double calculateComision(final User user, final double amount,
                                           final String currency,
                                           final List<ExchangeRate> exchangeRates) {
        // Get the user plan
        String plan = user.getAccountPlan();
        double fee = 0;

        // Convert the silver threshold into currency of user
        double thresholdSilver = 500;
        if (!currency.equals("RON")) {
            double exchangeRate = ExchangeRate.getExchangeRate("RON", currency, exchangeRates);
            thresholdSilver = Math.round((500.0 * exchangeRate) * 100.0) / 100.0;
        }

        // Calculate commision
        switch (plan) {
            case "standard":
                fee = amount * 0.002;
                break;

            case "student":
            case "gold":
                fee = 0;
                break;

            case "silver":
                if (amount >= thresholdSilver) {
                    fee = amount * 0.001;
                }
                break;

            default:
                // unknown plan
                fee = 0;
        }

        return fee;
    }

    public void cashWithdrawal(final CommandInput command, final ArrayNode output) {
        int timestamp = command.getTimestamp();
        User user = Tools.findUserByEmail(command.getEmail(), users);

        // Handle the "User not found" case
        if (user == null) {
            ObjectNode errorOutput = objectMapper.createObjectNode();
            errorOutput.put("command", "cashWithdrawal");

            ObjectNode errorDetails = objectMapper.createObjectNode();
            errorDetails.put("description", "User not found");
            errorDetails.put("timestamp", timestamp);

            errorOutput.set("output", errorDetails);
            errorOutput.put("timestamp", timestamp);

            output.add(errorOutput);
            return;
        }

        String cardNumber = command.getCardNumber();
        Card card = Tools.findCardByCardNumber(cardNumber, users);

        // Handle the "Card not found" case
        if (card == null) {
            ObjectNode errorOutput = objectMapper.createObjectNode();
            errorOutput.put("command", "cashWithdrawal");

            ObjectNode errorDetails = objectMapper.createObjectNode();
            errorDetails.put("description", "Card not found");
            errorDetails.put("timestamp", timestamp);

            errorOutput.set("output", errorDetails);
            errorOutput.put("timestamp", timestamp);

            output.add(errorOutput);
            return;
        }

        Account account = card.getAccount();

        if (account == null) {
            return;
        }

        double amountInRON = command.getAmount(); // the amount given in command input is in "RON"

        // Calculate and add commission (still in "RON")
        double commission = calculateComision(user, amountInRON, "RON", exchangeRates);
        amountInRON += commission;

        // Convert the amount + commision in user's currency
        double exchangeRate = ExchangeRate.getExchangeRate("RON", account.getCurrency(), exchangeRates);
        double amountInAccountCurrency = amountInRON * exchangeRate;

        // Check if there are sufficient funds to perform withdrawal
        if (account.getBalance() >= amountInAccountCurrency) {
            account.spend(amountInAccountCurrency);
            transactionService.addWithdrawalTransaction(timestamp, user, amountInRON - commission);
        } else {
            transactionService.addInsufficientFundsTransaction(timestamp,
                    "Insufficient funds", user, account.getIban());
        }
    }

    public void addNewBusinessAssociate(final CommandInput command) {
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
        for (Card card : businessAccount.getCards()) {
            Tools.addCardToAllInstances(card, iban, users);
        }

        if (role.equals("manager")) {
            businessAccount.addManagerEmail(newEmail);
        }

        if (role.equals("employee")) {
            businessAccount.addEmployeeEmail(newEmail);
        }
    }

    public void changeSpendingLimit(final CommandInput command, final ArrayNode output) {
        int timestamp = command.getTimestamp();
        ;
        String email = command.getEmail();
        String iban = command.getAccount();
        double newLimit = command.getAmount();

        Account account = Tools.findAccountByIBAN(iban, users);
        if (account == null || !account.getAccountType().equals("business")) {
            return;
        }

        if (!account.getOwnerEmail().equals(email)) {
            String description = "You must be owner in order to change spending limit.";
            // Creează un nod JSON pentru eroare
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

    public void changeDepositLimit(final CommandInput command, final ArrayNode output) {
        int timestamp = command.getTimestamp();
        String email = command.getEmail();
        String iban = command.getAccount();
        double newLimit = command.getAmount();

        Account account = Tools.findAccountByIBAN(iban, users);
        if (account == null || !account.getAccountType().equals("business")) {
            return;
        }

        if (!account.getOwnerEmail().equals(email)) {
            String description = "You must be owner in order to change deposit limit.";
            // Creează un nod JSON pentru eroare
            ObjectNode errorNode = output.addObject();
            errorNode.put("command", "changeSpendingLimit");
            ObjectNode outputNode = errorNode.putObject("output");
            outputNode.put("description", description);
            outputNode.put("timestamp", timestamp);
            errorNode.put("timestamp", timestamp);
            return;
        }

        account.setDepositLimit(newLimit, email);
    }

    public void businessReport(CommandInput command, final ArrayNode output) {
        // Extract fields from CommandInput
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
        if (account instanceof BusinessAccount) {
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
                generateTransactionReport(businessAccount, startTimestamp, endTimestamp, outputNode);
            } else if ("commerciant".equalsIgnoreCase(type)) {
                generateCommerciantReport(businessAccount, startTimestamp, endTimestamp, outputNode);
            }

            // Add the timestamp
            report.put("timestamp", timestamp);

            // Add the full report to the output array
            output.add(report);
        }
    }

    /**
     * Generates the transaction report.
     */
    private void generateTransactionReport(BusinessAccount businessAccount, int startTimestamp, int endTimestamp, ObjectNode outputNode) {
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

    /**
     * Generates the commerciant report.
     */
    private void generateCommerciantReport(BusinessAccount businessAccount, int startTimestamp, int endTimestamp, ObjectNode outputNode) {
        outputNode.put("statistics type", "commerciant");

        // Create the "commerciants" array
        ArrayNode commerciantsArray = outputNode.putArray("commerciants");

        // Get the owner's email to exclude their transactions
        String ownerEmail = businessAccount.getOwnerEmail();

        // Map of email -> list of TransactionDetail
        Map<String, List<TransactionDetail>> associateMap = businessAccount.getAssociateTransactions();

        // Map for storing total per commerciant
        Map<String, Double> totalPerCommerciant = new HashMap<>();
        // Map for storing managers who paid to each commerciant
        Map<String, List<String>> managersPerCommerciant = new HashMap<>();
        // Map for storing employees who paid to each commerciant
        Map<String, List<String>> employeesPerCommerciant = new HashMap<>();

        // Iterate through each email and associated transactions
        for (Map.Entry<String, List<TransactionDetail>> entry : associateMap.entrySet()) {
            String email = entry.getKey();

            // Exclude transactions made by the owner
            if (email.equals(ownerEmail)) {
                continue;
            }

            List<TransactionDetail> transactionDetails = entry.getValue();

            for (TransactionDetail detail : transactionDetails) {
                // Only interested in "spend" transactions
                if (!"spend".equalsIgnoreCase(detail.getType())) {
                    continue;
                }

                // Check if the transaction has a valid commerciant name
                String commName = detail.getCommerciantName();
                if (commName == null || commName.trim().isEmpty()) {
                    continue;
                }

                // Check if the timestamp is within the specified range
                int ts = detail.getTimestamp();
                if (ts < startTimestamp || ts > endTimestamp) {
                    continue;
                }

                // Add the amount to the commerciant's total
                totalPerCommerciant.put(commName, totalPerCommerciant.getOrDefault(commName, 0.0) + detail.getAmount());

                // Extract the full name of the user
                String userName = Tools.extractUsernameFromEmail(email);
                if (userName.isEmpty()) {
                    continue;
                }

                // Add the associate (manager/employee) to the corresponding list
                if (businessAccount.isManager(email)) {
                    managersPerCommerciant.putIfAbsent(commName, new ArrayList<>());
                    managersPerCommerciant.get(commName).add(userName); // Allows duplicates
                } else if (businessAccount.isEmployee(email)) {
                    employeesPerCommerciant.putIfAbsent(commName, new ArrayList<>());
                    employeesPerCommerciant.get(commName).add(userName); // Allows duplicates
                }
            }
        }

        // Sort commerciant names alphabetically
        List<String> sortedCommerciantNames = new ArrayList<>(totalPerCommerciant.keySet());
        sortedCommerciantNames.sort(String::compareToIgnoreCase);

        // Build the JSON object for each commerciant
        for (String commName : sortedCommerciantNames) {
            double totalReceived = totalPerCommerciant.get(commName);

            // Get and sort manager names
            List<String> mgrNames = managersPerCommerciant.getOrDefault(commName, new ArrayList<>());
            Collections.sort(mgrNames, String::compareToIgnoreCase);

            // Get and sort employee names
            List<String> empNames = employeesPerCommerciant.getOrDefault(commName, new ArrayList<>());
            Collections.sort(empNames, String::compareToIgnoreCase);

            // Create the JSON node for this commerciant
            ObjectNode commNode = commerciantsArray.addObject();
            commNode.put("commerciant", commName);
            commNode.put("total received", totalReceived);

            // Add the list of managers
            ArrayNode managersNode = commNode.putArray("managers");
            for (String mgrName : mgrNames) {
                managersNode.add(mgrName);
            }

            // Add the list of employees
            ArrayNode employeesNode = commNode.putArray("employees");
            for (String empName : empNames) {
                employeesNode.add(empName);
            }
        }
    }
}




