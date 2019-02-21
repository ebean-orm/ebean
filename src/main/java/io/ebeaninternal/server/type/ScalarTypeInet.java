package io.ebeaninternal.server.type;

import io.ebean.config.dbplatform.ExtraDbTypes;
import io.ebean.types.Inet;

import java.sql.SQLException;
import java.sql.Types;

/**
 * ScalarType for Inet to Varchar or Postgres INET.
 */
public abstract class ScalarTypeInet extends ScalarTypeBaseVarchar<Inet> {

  ScalarTypeInet() {
    super(Inet.class, false, ExtraDbTypes.INET);
  }

  @Override
  public abstract void bind(DataBind b, Inet value) throws SQLException;

  @Override
  public Inet convertFromDbString(String dbValue) {
    return parse(dbValue);
  }

  @Override
  public String convertToDbString(Inet beanValue) {
    return formatValue(beanValue);
  }

  @Override
  public String formatValue(Inet value) {
    return value.getAddress();
  }

  @Override
  public Inet parse(String value) {
    return new Inet(value);
  }

  /**
   * Inet to Varchar.
   */
  public static class Varchar extends ScalarTypeInet {

    @Override
    public void bind(DataBind b, Inet value) throws SQLException {
      if (value == null) {
        b.setNull(Types.VARCHAR);
      } else {
        b.setString(convertToDbString(value));
      }
    }
  }

  /**
   * Inet to Postgres INET.
   */
  public static class Postgres extends ScalarTypeInet {

    @Override
    public void bind(DataBind b, Inet value) throws SQLException {
      if (value == null) {
        b.setNull(Types.OTHER);
      } else {
        String strValue = convertToDbString(value);
        b.setObject(PostgresHelper.asInet(strValue));
      }
    }
  }
}
