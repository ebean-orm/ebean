package io.ebeaninternal.server.type;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * ScalarType for java.nio.file.Path which converts to and from a VARCHAR database column.
 */
public class ScalarTypePath extends ScalarTypeBaseVarchar<Path> {

  public ScalarTypePath() {
    super(Path.class);
  }

  @Override
  public Path convertFromDbString(String dbValue) {
    try {
      return Paths.get(new URI(dbValue));
    } catch (URISyntaxException e) {
      throw new RuntimeException("Error with Path URI [" + dbValue + "] " + e);
    }
  }

  @Override
  public String convertToDbString(Path beanValue) {
    return beanValue.toUri().toString();
  }

  @Override
  public String formatValue(Path path) {
    return convertToDbString(path);
  }

  @Override
  public Path parse(String value) {
    return convertFromDbString(value);
  }
}
