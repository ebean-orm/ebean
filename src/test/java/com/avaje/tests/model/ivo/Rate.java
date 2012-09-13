package com.avaje.tests.model.ivo;


import java.io.Serializable;
import java.math.BigDecimal;
import java.math.MathContext;

/**
 * A representation of a Rate (like Tax or Discount) that is typically
 * used to multiple against an amount (like Money).
 * <p>
 * A Rate effectively wraps a BigDecimal with associated control over
 * precision and scale.
 * <p>
 * Rates are typically used to multiple or divide against an amount.
 * For example, you can't ADD a Rate to Money.
 * </p>
 * @author rbygrave
 *
 */
public final class Rate implements Comparable<Rate>, Serializable {

    private static final long serialVersionUID = 1L;

    public static final Rate ZERO = new Rate(BigDecimal.ZERO);
    public static final Rate ONE = new Rate(BigDecimal.ONE);
    
    private final BigDecimal value;
    
    public Rate(BigDecimal value) {
        if (value == null){
            throw new NullPointerException("value can not be null");
        }
        this.value = value;
    }

    public Rate(double val) {
        this(BigDecimal.valueOf(val));
    }

    public Rate(String val) {
        this(new BigDecimal(val));
    }

    public String toString() {
        return value.toString();
    }
    
    public int compareTo(Rate o) {
        return value.compareTo(o.value);
    }
    
    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Rate){
            // use BigDecimal.compareTo to handle scale differences
            return value.compareTo(((Rate)obj).getValue()) == 0;
        }
        return false;
    }

    public BigDecimal getValue() {
        return value;
    }
    
    public Rate subtract(Rate amt){
        BigDecimal b = value.subtract(amt.value);
        return new Rate(b);
    }
    
    public Rate subtract(Rate amt, MathContext ctx){
        BigDecimal b = value.subtract(amt.value, ctx);
        return new Rate(b);
    }
    
    public Rate add(Rate amt){
        BigDecimal b = value.add(amt.value);
        return new Rate(b);
    }

    public Rate add(Rate amt, MathContext ctx){
        BigDecimal b = value.add(amt.value, ctx);
        return new Rate(b);
    }

    /**
     * A Rate times Money amount returns Money.
     */
    public Money times(Money amount){
        return multiply(amount);
    }

    public Money multiply(Money amount){
        BigDecimal b = value.multiply(amount.getAmount());
        return new Money(b);
    }

    public Rate multiply(Rate m){
        return multiply(m.value);
    }

    public Rate multiply(int val){
        return multiply(BigDecimal.valueOf(val));
    }

    public Rate multiply(float val){
        return multiply(BigDecimal.valueOf(val));
    }

    public Rate multiply(double val){
        return multiply(BigDecimal.valueOf(val));
    }
    
    public Rate multiply(BigDecimal m){
        BigDecimal b = value.multiply(m);
        return new Rate(b);
    }

    public Rate divide(BigDecimal divisor){
        BigDecimal b = value.divide(divisor);
        return new Rate(b);
    }
    
    public Rate divide(BigDecimal divisor, MathContext ctx){
        BigDecimal b = value.divide(divisor, ctx);
        return new Rate(b);
    }

}
