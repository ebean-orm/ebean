package io.ebeaninternal.server.type;

import io.ebean.config.dbplatform.ExtraDbTypes;
import io.ebean.types.Cidr;

import java.sql.SQLException;
import java.sql.Types;

/**
 * ScalarType for Cidr to Varchar or Postgres CIDR.
 */
public abstract class ScalarTypeCidr extends ScalarTypeBaseVarchar<Cidr> {

  ScalarTypeCidr() {
    super(Cidr.class, false, ExtraDbTypes.CIDR);
  }

  @Override
  public abstract void bind(DataBind b, Cidr value) throws SQLException;

  @Override
  public Cidr convertFromDbString(String dbValue) {
    return parse(dbValue);
  }

  @Override
  public String convertToDbString(Cidr beanValue) {
    return formatValue(beanValue);
  }

  @Override
  public String formatValue(Cidr value) {
    return value.getAddress();
  }

  @Override
  public Cidr parse(String value) {
    return new Cidr(value);
  }

  /**
   * Cidr to Varchar.
   */
  public static class Varchar extends ScalarTypeCidr {

    @Override
    public void bind(DataBind b, Cidr value) throws SQLException {
      if (value == null) {
        b.setNull(Types.VARCHAR);
      } else {
        b.setString(convertToDbString(value));
      }
    }
  }

  /**
   * Cidr to Postgres CIDR.
   */
  public static class Postgres extends ScalarTypeCidr {

    @Override
    public void bind(DataBind b, Cidr value) throws SQLException {
      if (value == null) {
        b.setNull(Types.OTHER);
      } else {
        String strValue = convertToDbString(value);
        b.setObject(PostgresHelper.asInet(strValue));
      }
    }
  }
}
