package io.ebeaninternal.server.type;

import io.ebean.config.dbplatform.ExtraDbTypes;
import io.ebean.text.TextException;

import java.net.InetAddress;
import java.sql.SQLException;
import java.sql.Types;

/**
 * ScalarType for InetAddress to Postgres INET.
 */
public class ScalarTypeInetAddressPostgres extends ScalarTypeBaseVarchar<InetAddress> {

  public ScalarTypeInetAddressPostgres() {
    super(InetAddress.class, false, ExtraDbTypes.INET);
  }

  @Override
  public void bind(DataBind b, InetAddress value) throws SQLException {
    if (value == null) {
      b.setNull(Types.OTHER);
    } else {
      String strValue = convertToDbString(value);
      b.setObject(PostgresHelper.asInet(strValue));
    }
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
    return formatValue(beanValue);
  }

  @Override
  public String formatValue(InetAddress v) {
    return ConvertInetAddresses.toHostAddress(v);
  }

  @Override
  public InetAddress parse(String value) {
    try {
      return ConvertInetAddresses.fromHost(value);
    } catch (IllegalArgumentException e) {
      throw new TextException("Error with InetAddresses [{}]", value, e);
    }
  }

}
