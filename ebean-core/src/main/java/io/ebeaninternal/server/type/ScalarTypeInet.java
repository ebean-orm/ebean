package io.ebeaninternal.server.type;

import io.ebean.config.dbplatform.ExtraDbTypes;
import io.ebean.core.type.DataBinder;
import io.ebean.types.Inet;

import java.sql.SQLException;
import java.sql.Types;

/**
 * ScalarType for Inet to Varchar or Postgres INET.
 */
public abstract class ScalarTypeInet extends ScalarTypeBaseVarchar<Inet> {

  ScalarTypeInet(int jdbcType) {
    super(Inet.class, false, jdbcType);
  }

  @Override
  public abstract void bind(DataBinder binder, Inet value) throws SQLException;

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

    Varchar() {
      super(Types.VARCHAR);
    }

    @Override
    public void bind(DataBinder binder, Inet value) throws SQLException {
      if (value == null) {
        binder.setNull(Types.VARCHAR);
      } else {
        binder.setString(convertToDbString(value));
      }
    }
  }

  /**
   * Inet to Postgres INET.
   */
  public static class Postgres extends ScalarTypeInet {

    Postgres() {
      super(ExtraDbTypes.INET);
    }

    @Override
    public void bind(DataBinder binder, Inet value) throws SQLException {
      if (value == null) {
        binder.setNull(Types.OTHER);
      } else {
        String strValue = convertToDbString(value);
        binder.setObject(PostgresHelper.asInet(strValue));
      }
    }
  }
}
