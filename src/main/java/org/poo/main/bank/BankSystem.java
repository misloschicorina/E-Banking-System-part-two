package org.poo.main.bank;

import org.poo.main.commands.*;
import org.poo.main.commerciant.Commerciant;
import org.poo.main.exchange_rate.ExchangeRate;
import org.poo.main.transactions.*;
import org.poo.main.user.User;
import org.poo.utils.Utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.poo.fileio.CommandInput;

import java.util.*;

public final class BankSystem {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final List<User> users = new ArrayList<>();
    private final List<ExchangeRate> exchangeRates = new ArrayList<>();
    private final List<Commerciant> commerciants = new ArrayList<>();
    private final TransactionService transactionService;

    private final Command printUsersCommand;
    private final Command addAccountCommand;
    private final Command createCardCommand;
    private final Command createOneTimeCardCommand;
    private final Command addFundsCommand;
    private final Command deleteAccountCommand;
    private final Command deleteCardCommand;
    private final Command payOnlineCommand;
    private final Command sendMoneyCommand;
    private final Command setAliasCommand;
    private final Command printTransactionsCommand;
    private final Command setMinimumBalanceCommand;
    private final Command checkCardStatusCommand;
    private final Command splitPaymentCommand;
    private final Command acceptSplitPaymentCommand;
    private final Command rejectSplitPaymentCommand;
    private final Command reportCommand;
    private final Command spendingsReportCommand;
    private final Command addInterestCommand;
    private final Command changeInterestRateCommand;
    private final Command withdrawSavingsCommand;
    private final Command upgradePlanCommand;
    private final Command cashWithdrawalCommand;
    private final Command addNewBusinessAssociateCommand;
    private final Command changeSpendingLimitCommand;
    private final Command changeDepositLimitCommand;
    private final Command businessReportCommand;

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
        this.addAccountCommand = new AddAccountCommand(users, exchangeRates, transactionService);
        this.createCardCommand = new CreateCardCommand(users, transactionService);
        this.createOneTimeCardCommand = new CreateOneTimeCardCommand(users, transactionService);
        this.addFundsCommand = new AddFundsCommand(users);
        this.deleteAccountCommand =
                new DeleteAccountCommand(users, transactionService, objectMapper);
        this.deleteCardCommand = new DeleteCardCommand(users, transactionService);
        this.payOnlineCommand =
                new PayOnlineCommand(users, exchangeRates, commerciants, transactionService);
        this.sendMoneyCommand =
                new SendMoneyCommand(users, exchangeRates, commerciants, transactionService);
        this.setAliasCommand = new SetAliasCommand(users);
        this.printTransactionsCommand = new PrintTransactionsCommand(objectMapper, users);
        this.setMinimumBalanceCommand = new SetMinimumBalanceCommand(users);
        this.checkCardStatusCommand =
                new CheckCardStatusCommand(users, transactionService, objectMapper);
        this.splitPaymentCommand = new SplitPaymentCommand(users);
        this.acceptSplitPaymentCommand =
                new AcceptSplitPaymentCommand(users, transactionService, exchangeRates);
        this.rejectSplitPaymentCommand = new RejectSplitPaymentCommand(users, transactionService);
        this.reportCommand = new ReportCommand(objectMapper, users, exchangeRates);
        this.spendingsReportCommand = new
                SpendingsReportCommand(objectMapper, users, exchangeRates);
        this.addInterestCommand = new AddInterestCommand(users, transactionService);
        this.changeInterestRateCommand = new ChangeInterestRateCommand(users, transactionService);
        this.withdrawSavingsCommand =
                new WithdrawSavingsCommand(users, exchangeRates, transactionService);
        this.upgradePlanCommand = new UpgradePlanCommand(users, exchangeRates, transactionService);
        this.cashWithdrawalCommand =
                new CashWithdrawalCommand(users, exchangeRates, transactionService);
        this.addNewBusinessAssociateCommand = new AddNewBusinessAssociateCommand(users);
        this.changeSpendingLimitCommand = new ChangeSpendingLimitCommand(users);
        this.changeDepositLimitCommand = new ChangeDepositLimitCommand(users);
        this.businessReportCommand = new BusinessReportCommand(users);
    }

    public void processCommands(final CommandInput[] commands, final ArrayNode output) {
        for (CommandInput command : commands) {
            switch (command.getCommand()) {
                case "printUsers" -> printUsersCommand.execute(command, output);
                case "addAccount" -> addAccountCommand.execute(command, output);
                case "createCard" -> createCardCommand.execute(command, output);
                case "createOneTimeCard" -> createOneTimeCardCommand.execute(command, output);
                case "addFunds" -> addFundsCommand.execute(command, output);
                case "deleteAccount" -> deleteAccountCommand.execute(command, output);
                case "deleteCard" -> deleteCardCommand.execute(command, output);
                case "payOnline" -> payOnlineCommand.execute(command, output);
                case "sendMoney" -> sendMoneyCommand.execute(command, output);
                case "setAlias" -> setAliasCommand.execute(command, output);
                case "printTransactions" -> printTransactionsCommand.execute(command, output);
                case "setMinimumBalance" -> setMinimumBalanceCommand.execute(command, output);
                case "checkCardStatus" -> checkCardStatusCommand.execute(command, output);
                case "splitPayment" -> splitPaymentCommand.execute(command, output);
                case "acceptSplitPayment" -> acceptSplitPaymentCommand.execute(command, output);
                case "rejectSplitPayment" -> rejectSplitPaymentCommand.execute(command, output);
                case "report" -> reportCommand.execute(command, output);
                case "spendingsReport" -> spendingsReportCommand.execute(command, output);
                case "addInterest" -> addInterestCommand.execute(command, output);
                case "changeInterestRate" -> changeInterestRateCommand.execute(command, output);
                case "withdrawSavings" -> withdrawSavingsCommand.execute(command, output);
                case "upgradePlan" -> upgradePlanCommand.execute(command, output);
                case "cashWithdrawal" -> cashWithdrawalCommand.execute(command, output);
                case "addNewBusinessAssociate" ->
                        addNewBusinessAssociateCommand.execute(command, output);
                case "changeSpendingLimit" -> changeSpendingLimitCommand.execute(command, output);
                case "changeDepositLimit" -> changeDepositLimitCommand.execute(command, output);
                case "businessReport" -> businessReportCommand.execute(command, output);
                default -> {
                }
            }
        }
        Utils.resetRandom();
    }
}
