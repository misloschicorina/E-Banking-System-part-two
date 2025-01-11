package org.poo.main.cards;

import org.poo.main.accounts.Account;
import org.poo.main.user.User;

/**
 * Factory class for creating standard cards.
 */
public final class StandardCardFactory implements CardFactory {

    /**
     * Creates a new standard card associated with a user and account.
     *
     * @param user the owner of the card
     * @param account the account linked to the card
     * @param cardNumber the unique identifier of the card
     * @return a new instance of Card
     */
    @Override
    public Card createCard(final User user, final Account account, final String cardNumber) {
        return new Card(user, account, cardNumber);
    }
}
