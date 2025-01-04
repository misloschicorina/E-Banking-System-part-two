package org.poo.main.cards;

import org.poo.main.accounts.Account;
import org.poo.main.user.User;

/**
 * Represents a bank card associated with an account and a user.
 */
public class Card {
    private String cardNumber;
    private User cardOwner;
    private Account account; // Associated account
    private String status;

    public static final String STATUS_ACTIVE = "active";
    public static final String STATUS_INACTIVE = "frozen";

    public Card(final User cardOwner, final Account account, final String cardNumber) {
        this.cardNumber = cardNumber;
        this.cardOwner = cardOwner;
        this.account = account;
        this.status = STATUS_ACTIVE;
    }

    /**
     * Gets the card number.
     *
     * @return the card number
     */
    public String getCardNumber() {
        return cardNumber;
    }

    /**
     * Gets the card owner.
     *
     * @return the card owner
     */
    public User getCardOwner() {
        return cardOwner;
    }

    /**
     * Gets the associated account.
     *
     * @return the associated account
     */
    public Account getAccount() {
        return account;
    }

    /**
     * Gets the status of the card.
     *
     * @return the status of the card
     */
    public String getStatus() {
        return status;
    }

    /**
     * Checks if this is a one-time card.
     *
     * @return false by default
     */
    public boolean isOneTimeCard() {
        return false;
    }

    /**
     * Freezes the card by setting its status to inactive.
     */
    public void freezeCard() {
        this.status = STATUS_INACTIVE;
    }
}
