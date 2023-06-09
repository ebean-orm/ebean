package io.ebean.platform.postgres;

import io.ebean.config.dbplatform.BasicBindValidatorFactory;
import org.postgresql.util.PGobject;

/**
 * @author Roland Praml, FOCONIS AG
 */
public class PgBindValidatorFactory extends BasicBindValidatorFactory {
  @Override
  protected void validate(Object value, int dbLength, String table, String column) {
    if (value instanceof PGobject) {
      value = ((PGobject) value).getValue();
    }
    super.validate(value, dbLength, table, column);
  }
}
