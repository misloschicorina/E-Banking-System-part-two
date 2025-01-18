package org.poo.main.tools;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import org.poo.fileio.CommandInput;
import org.poo.main.Commerciant.Commerciant;
import org.poo.main.TransactionDetail;
import org.poo.main.cards.Card;
import org.poo.main.split.SplitPayment;
import org.poo.main.accounts.Account;
import org.poo.main.exchange_rate.ExchangeRate;
import org.poo.main.transactions.Transaction;
import org.poo.main.transactions.TransactionFilter;
import org.poo.main.user.User;

public final class Tools {

    private static final ObjectMapper ObjectMapper = new ObjectMapper();

    private Tools() {
    }

    /**
     * Finds a user by email from a list of users.
     *
     * @param email the email to search for
     * @param users the list of users
     * @return the user with the specified email, or null if not found
     */
    public static User findUserByEmail(final String email, final List<User> users) {
        for (User user : users) {
            if (user.getEmail().equals(email)) {
                return user;
            }
        }
        return null;
    }

    /**
     * Finds an account by IBAN from a list of users.
     *
     * @param iban  the IBAN to search for
     * @param users the list of users
     * @return the account with the specified IBAN, or null if not found
     */
    public static Account findAccountByIBAN(final String iban, final List<User> users) {
        for (User user : users) {
            for (Account account : user.getAccounts()) {
                if (account.getIban().equals(iban)) {
                    return account;
                }
            }
        }
        return null;
    }

    /**
     * Finds a user associated with a given IBAN.
     *
     * @param iban  the IBAN to search for
     * @param users the list of users
     * @return the user associated with the specified IBAN, or null if not found
     */
    public static User findUserByAccount(final String iban, final List<User> users) {
        for (User user : users) {
            for (Account account : user.getAccounts()) {
                if (account.getIban().equals(iban)) {
                    return user;
                }
            }
        }
        return null;
    }

    /**
     * Converts a list of cards into a JSON array.
     *
     * @param cards the list of cards
     * @return a JSON array representing the cards
     */
    public static ArrayNode printCardsForAccount(final List<Card> cards) {
        ArrayNode cardsArray = ObjectMapper.createArrayNode();

        for (Card card : cards) {
            ObjectNode cardNode = ObjectMapper.createObjectNode();
            cardNode.put("cardNumber", card.getCardNumber());
            cardNode.put("status", card.getStatus());
            cardsArray.add(cardNode);
        }

        return cardsArray;
    }

    public static ArrayNode printAccountsForUser(final List<Account> accounts, final String ownerEmail) {
        ArrayNode accountsArray = ObjectMapper.createArrayNode();

        for (Account account : accounts) {
            // Only include the account if the ownerEmail matches the user's email
            if (account.getOwnerEmail().equals(ownerEmail)) {
                ObjectNode accountNode = ObjectMapper.createObjectNode();
                accountNode.put("IBAN", account.getIban());
                accountNode.put("balance", account.getBalance());
                accountNode.put("currency", account.getCurrency());
                accountNode.put("type", account.getAccountType());

                ArrayNode cardsArray = printCardsForAccount(account.getCards());
                accountNode.set("cards", cardsArray);

                accountsArray.add(accountNode);
            }
        }

        return accountsArray;
    }

    /**
     * Converts a user into a JSON object.
     *
     * @param user the user to convert
     * @return a JSON object representing the user
     */
    public static ObjectNode printUser(final User user) {
        ObjectNode userNode = ObjectMapper.createObjectNode();
        userNode.put("firstName", user.getFirstName());
        userNode.put("lastName", user.getLastName());
        userNode.put("email", user.getEmail());

        // Pass the user's email to filter out only the accounts they own
        ArrayNode accountsArray = printAccountsForUser(user.getAccounts(), user.getEmail());
        userNode.set("accounts", accountsArray);

        return userNode;
    }


    /**
     * Retrieves the exchange rate between two currencies.
     *
     * @param from          the source currency
     * @param to            the target currency
     * @param exchangeRates the list of exchange rates
     * @return the exchange rate, or 0 if not found
     */
    public static double getExchangeRate(final String from, final String to,
                                         final List<ExchangeRate> exchangeRates) {
        return ExchangeRate.getExchangeRate(from, to, exchangeRates);
    }

