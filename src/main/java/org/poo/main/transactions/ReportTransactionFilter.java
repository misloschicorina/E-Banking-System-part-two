package org.poo.main.transactions;

/**
 * Filters transactions based on the provided IBAN. Matches transactions where the IBAN is
 * involved as the sender, receiver, or part of the associated accounts.
 */
public class ReportTransactionFilter implements TransactionFilter {

    /**
     * Filters transactions based on the IBAN.
     *
     * @param transaction The transaction to be filtered. Must not be null.
     * @param iban        The IBAN to match. Must not be null.
     * @return true if the transaction matches the given IBAN, false otherwise.
     */
    @Override
    public boolean filter(final Transaction transaction, final String iban) {
        if (transaction.getAccountIBAN() == null) {
            return (transaction.getSenderIBAN() != null
                    && transaction.getSenderIBAN().equals(iban))
                    || (transaction.getReceiverIBAN() != null
                    && transaction.getReceiverIBAN().equals(iban))
                    || (transaction.getInvolvedAccounts() != null
                    && transaction.getInvolvedAccounts().contains(iban));
        }
        return transaction.getAccountIBAN().equals(iban);
    }
}
