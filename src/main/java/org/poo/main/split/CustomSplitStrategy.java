package org.poo.main.split;

import org.poo.fileio.CommandInput;

import java.util.List;

/**
 * Strategy for custom split payments.
 */
public final class CustomSplitStrategy implements SplitStrategy {

    /**
     * Retrieves the predefined amounts for each account from the command input.
     *
     * @param totalAmount the total amount to be split (not used in this strategy)
     * @param ibans the list of IBANs representing the accounts involved in the split
     * @param command the command input containing predefined amounts for the split
     * @return a list of amounts corresponding to each account, as specified in the input
     */
    @Override
    public List<Double> calculateSplit(final double totalAmount, final List<String> ibans,
                                                            final CommandInput command) {
        // Retrieve the predefined amounts from the CommandInput
        return command.getAmountForUsers();
    }
}
