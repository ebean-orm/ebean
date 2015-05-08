package com.avaje.ebeaninternal.server.type;

import java.util.Locale;

/**
 * ScalarType for java.util.Currency which converts to and from a VARCHAR database column.
 */
public class ScalarTypeLocale extends ScalarTypeBaseVarchar<Locale> {

  public ScalarTypeLocale() {
    super(Locale.class);
  }

  @Override
  public int getLength() {
    return 20;
  }

  @Override
  public Locale convertFromDbString(String dbValue) {
    return parse(dbValue);
  }

  @Override
  public String convertToDbString(Locale beanValue) {
    return ((Locale) beanValue).toString();
  }

  @Override
  public String formatValue(Locale t) {
    return t.toString();
  }

  @Override
  public Locale parse(String value) {

    int pos1 = -1;
    int pos2 = -1;

    for (int i = 0; i < value.length(); i++) {
      char c = value.charAt(i);
      if (c == '_') {
        if (pos1 > -1) {
          pos2 = i;
          break;
        } else {
          pos1 = i;
        }
      }
    }
    if (pos1 == -1) {
      return new Locale(value);
    }
    String language = value.substring(0, pos1);
    if (pos2 == -1) {
      String country = value.substring(pos1 + 1);
      return new Locale(language, country);
    } else {
      String country = value.substring(pos1 + 1, pos2);
      String variant = value.substring(pos2 + 1);
      return new Locale(language, country, variant);
    }
  }

}
