package org.poo.main.cards;

import org.poo.main.accounts.Account;
import org.poo.main.user.User;

/**
 * Factory interface for creating card objects.
 */
public interface CardFactory {

    /**
     * Creates a card associated with a user and account.
     *
     * @param user the owner of the card
     * @param account the account linked to the card
     * @param cardNumber the unique identifier of the card
     * @return a new instance of a Card
     */
    Card createCard(User user, Account account, String cardNumber);
}
