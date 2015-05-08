package com.avaje.ebeaninternal.server.type;

import java.net.InetAddress;

import com.avaje.ebean.text.TextException;

/**
 * ScalarType for java.net.URI which converts to and from a VARCHAR database column.
 */
public class ScalarTypeInetAddress extends ScalarTypeBaseVarchar<InetAddress> {

  public ScalarTypeInetAddress() {
    super(InetAddress.class);
  }

  @Override
  public int getLength() {
    return 50;
  }

  @Override
  public InetAddress convertFromDbString(String dbValue) {
    try {
      return parse(dbValue);
    } catch (IllegalArgumentException e) {
      throw new RuntimeException("Error with InetAddresses [" + dbValue + "] " + e);
    }
  }

  @Override
  public String convertToDbString(InetAddress beanValue) {
    return ConvertInetAddresses.toUriString(beanValue);
  }

  @Override
  public String formatValue(InetAddress v) {
    return ConvertInetAddresses.toUriString(v);
  }

  @Override
  public InetAddress parse(String value) {
    try {
      return ConvertInetAddresses.forUriString(value);
    } catch (IllegalArgumentException e) {
      throw new TextException("Error with InetAddresses [" + value + "] ", e);
    }
  }
}
