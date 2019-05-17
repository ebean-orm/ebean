package io.ebeaninternal.server.type;

import io.ebean.config.dbplatform.ExtraDbTypes;
import io.ebean.types.Cdir;

import java.sql.SQLException;
import java.sql.Types;

/**
 * ScalarType for Cdir to Varchar or Postgres CDIR.
 */
public abstract class ScalarTypeCdir extends ScalarTypeBaseVarchar<Cdir> {

  ScalarTypeCdir() {
    super(Cdir.class, false, ExtraDbTypes.INET);
  }

  @Override
  public abstract void bind(DataBind b, Cdir value) throws SQLException;

  @Override
  public Cdir convertFromDbString(String dbValue) {
    return parse(dbValue);
  }

  @Override
  public String convertToDbString(Cdir beanValue) {
    return formatValue(beanValue);
  }

  @Override
  public String formatValue(Cdir value) {
    return value.getAddress();
  }

  @Override
  public Cdir parse(String value) {
    return new Cdir(value);
  }

  /**
   * Cdir to Varchar.
   */
  public static class Varchar extends ScalarTypeCdir {

    @Override
    public void bind(DataBind b, Cdir value) throws SQLException {
      if (value == null) {
        b.setNull(Types.VARCHAR);
      } else {
        b.setString(convertToDbString(value));
      }
    }
  }

  /**
   * Cdir to Postgres CDIR.
   */
  public static class Postgres extends ScalarTypeCdir {

    @Override
    public void bind(DataBind b, Cdir value) throws SQLException {
      if (value == null) {
        b.setNull(Types.OTHER);
      } else {
        String strValue = convertToDbString(value);
        b.setObject(PostgresHelper.asInet(strValue));
      }
    }
  }
}
