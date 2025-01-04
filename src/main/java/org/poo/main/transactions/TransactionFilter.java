package org.poo.main.transactions;

/**
 * Filter interface for transactions based on given criteria.
 * Implementing classes should override the filter method to define their own filtering logic.
 */
public interface TransactionFilter {

    /**
     * Filters transactions based on the given transaction and IBAN.
     *
     * @param transaction The transaction to be filtered. Must not be null.
     * @param iban        The IBAN associated with the transaction. Must not be null.
     * @return true if the transaction matches the filter criteria, false otherwise.
     */
    boolean filter(Transaction transaction, String iban);
}
