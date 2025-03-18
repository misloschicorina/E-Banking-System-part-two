package org.poo.main.cards;

import org.poo.main.accounts.Account;
import org.poo.main.user.User;

/**
 * Factory class for creating cards.
 */
public final class CardFactory {

    /**
     * Private constructor to prevent instantiation.
     */
    private CardFactory() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated.");
    }

    public enum CardType {
        STANDARD, ONE_TIME
    }

    /**
     * Creates a card of the specified type associated with a user and account.
     *
     * @param cardType   the type of card to create
     * @param user       the owner of the card
     * @param account    the account linked to the card
     * @param cardNumber the unique identifier of the card
     * @return a new instance of a Card
     * @throws IllegalArgumentException if the card type is unknown
     */
    public static Card createCard(final CardType cardType, final User user,
                                  final Account account, final String cardNumber) {
        switch (cardType) {
            case STANDARD:
                return new Card(user, account, cardNumber);
            case ONE_TIME:
                return new OneTimeCard(user, account, cardNumber);
            default:
                throw new IllegalArgumentException("Unknown card type: " + cardType);
        }
    }
}
