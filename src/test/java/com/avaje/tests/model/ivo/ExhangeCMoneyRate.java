
package com.avaje.tests.model.ivo;

/**
 * Test compound type with nested compound type.
 */
public class ExhangeCMoneyRate {

    private final Rate rate;
    private final CMoney cmoney;
    
    public ExhangeCMoneyRate(Rate rate, CMoney cmoney) {
        this.rate = rate;
        this.cmoney = cmoney;
    }

    public Rate getRate() {
        return rate;
    }

    public CMoney getCmoney() {
        return cmoney;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ExhangeCMoneyRate){
            ExhangeCMoneyRate e = (ExhangeCMoneyRate)obj;
            return e.rate.equals(rate) && e.cmoney.equals(cmoney);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hc = rate.hashCode();
        hc = hc * 31 + cmoney.hashCode();
        return hc;
    }

    @Override
    public String toString() {
        return "rate="+rate+" cmoney="+cmoney;
    }
    
    
}
