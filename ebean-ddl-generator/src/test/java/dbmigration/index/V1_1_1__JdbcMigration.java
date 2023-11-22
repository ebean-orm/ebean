package dbmigration.index;

import io.ebean.migration.JdbcMigration;

import java.sql.Connection;

/**
 * @author Roland Praml, FOCONIS AG
 */
public class V1_1_1__JdbcMigration implements JdbcMigration {
  @Override
  public void migrate(Connection connection) {

  }

  @Override
  public int getChecksum() {
    return 42;
  }
}