    /**
     * Calculates the final amount after applying the exchange rate.
     *
     * @param account       the account involved
     * @param amount        the initial amount
     * @param exchangeRates the list of exchange rates
     * @param currency      the target currency
     * @return the final amount in the target currency, or 0 if no rate is found
     */
    public static double calculateFinalAmount(final Account account, final double amount,
                                              final List<ExchangeRate> exchangeRates,
                                              final String currency) {
        // Dacă moneda contului este deja aceeași cu moneda splitului, returnăm direct suma
        if (account.getCurrency().equals(currency)) {
            return amount;
        }

        // Obținem rata de schimb pentru conversie
        double rate = getExchangeRate(currency, account.getCurrency(), exchangeRates);

        // Verificăm dacă rata de schimb este validă
        if (rate == 0) {
            throw new IllegalArgumentException("Exchange rate not found for " + currency + " to " + account.getCurrency());
        }

        // Convertim suma în moneda contului
        return amount * rate;
    }

    /**
     * Converts a list of transactions into a JSON array.
     *
     * @param transactions the list of transactions
     * @return a JSON array representing the transactions
     */
    public static ArrayNode getTransactions(final List<Transaction> transactions) {
        // Create the array to hold transaction nodes
        ArrayNode transactionsArray = ObjectMapper.createArrayNode();

        for (Transaction transaction : transactions) {
            ObjectNode transactionNode = ObjectMapper.createObjectNode();

            // Add timestamp at the beginning for all transactions
            transactionNode.put("timestamp", transaction.getTimestamp());

            // Add description field after timestamp
            transactionNode.put("description", transaction.getDescription());

            // Handle split payment transactions
            if (transaction.getDescription().startsWith("Split payment")) {
                // Add currency
                transactionNode.put("currency", transaction.getCurrency());

                // Add amountForUsers as an array
                ArrayNode amountsForUsersArray = ObjectMapper.createArrayNode();
                if (transaction.getAmountForUsers() != null ) {
                    if (transaction.getSplitPaymentType().equals("custom")) {
                        for (Double amount : transaction.getAmountForUsers()) {
                            amountsForUsersArray.add(amount);
                        }
                        transactionNode.set("amountForUsers", amountsForUsersArray);
                    }
                    else { // equal type
                        double amount = transaction.getAmountForUsers().get(0);
                        transactionNode.put("amount", amount);

                    }
                }


                // Add involved accounts as an array
                ArrayNode involvedAccountsArray = ObjectMapper.createArrayNode();
                if (transaction.getInvolvedAccounts() != null) {
                    for (String account : transaction.getInvolvedAccounts()) {
                        involvedAccountsArray.add(account);
                    }
                }
                transactionNode.set("involvedAccounts", involvedAccountsArray);

                // Add split payment type
                transactionNode.put("splitPaymentType", transaction.getSplitPaymentType());

                if (transaction.getError() != null) {
                    transactionNode.put("error", transaction.getError());
                }
            }

            // Handle specific format for cash withdrawals
            else if (transaction.getDescription().startsWith("Cash withdrawal")) {
                // Format the amount as an integer for cash withdrawals
                if (transaction.getAmount() != null) {
                    transactionNode.put("amount", transaction.getAmount().doubleValue());
                }
            }

            // Handle upgrade plan transactions
            else if ("Upgrade plan".equals(transaction.getDescription())) {
                transactionNode.put("accountIBAN", transaction.getAccountIBAN());
                transactionNode.put("newPlanType", transaction.getPlan());
            }

            // Handle interest rate income transactions
            else if ("Interest rate income".equals(transaction.getDescription())) {
                // Add separate fields for amount and currency
                if (transaction.getAmount() != null) {
                    transactionNode.put("amount", transaction.getAmount());
                }
                if (transaction.getCurrency() != null) {
                    transactionNode.put("currency", transaction.getCurrency());
                }
            }

            else if ("Savings withdrawal".equals(transaction.getDescription())) {
                // Format fields exactly as requested:
                if (transaction.getAmount() != null) {
                    transactionNode.put("amount", transaction.getAmount());
                }
                // The classic account IBAN is the "receiverIBAN"
                if (transaction.getReceiverIBAN() != null) {
                    transactionNode.put("classicAccountIBAN", transaction.getReceiverIBAN());
                }
                // The savings account IBAN is the "senderIBAN"
                if (transaction.getSenderIBAN() != null) {
                    transactionNode.put("savingsAccountIBAN", transaction.getSenderIBAN());
                }
            }

            // Default handling for other transactions
            else {
                if ("The card has been destroyed".equals(transaction.getDescription())
                        || "New card created".equals(transaction.getDescription())) {
                    transactionNode.put("account", transaction.getAccountIBAN());
                }
                if (transaction.getCardNumber() != null) {
                    transactionNode.put("card", transaction.getCardNumber());
                }
                if (transaction.getCardHolder() != null) {
                    transactionNode.put("cardHolder", transaction.getCardHolder());
                }
                if (transaction.getAmount() != null) {
                    if ("Card payment".equals(transaction.getDescription())) {
                        transactionNode.put("amount", transaction.getAmount());
                    } else {
                        String formattedAmount =
                                transaction.getAmount() + " " + transaction.getCurrency();
                        transactionNode.put("amount", formattedAmount);
                    }
                }
                if (transaction.getSenderIBAN() != null) {
                    transactionNode.put("senderIBAN", transaction.getSenderIBAN());
                }
                if (transaction.getReceiverIBAN() != null) {
                    transactionNode.put("receiverIBAN", transaction.getReceiverIBAN());
                }
                if (transaction.getTransferType() != null) {
                    transactionNode.put("transferType", transaction.getTransferType());
                }
                if (transaction.getCommerciant() != null) {
                    transactionNode.put("commerciant", transaction.getCommerciant());
                }
            }

            // Add the transaction node to the array
            transactionsArray.add(transactionNode);
        }
        return transactionsArray;
    }

