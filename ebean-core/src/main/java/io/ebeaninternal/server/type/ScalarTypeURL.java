package io.ebeaninternal.server.type;

import io.ebean.text.TextException;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * ScalarType for java.net.URL which converts to and from a VARCHAR database column.
 */
final class ScalarTypeURL extends ScalarTypeBaseVarchar<URL> {

  ScalarTypeURL() {
    super(URL.class);
  }

  @Override
  public URL convertFromDbString(String dbValue) {
    try {
      return new URL(dbValue);
    } catch (MalformedURLException e) {
      throw new RuntimeException("Error with URL [" + dbValue + "] " + e);
    }
  }

  @Override
  public String convertToDbString(URL beanValue) {
    return formatValue(beanValue);
  }

  @Override
  public String formatValue(URL v) {
    return v.toString();
  }

  @Override
  public URL parse(String value) {
    try {
      return new URL(value);
    } catch (MalformedURLException e) {
      throw new TextException("Error with URL [{}]", value, e);
    }
  }

}
