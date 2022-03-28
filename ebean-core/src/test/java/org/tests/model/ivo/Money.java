package org.tests.model.ivo;


import java.io.Serializable;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Iterator;

/**
 * A representation of Money effectively wrapping BigDecimal.
 * <p>
 * <p>
 * </p>
 *
 * @author rbygrave
 */
public final class Money implements Comparable<Money>, Serializable {

  private static final long serialVersionUID = 1L;

  public static final Money ZERO = new Money(BigDecimal.ZERO);

  private final BigDecimal amount;

  public Money(BigDecimal amount) {
    if (amount == null) {
      throw new NullPointerException("amount can not be null");
    }
    this.amount = amount;
  }

  public Money(double val) {
    this(BigDecimal.valueOf(val));
  }

  public Money(String val) {
    this(new BigDecimal(val));
  }

  @Override
  public String toString() {
    return amount.toString();
  }


  @Override
  public int hashCode() {
    return amount.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof Money) {
      // use BigDecimal.compareTo to handle scale differences
      return amount.compareTo(((Money) obj).getAmount()) == 0;
    }
    return false;
  }

  @Override
  public int compareTo(Money o) {
    return amount.compareTo(o.amount);
  }

  public BigDecimal getAmount() {
    return amount;
  }

  public Money subtract(Money amt) {
    BigDecimal b = amount.subtract(amt.amount);
    return new Money(b);
  }

  public Money subtract(Money amt, MathContext ctx) {
    BigDecimal b = amount.subtract(amt.amount, ctx);
    return new Money(b);
  }

  public Money add(Money amt) {
    BigDecimal b = amount.add(amt.amount);
    return new Money(b);
  }

  public Money add(Money amt, MathContext ctx) {
    BigDecimal b = amount.add(amt.amount, ctx);
    return new Money(b);
  }

  public Money multiply(Money m) {
    return multiply(m.amount);
  }

  public Money multiply(int val) {
    return multiply(BigDecimal.valueOf(val));
  }

  public Money multiply(float val) {
    return multiply(BigDecimal.valueOf(val));
  }

  public Money multiply(double val) {
    return multiply(BigDecimal.valueOf(val));
  }

  public Money multiply(BigDecimal m) {
    BigDecimal b = amount.multiply(m);
    return new Money(b);
  }

  public Money divide(BigDecimal divisor) {
    BigDecimal b = amount.divide(divisor);
    return new Money(b);
  }

  public Money divide(BigDecimal divisor, MathContext ctx) {
    BigDecimal b = amount.divide(divisor, ctx);
    return new Money(b);
  }

  public static Money sum(Money... m) {
    BigDecimal t = BigDecimal.ZERO;
    for (Money money : m) {
      t = t.add(money.amount);
    }
    return new Money(t);
  }

  public static Money sum(Iterator<Money> it) {
    BigDecimal t = BigDecimal.ZERO;
    while (it.hasNext()) {
      t = t.add(it.next().amount);
    }
    return new Money(t);
  }

  public static Money sum(Iterator<Money> it, MathContext ctx) {
    BigDecimal t = BigDecimal.ZERO;
    while (it.hasNext()) {
      t = t.add(it.next().amount, ctx);
    }
    return new Money(t);
  }
}