    /**
     * Validates the user and account by checking if the account exists
     * for a given IBAN and returns the corresponding user.
     *
     * @param command the command input containing the IBAN
     * @param users the list of users to search through
     * @return the user associated with the account, or null if no account is found
     */
    private static User validateUserAndAccount(final CommandInput command,
                                               final List<User> users) {
        String iban = command.getAccount();
        Account account = findAccountByIBAN(iban, users);
        if (account == null) {
            return null;
        }
        return findUserByAccount(iban, users);
    }

    /**
     * Generates report data based on the command, filter, and additional parameters.
     *
     * @param command            the command input
     * @param filter             the transaction filter
     * @param includeCommerciants flag to include commerciants in the report
     * @param users              the list of users
     * @param exchangeRates      the list of exchange rates
     * @return an ObjectNode containing the report data
     */
    public static ObjectNode generateReportData(
            final CommandInput command,
            final TransactionFilter filter,
            final boolean includeCommerciants,
            final List<User> users,
            final List<ExchangeRate> exchangeRates) {

        ObjectMapper objectMapper = new ObjectMapper();
        String iban = command.getAccount();

        // Validate the user and account
        User user = validateUserAndAccount(command, users);
        if (user == null) {
            return createErrorNode(command, "Account not found");
        }

        // Filtering the transactions
        List<Transaction> filteredTransactions =
                filterTransactions(user.getTransactions(), command, filter, iban);

        // Calculate totals for commerciants if requested
        Map<String, Double> commerciantsTotals;
        if (includeCommerciants) {
            commerciantsTotals = calculateCommerciantsTotals(filteredTransactions);
        } else {
            commerciantsTotals = null;
        }

        // Generate the report output node with filtered transactions and commerciant totals
        return createReportOutputNode(command, filteredTransactions, commerciantsTotals,
                                                        includeCommerciants, iban, user);
    }

    /**
     * Creates an output node for generating a report based on the provided
     * input, filtered transactions, commerciants' totals, and additional details.
     *
     * @param command the command input containing the parameters for the report
     * @param filteredTransactions the list of filtered transactions to include in the report
     * @param commerciantsTotals a map of commerciants' total transaction amounts
     * @param includeCommerciants flag to indicate if commerciants data should be included
     * @param iban the IBAN for which the report is being created
     * @param user the user associated with the account
     * @return an ObjectNode representing the report output
     */
    private static ObjectNode createReportOutputNode(
            final CommandInput command,
            final List<Transaction> filteredTransactions,
            final Map<String, Double> commerciantsTotals,
            final boolean includeCommerciants,
            final String iban,
            final User user) {

        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode outputNode = objectMapper.createObjectNode();

        // Add general account information
        outputNode.put("IBAN", iban);
        //outputNode.put("balance", findAccountByIBAN(iban, List.of(user)).getBalance());
        double balance = findAccountByIBAN(iban, List.of(user)).getBalance();
        outputNode.put("balance", balance);


        outputNode.put("currency", findAccountByIBAN(iban, List.of(user)).getCurrency());

        // AAdd filtered transactions to the output node
        ArrayNode transactionsArray = getTransactions(filteredTransactions);
        outputNode.set("transactions", transactionsArray);

        // Include commerciants' total transaction amounts if requested
        if (includeCommerciants && commerciantsTotals != null) {
            ArrayNode commerciantsArray = objectMapper.createArrayNode();

            // Sort commerciants alphabetically by name
            commerciantsTotals.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .forEach(entry -> {
                        // Create node for each commerciant
                        ObjectNode commerciantNode = objectMapper.createObjectNode();
                        commerciantNode.put("commerciant", entry.getKey());
                        commerciantNode.put("total", entry.getValue());
                        commerciantsArray.add(commerciantNode);
                    });

            outputNode.set("commerciants", commerciantsArray);
        }

        return outputNode;
    }

