package org.poo.main.cashback;

import org.poo.main.Commerciant.Commerciant;
import org.poo.main.accounts.Account;
import org.poo.main.exchange_rate.ExchangeRate;

import java.util.List;

/**
 * Class representing cashback information and utilities for calculating cashback.
 */
public final class CashbackInfo {
    private static final double STANDARD_CASHBACK_100 = 0.001;
    private static final double STANDARD_CASHBACK_300 = 0.002;
    private static final double STANDARD_CASHBACK_500 = 0.0025;

    private static final double SILVER_CASHBACK_100 = 0.003;
    private static final double SILVER_CASHBACK_300 = 0.004;
    private static final double SILVER_CASHBACK_500 = 0.005;

    private static final double GOLD_CASHBACK_100 = 0.005;
    private static final double GOLD_CASHBACK_300 = 0.0055;
    private static final double GOLD_CASHBACK_500 = 0.007;

    private static final double THRESHOLD_100 = 100.0;
    private static final double THRESHOLD_300 = 300.0;
    private static final double THRESHOLD_500 = 500.0;

    private int transactionCount;
    private double totalSpent;
    private int cashbackApplied;

    public CashbackInfo() {
        this.transactionCount = 0;
        this.totalSpent = 0.0;
        this.cashbackApplied = 0;
    }

    /**
     * Calculates the cashback amount for a transaction based on the merchant's strategy.
     *
     * @param amount the transaction amount
     * @param commerciant the merchant involved in the transaction
     * @return the calculated cashback amount
     */
    public double calculateTransactionCashback(final double amount, final Commerciant commerciant) {
        //  nr Transaction cashback logic not implemented yet
        return 0.0;
    }

    /**
     * Calculates the cashback based on the total spending threshold.
     *
     * @param amount the current transaction amount
     * @param userPlan the user's account plan (e.g., standard, silver, gold)
     * @param currency the account's currency
     * @param paymentCurrency the currency of the transaction
     * @param exchangeRates the list of exchange rates for currency conversion
     * @param account the account involved in the transaction
     * @return the calculated cashback amount
     */
    public double calculateSpendingCashback(final double amount, final String userPlan,
                                            final String currency, final String paymentCurrency,
                                            final List<ExchangeRate> exchangeRates,
                                            final Account account) {
        double totalSpentInCurrency = account.getTotalSpendingThreshold();

        // Determine cashback rates based on the user's plan
        double cashbackRate100 = 0.0, cashbackRate300 = 0.0, cashbackRate500 = 0.0;

        switch (userPlan) {
            case "standard", "student" -> {
                cashbackRate100 = STANDARD_CASHBACK_100;
                cashbackRate300 = STANDARD_CASHBACK_300;
                cashbackRate500 = STANDARD_CASHBACK_500;
            }
            case "silver" -> {
                cashbackRate100 = SILVER_CASHBACK_100;
                cashbackRate300 = SILVER_CASHBACK_300;
                cashbackRate500 = SILVER_CASHBACK_500;
            }
            case "gold" -> {
                cashbackRate100 = GOLD_CASHBACK_100;
                cashbackRate300 = GOLD_CASHBACK_300;
                cashbackRate500 = GOLD_CASHBACK_500;
            }
            default -> {
                // Unknown plan, no cashback applied
            }
        }

        // Adjust thresholds for the account's currency
        double threshold100 = THRESHOLD_100;
        double threshold300 = THRESHOLD_300;
        double threshold500 = THRESHOLD_500;

        if (!currency.equals("RON")) {
            double exchangeRate = ExchangeRate.getExchangeRate("RON", currency, exchangeRates);
            threshold100 = Math.round((THRESHOLD_100 * exchangeRate) * 100.0) / 100.0;
            threshold300 = Math.round((THRESHOLD_300 * exchangeRate) * 100.0) / 100.0;
            threshold500 = Math.round((THRESHOLD_500 * exchangeRate) * 100.0) / 100.0;
        }

        // Determine the appropriate cashback rate based on thresholds
        double cashbackRate = 0.0;
        if (totalSpentInCurrency >= threshold500) {
            cashbackRate = cashbackRate500;
            this.cashbackApplied++;
        } else if (totalSpentInCurrency >= threshold300) {
            cashbackRate = cashbackRate300;
            this.cashbackApplied++;
        } else if (totalSpentInCurrency >= threshold100) {
            cashbackRate = cashbackRate100;
            this.cashbackApplied++;
        }

        // Convert the transaction amount to the account's currency
        double transactionAmountInCurrency = amount;
        if (!currency.equals(paymentCurrency)) {
            double exchangeRate =
                    ExchangeRate.getExchangeRate(paymentCurrency, currency, exchangeRates);
            transactionAmountInCurrency = Math.round((amount * exchangeRate) * 100.0) / 100.0;
        }

        // Calculate and return the cashback amount
        return transactionAmountInCurrency * cashbackRate;
    }

}
