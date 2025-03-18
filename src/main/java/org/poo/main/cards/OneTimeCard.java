package org.poo.main.cards;

import org.poo.main.accounts.Account;
import org.poo.main.user.User;

/**
 * Represents a one-time use card associated with an account and a user.
 */
public final class OneTimeCard extends Card {
    private boolean used;

    public OneTimeCard(final User cardOwner, final Account account, final String cardNumber) {
        super(cardOwner, account, cardNumber);
        this.used = false;
    }

    /**
     * Checks if the card has been used.
     *
     * @return true if the card has been used, false otherwise
     */
    public boolean isUsed() {
        return used;
    }

    /**
     * Marks the card as used.
     */
    public void markAsUsed() {
        this.used = true;
    }

    /**
     * Indicates that this card is a one-time card.
     *
     * @return true, as this is a one-time card
     */
    @Override
    public boolean isOneTimeCard() {
        return true;
    }
}