    /**
     * Creates an error node in the JSON output.
     *
     * @param command the command input containing the timestamp of the error
     * @param message the error message to include in the output
     * @return an ObjectNode representing the error output
     */
    private static ObjectNode createErrorNode(final CommandInput command, final String message) {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode errorNode = objectMapper.createObjectNode();
        errorNode.put("description", message);
        errorNode.put("timestamp", command.getTimestamp());
        return errorNode;
    }

    /**
     * Filters transactions based on the provided command input parameters and a transaction filter.
     *
     * @param transactions the list of all transactions to filter
     * @param command the command input containing the timestamp range
     * @param filter the filter to apply to the transactions
     * @param iban the IBAN for which the transactions are filtered
     * @return a list of filtered transactions
     */
    private static List<Transaction> filterTransactions(
            final List<Transaction> transactions,
            final CommandInput command,
            final TransactionFilter filter,
            final String iban) {

        // Get the start and end timestamps from the command
        int startTimestamp = command.getStartTimestamp();
        int endTimestamp = command.getEndTimestamp();

        // Create a list to store the filtered transactions
        List<Transaction> filteredTransactions = new ArrayList<>();

        // Iterate through all the transactions
        for (int i = 0; i < transactions.size(); i++) {
            Transaction transaction = transactions.get(i);

            // Check if the transaction is within the timestamp range and passes the filter
            if (transaction.getTimestamp() >= startTimestamp
                    && transaction.getTimestamp() <= endTimestamp
                    && filter.filter(transaction, iban)) {

                Transaction lastTransaction = null;

                // Check if the filtered transactions list is not empty
                if (!filteredTransactions.isEmpty()) {
                    lastTransaction = filteredTransactions.get(filteredTransactions.size() - 1);
                }

                // Skip the transaction if it is a duplicate (same timestamp as the last one)
                if (lastTransaction != null
                        && lastTransaction.getTimestamp() == transaction.getTimestamp()) {
                    continue;
                }

                // Add the transaction to the filtered list
                filteredTransactions.add(transaction);
            }
        }

        return filteredTransactions;
    }

    /**
     * Calculates the total transaction amount for each commerciant based on the
     * list of transactions. The totals are stored in a map with the commerciant name
     * as the key and the total amount as the value.
     *
     * @param transactions the list of transactions to calculate totals from
     * @return a map with commerciants as keys and total amounts as values
     */
    private static Map<String, Double> calculateCommerciantsTotals(
                                    final List<Transaction> transactions) {

        // Create a map to store the total amount for each commerciant
        Map<String, Double> commerciantsTotals = new HashMap<>();

        // Iterate through all the transactions
        for (Transaction transaction : transactions) {
            String commerciant = transaction.getCommerciant();
            if (commerciant != null) {
                // Update the total amount for the commerciant, adding if exists,
                // or initializing if not
                commerciantsTotals.put(commerciant,
                        commerciantsTotals.getOrDefault(commerciant, 0.0)
                                + transaction.getAmount());
            }
        }

        // Return the map containing total amounts for each commerciant
        return commerciantsTotals;
    }

    /**
     * Finds a commerciant by IBAN in the given list of commerciants.
     *
     * @param iban          The IBAN to search for.
     * @param commerciants  The list of commerciants to search in.
     * @return The commerciant if found, null otherwise.
     */
    public static Commerciant findCommerciantByIBAN(final String iban, final List<Commerciant> commerciants) {
        for (Commerciant commerciant : commerciants) {
            if (commerciant.getAccount().equals(iban)) {
                return commerciant;
            }
        }
        return null;
    }

