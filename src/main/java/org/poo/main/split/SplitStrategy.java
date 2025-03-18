package org.poo.main.split;

import org.poo.fileio.CommandInput;

import java.util.List;

/**
 * Strategy interface for calculating split payment amounts.
 * Different implementations provide specific ways to split the total amount among accounts.
 */
public interface SplitStrategy {

    /**
     * Calculates the amounts to be split among the given accounts.
     *
     * @param totalAmount the total amount to be split
     * @param ibans the list of IBANs representing the accounts involved in the split
     * @param command the command input containing additional information for the split
     * @return a list of amounts corresponding to each account
     */
    List<Double> calculateSplit(double totalAmount, List<String> ibans, CommandInput command);
}
