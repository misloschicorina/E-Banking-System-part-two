package org.poo.main.cards;

import org.poo.main.accounts.Account;
import org.poo.main.user.User;

/**
 * Factory class for creating one-time use cards.
 */
public final class OneTimeCardFactory implements CardFactory {

    /**
     * Creates a new one-time use card associated with a user and account.
     *
     * @param user the owner of the card
     * @param account the account linked to the card
     * @param cardNumber the unique identifier of the card
     * @return a new instance of OneTimeCard
     */
    @Override
    public Card createCard(final User user, final Account account, final String cardNumber) {
        return new OneTimeCard(user, account, cardNumber);
    }
}
