package com.avaje.tests.model.ivo;

import java.util.Currency;

/**
 * Currency and Money.
 * 
 * @author rbygrave
 *
 */
public class CMoney {

    private final Money amount;
    private final Currency currency;
    
    public CMoney(Money amount, Currency currency) {
        this.amount = amount;
        this.currency = currency;
    }

    public Money getAmount() {
        return amount;
    }

    public Currency getCurrency() {
        return currency;
    }
}
