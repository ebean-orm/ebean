package io.ebean.config.dbplatform;

import java.nio.charset.StandardCharsets;

/**
 * BindValidator that validates the UTF8 length. For example, this is required for DB2 when not using the CODEUNITS32 character set.
 *
 * @author Roland Praml, FOCONIS AG
 */
public class Utf8BindValidatorFactory extends DefaultBindValidatorFactory {

  /**
   * Default validator, that handles length check for String, Arrays, and Files, respectively InputStreamInfo.
   */
  protected void validate(Object value, int dbLength, String table, String column) {
    if (value instanceof String) {
      String s = (String) value;
      if (s.length() < dbLength * 4) {
        return;
      }
      value = s.getBytes(StandardCharsets.UTF_8);
    }
    super.validate(value, dbLength, table, column);
  }
}
