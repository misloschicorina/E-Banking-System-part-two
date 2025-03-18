package org.poo.main.commerciant;

/**
 * Represents a commerciant (merchant) in the banking system.
 */
public final class Commerciant {

    private String name;
    private int id;
    private String account; // IBAN
    private String type;
    private String cashbackStrategy;

    public Commerciant(final String name, final int id, final String account,
                       final String type, final String cashbackStrategy) {
        this.name = name;
        this.id = id;
        this.account = account;
        this.type = type;
        this.cashbackStrategy = cashbackStrategy;
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    public void setId(final int id) {
        this.id = id;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(final String account) {
        this.account = account;
    }

    public String getType() {
        return type;
    }

    public void setType(final String type) {
        this.type = type;
    }

    public String getCashbackStrategy() {
        return cashbackStrategy;
    }

    public void setCashbackStrategy(final String cashbackStrategy) {
        this.cashbackStrategy = cashbackStrategy;
    }
}
