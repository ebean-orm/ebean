package io.ebeaninternal.server.type;

import io.ebean.text.TextException;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * ScalarType for java.net.URI which converts to and from a VARCHAR database column.
 */
final class ScalarTypeURI extends ScalarTypeBaseVarchar<URI> {

  ScalarTypeURI() {
    super(URI.class);
  }

  @Override
  public URI convertFromDbString(String dbValue) {
    try {
      return new URI(dbValue);
    } catch (URISyntaxException e) {
      throw new RuntimeException("Error with URI [" + dbValue + "] " + e);
    }
  }

  @Override
  public String convertToDbString(URI beanValue) {
    return beanValue.toString();
  }

  @Override
  public String formatValue(URI v) {
    return v.toString();
  }

  @Override
  public URI parse(String value) {
    try {
      return new URI(value);
    } catch (URISyntaxException e) {
      throw new TextException("Error with URI [{}]", value, e);
    }
  }
}
