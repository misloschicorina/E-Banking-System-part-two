package org.poo.main.transactions;

/**
 * Filters transactions that represent card payments associated with a given IBAN.
 */
public class SpendingsTransactionFilter implements TransactionFilter {

    /**
     * Filters transactions based on the description "Card payment" and matching IBAN.
     *
     * @param transaction The transaction to be filtered. Must not be null.
     * @param iban        The IBAN to match. Must not be null.
     * @return true if the transaction is a card payment associated with the given IBAN,
     *         false otherwise.
     */
    @Override
    public boolean filter(final Transaction transaction, final String iban) {
        return "Card payment".equals(transaction.getDescription())
                && iban.equals(transaction.getAccountIBAN());
    }
}
