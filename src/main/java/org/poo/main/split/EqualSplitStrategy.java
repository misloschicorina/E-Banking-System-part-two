package org.poo.main.split;

import org.poo.fileio.CommandInput;

import java.util.ArrayList;
import java.util.List;

/**
 *  Strategy for equal split payments.
 */
public final class EqualSplitStrategy implements SplitStrategy {

    /**
     * Calculates the amounts to be split equally among the given accounts.
     *
     * @param totalAmount the total amount to be split
     * @param ibans the list of IBANs representing the accounts involved in the split
     * @param command the command input containing additional information for the split
     * @return a list of equal amounts corresponding to each account
     */
    @Override
    public List<Double> calculateSplit(final double totalAmount, final List<String> ibans,
                                                                final CommandInput command) {
        // Calculate the equal split amount
        double splitAmount = totalAmount / ibans.size();
        List<Double> amounts = new ArrayList<>();

        // Add the split amount for each IBAN
        for (int i = 0; i < ibans.size(); i++) {
            amounts.add(splitAmount);
        }
        return amounts;
    }
}