    /**
     * Calculates transaction fee based on the user plan.
     *
     * @param plan         The plan type of the user.
     * @param amount       The amount for which fee needs to be calculated.
     * @param currency     The currency of the amount.
     * @param exchangeRates The list of available exchange rates.
     * @return The calculated fee.
     */
    public static double calculateFee(final String plan, final double amount, final String currency,
                                      final List<ExchangeRate> exchangeRates) {
        double fee = 0;

        // Convert the amount to RON for comparison
        double amountInRON = ExchangeRate.convertToRON(amount, currency, exchangeRates);

        switch (plan) {
            case "standard":
                // Calculate fee directly based on the given amount
                fee = amount * 0.002; // 0.2% fee
                break;

            case "silver":
                // Apply a lower fee only if the equivalent amount in RON >= 500
                if (amountInRON >= 500) {
                    double amountForFee = ExchangeRate.convertToRON(amount, currency, exchangeRates);
                    fee = amountForFee * 0.001; // 0.1% fee
                    fee = ExchangeRate.convertFromRON(fee, currency, exchangeRates); // Convert back to the original currency
                } else {
                    fee = amount * 0.002; // Default fee for silver if amount < 500 RON
                }
                break;

            case "gold":
            case "student":
                fee = 0; // No fee
                break;

            default:
                fee = 0; // Default to no fee
        }

        return fee;
    }

    public static Card findCardByCardNumber(final String cardNumber, final List<User> users) {
        // Iterăm prin toți utilizatorii
        for (User user : users) {
            // Iterăm prin toate conturile utilizatorului
            for (Account account : user.getAccounts()) {
                // Iterăm prin toate cardurile fiecărui cont
                for (Card card : account.getCards()) {
                    if (card.getCardNumber().equals(cardNumber)) {
                        return card; // Returnăm cardul găsit
                    }
                }
            }
        }
        return null; // Cardul nu a fost găsit
    }

    public static Commerciant findCommerciantByName(String name, final List<Commerciant> commerciants) {
        for (Commerciant commerciant : commerciants) {
            if (commerciant.getName().equalsIgnoreCase(name)) {
                return commerciant;
            }
        }
        return null;
    }

    public static String verifyAmounts(SplitPayment splitPayment, List<ExchangeRate> exchangeRates, List<User> users) {
        List<String> accounts = splitPayment.getAccounts();
        List<Double> amounts = splitPayment.getAmounts();
        String currency = splitPayment.getCurrency();

        for (int i = 0; i < accounts.size(); i++) {
            String accountIBAN = accounts.get(i);
            double amount = amounts.get(i);

            if (splitPayment.getAmounts().size() == 3) {
                System.out.println("Accounts in SplitPayment: " + splitPayment.getAccounts());
                System.out.println("Amounts in SplitPayment: " + splitPayment.getAmounts());
            }

            // Obținem contul utilizând IBAN-ul
            Account account = Tools.findAccountByIBAN(accountIBAN, users);

            if (account == null) {
                throw new IllegalArgumentException("Account with IBAN " + accountIBAN + " not found.");
            }

            if (splitPayment.getAmounts().size() == 3) {
                System.out.println("Accounts in SplitPayment: " + splitPayment.getAccounts());
                System.out.println("Amounts in SplitPayment: " + splitPayment.getAmounts());
            }

            // Convertim suma în moneda contului
            double convertedAmount = Tools.calculateFinalAmount(account, amount, exchangeRates, currency);

            // Verificăm dacă balanța contului este suficientă
            if (account.getBalance() < convertedAmount) {
                return accountIBAN; // Returnăm IBAN-ul contului care nu are suficiente fonduri
            }
        }

        return null; // To
    }

    public static void removeSplitPaymentFromUsers(final SplitPayment splitPayment, final List<User> users) {
        for (String iban : splitPayment.getAccounts()) {
            User user = findUserByAccount(iban, users);
            if (user != null) {
                user.removePendingSplitPayment(splitPayment);
            }
        }
    }

