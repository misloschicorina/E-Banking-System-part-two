package org.poo.main;

import org.poo.main.Commerciant.Commerciant;
import org.poo.main.exchange_rate.ExchangeRate;

import java.util.List;

public class CashbackInfo {
    public int transactionCount;  // number of transactions made at commerciant
    public double totalSpent;     // total amount spent at commerciant
    public int cashbackApplied;   // how many cashbacks were applied (for nrOfTransactions chasback type)

    public CashbackInfo() {
        this.transactionCount = 0;
        this.totalSpent = 0.0;
        this.cashbackApplied = 0;
    }

    public void addTransaction(double amount, String transactionCurrency, String targetCurrency, List<ExchangeRate> exchangeRates) {
        // Verify if rate exchange is necessary and perform if needed
        if (!transactionCurrency.equals(targetCurrency)) {
            double exchangeRate = ExchangeRate.getExchangeRate(transactionCurrency, targetCurrency, exchangeRates);
            amount = Math.round((amount * exchangeRate) * 100.0) / 100.0;
        }

        // Incrementing the nr of transactions and total amount spent
        this.transactionCount++;
        this.totalSpent += amount;
    }

    public double calculateTransactionCashback(final double amount, final Commerciant commerciant) {
        // Definim ratele de cashback pentru fiecare tip de comerciant
        double cashbackAmount = 0.0;
        double cashbackRate = 0.0;

        // Verificăm tipul comerciantului (Food, Clothes, Tech) pentru a aplica cashback-ul corect
        String category = commerciant.getType();

        // Verificăm pragul de tranzacții atins
        if (this.transactionCount >= 2 && this.cashbackApplied < 1) {
            // Aplicăm 2% cashback pentru următoarea tranzacție la un comerciant de tip Food
            if ("Food".equalsIgnoreCase(category)) {
                cashbackRate = 0.02; // 2% cashback
                cashbackAmount = amount * cashbackRate;
                this.cashbackApplied++;  // Incrementăm numărul de cashback-uri aplicate
            }
        } else if (this.transactionCount >= 5 && this.cashbackApplied < 2) {
            // Aplicăm 5% cashback pentru următoarea tranzacție la un comerciant de tip Clothes
            if ("Clothes".equalsIgnoreCase(category)) {
                cashbackRate = 0.05; // 5% cashback
                cashbackAmount = amount * cashbackRate;
                this.cashbackApplied++;  // Incrementăm numărul de cashback-uri aplicate
            }
        } else if (this.transactionCount >= 10 && this.cashbackApplied < 3) {
            // Aplicăm 10% cashback pentru următoarea tranzacție la un comerciant de tip Tech
            if ("Tech".equalsIgnoreCase(category)) {
                cashbackRate = 0.10; // 10% cashback
                cashbackAmount = amount * cashbackRate;
                this.cashbackApplied++;  // Incrementăm numărul de cashback-uri aplicate
            }
        }

        // Verificăm dacă au fost deja aplicate 3 cashbackuri
        if (this.cashbackApplied >= 3) {
            // Dacă s-au aplicat deja 3 cashbackuri, nu mai aplicăm niciun alt cashback
            return 0.0;  // Nicio sumă de cashback nu va fi aplicată
        }

        // System.out.println("Transaction-based cashback calculated: " + cashbackAmount);

        return cashbackAmount;
    }

    public double calculateSpendingCashback(final double amount, final String userPlan,
                                            final String currency, final List<ExchangeRate> exchangeRates) {
        // Obținem totalSpent în moneda contului
        double totalSpentInCurrency = this.totalSpent;

        // Definim ratele de cashback în funcție de planul utilizatorului
        double cashbackRate100 = 0.0, cashbackRate300 = 0.0, cashbackRate500 = 0.0;

        if ("standard".equals(userPlan) || "student".equals(userPlan)) {
            cashbackRate100 = 0.001;  // 0.1%
            cashbackRate300 = 0.002;  // 0.2%
            cashbackRate500 = 0.0025; // 0.25%
        } else if ("silver".equals(userPlan)) {
            cashbackRate100 = 0.003;  // 0.3%
            cashbackRate300 = 0.004;  // 0.4%
            cashbackRate500 = 0.005;  // 0.5%
        } else if ("gold".equals(userPlan)) {
            cashbackRate100 = 0.005;  // 0.5%
            cashbackRate300 = 0.0055; // 0.55%
            cashbackRate500 = 0.007;  // 0.7%
        }

        // Transformăm pragurile în moneda contului
        double threshold100 = 100.0;
        double threshold300 = 300.0;
        double threshold500 = 500.0;

        if (!currency.equals("RON")) { // Dacă moneda contului nu este RON
            double exchangeRate = ExchangeRate.getExchangeRate("RON", currency, exchangeRates);
            threshold100 = Math.round((100.0 * exchangeRate) * 100.0) / 100.0;
            threshold300 = Math.round((300.0 * exchangeRate) * 100.0) / 100.0;
            threshold500 = Math.round((500.0 * exchangeRate) * 100.0) / 100.0;
        }

        // System.out.println("Praguri în moneda contului (" + currency + "): " + threshold100 + ", " + threshold300 + ", " + threshold500);

        // Verificăm pragurile pentru cashback
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

        double newAmount = amount;
        double exchangeRate = ExchangeRate.getExchangeRate("RON", currency, exchangeRates);
        newAmount = Math.round((amount * exchangeRate) * 100.0) / 100.0;

        // Calculăm cashback-ul pe baza valorii tranzacției curente
        double cashbackAmount = newAmount * cashbackRate;

        return cashbackAmount;
    }

}
