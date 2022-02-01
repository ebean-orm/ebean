package io.ebeaninternal.server.persist;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Horrible DB2 hack.
 */
final class DB2GetKeys {

  private static Class<? extends PreparedStatement> DB2_PREPARED_STATEMENT;
  private static Method GET_DB_GENERATED_KEYS;

  static {
    try {
      DB2_PREPARED_STATEMENT = (Class<? extends PreparedStatement>) Class.forName("com.ibm.db2.jcc.DB2PreparedStatement");
      GET_DB_GENERATED_KEYS = DB2_PREPARED_STATEMENT.getDeclaredMethod("getDBGeneratedKeys");
    } catch (ClassNotFoundException | NoSuchMethodException | SecurityException cnf) {
      // NOP
    }
  }

  static boolean getGeneratedKeys(PreparedStatement pstmt, List<BatchPostExecute> list) throws SQLException {
    if (DB2_PREPARED_STATEMENT != null) {
      PreparedStatement db2Stmt = null;
      if (DB2_PREPARED_STATEMENT.isInstance(pstmt)) {
        db2Stmt = pstmt;
      } else if (pstmt.isWrapperFor(DB2_PREPARED_STATEMENT)) {
        db2Stmt = pstmt.unwrap(DB2_PREPARED_STATEMENT);
      }
      if (db2Stmt != null) {
        // WTF: https://stackoverflow.com/questions/41725492/how-to-get-auto-generated-keys-of-batch-insert-statement
        ResultSet[] result;
        try {
          result = (ResultSet[]) GET_DB_GENERATED_KEYS.invoke(db2Stmt);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
          throw new SQLException("Could not get generated keys for DB2", e);
        }
        int index = 0;
        for (ResultSet resultSet : result) {
          while (resultSet.next()) {
            Object idValue = resultSet.getObject(1);
            list.get(index++).setGeneratedKey(idValue);
          }
        }
        return true;
      }
    }
    return false;
  }
}
