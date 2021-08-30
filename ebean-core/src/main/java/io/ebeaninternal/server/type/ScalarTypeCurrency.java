package io.ebeaninternal.server.type;

import java.util.Currency;

/**
 * ScalarType for java.util.Currency which converts to and from a VARCHAR database column.
 */
final class ScalarTypeCurrency extends ScalarTypeBaseVarchar<Currency> {

  ScalarTypeCurrency() {
    super(Currency.class);
  }

  @Override
  public int getLength() {
    return 3;
  }

  @Override
  public Currency convertFromDbString(String dbValue) {
    return Currency.getInstance(dbValue);
  }

  @Override
  public String convertToDbString(Currency beanValue) {
    return beanValue.getCurrencyCode();
  }

  @Override
  public String formatValue(Currency v) {
    return v.toString();
  }

  @Override
  public Currency parse(String value) {
    return Currency.getInstance(value);
  }

}