    /**
     * Adds a card to all account instances with the same IBAN in all users' account lists.
     *
     * @param card   the card to add
     * @param iban   the IBAN of the account
     * @param users  the list of all users in the system
     */
    public static void addCardToAllInstances(Card card, String iban, List<User> users) {
        for (User user : users) {
            for (Account account : user.getAccounts()) {
                if (account.getIban().equals(iban)) {
                    // Verificăm dacă cardul nu există deja
                    if (!account.getCards().contains(card)) {
                        account.addCard(card); // Adaugă cardul în contul cu IBAN-ul respectiv
                    }
                }
            }
        }
    }

    public static double calculateSpentBetween(
            Map<String, List<TransactionDetail>> associateTransactions,
            String email,
            int startTimestamp,
            int endTimestamp) {
        List<TransactionDetail> transactions = associateTransactions.getOrDefault(email, new ArrayList<>());
        return transactions.stream()
                .filter(t -> t.getType().equals("spend") &&
                        t.getTimestamp() >= startTimestamp &&
                        t.getTimestamp() <= endTimestamp)
                .mapToDouble(TransactionDetail::getAmount)
                .sum();
    }

    public static double calculateDepositedBetween(
            Map<String, List<TransactionDetail>> associateTransactions,
            String email,
            int startTimestamp,
            int endTimestamp) {
        List<TransactionDetail> transactions = associateTransactions.getOrDefault(email, new ArrayList<>());
        return transactions.stream()
                .filter(t -> t.getType().equals("deposit") &&
                        t.getTimestamp() >= startTimestamp &&
                        t.getTimestamp() <= endTimestamp)
                .mapToDouble(TransactionDetail::getAmount)
                .sum();
    }

    public static String extractUsernameFromEmail(String email) {
        // Split the email at '@' and then at '_'
        String[] parts = email.split("@")[0].split("_");
        if (parts.length >= 2) {
            // Extract last names (all parts except the first)
            StringBuilder lastNames = new StringBuilder();
            for (int i = 1; i < parts.length; i++) {
                if (i > 1) {
                    lastNames.append(" ");
                }
                lastNames.append(capitalizeWithHyphen(parts[i]));
            }

            // Extract first name (the first part)
            String firstName = capitalizeWithHyphen(parts[0]);

            // Combine last names and first name
            return lastNames.toString() + " " + firstName;
        }

        // If there's only one part, just capitalize it
        return capitalizeWithHyphen(parts[0]);
    }

    /**
     * Capitalizes each word, including those separated by hyphens.
     * Preserves the original case if the part is entirely uppercase.
     *
     * @param str the input string
     * @return the capitalized string
     */
    private static String capitalizeWithHyphen(String str) {
        String[] hyphenParts = str.split("-");
        for (int i = 0; i < hyphenParts.length; i++) {
            if (hyphenParts[i].isEmpty()) {
                continue; // Skip empty parts
            }
            if (isAllUpperCase(hyphenParts[i])) {
                // Preserve uppercase if the part is entirely uppercase
                hyphenParts[i] = hyphenParts[i];
            } else {
                // Capitalize the first letter and lowercase the rest
                hyphenParts[i] = hyphenParts[i].substring(0, 1).toUpperCase() +
                        hyphenParts[i].substring(1).toLowerCase();
            }
        }
        return String.join("-", hyphenParts);
    }

    /**
     * Checks if the entire string is in uppercase.
     *
     * @param str the input string
     * @return true if all characters are uppercase, false otherwise
     */
    private static boolean isAllUpperCase(String str) {
        for (char c : str.toCharArray()) {
            if (Character.isLetter(c) && !Character.isUpperCase(c)) {
                return false;
            }
        }
        return true;
    }

    public static boolean isCommerciantIban(final String iban, final List<Commerciant> commerciants) {
        // method to verify in the list of commerciants if the given iban is of one commerciant
        for (Commerciant commerciant : commerciants) {
            if (commerciant.getAccount().equals(iban)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Finds an account by its alias from the list of users.
     *
     * @param alias the alias to search for
     * @param users the list of users to search within
     * @return the Account object with the matching alias, or null if not found
     */
    public static Account findAccountByAlias(String alias, List<User> users) {
        if (alias == null || alias.isEmpty() || users == null) {
            return null;
        }

        for (User user : users) {
            List<Account> accounts = user.getAccounts(); // Get the accounts of the user
            if (accounts == null) {
                continue;
            }

            for (Account account : accounts) {
                if (alias.equalsIgnoreCase(account.getAlias())) {
                    return account; // Return the account if alias matches
                }
            }
        }

        return null; // Return null if no matching account is found
    }


}
