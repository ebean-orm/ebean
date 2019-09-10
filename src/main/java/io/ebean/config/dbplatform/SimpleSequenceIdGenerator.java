package io.ebean.config.dbplatform;

import io.ebean.Transaction;
import io.ebean.util.JdbcClose;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.PersistenceException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * A very simple Database sequence based IdGenerator.
 * <p>
 * One which batch requests sequence Id's would be better for performance.
 * </p>
 */
public class SimpleSequenceIdGenerator implements PlatformIdGenerator {

  private static final Logger logger = LoggerFactory.getLogger(SimpleSequenceIdGenerator.class);

  private final String sql;

  private final DataSource dataSource;

  private final String seqName;

  /**
   * Construct given a dataSource and sql to return the next sequence value.
   */
  public SimpleSequenceIdGenerator(DataSource dataSource, String sql, String seqName) {
    this.dataSource = dataSource;
    this.sql = sql;
    this.seqName = seqName;
  }

  @Override
  public String getName() {
    return seqName;
  }

  @Override
  public boolean isDbSequence() {
    return true;
  }

  @Override
  public void preAllocateIds(int batchSize) {
    // just ignore this
  }

  @Override
  public Object nextId(Transaction t) {

    boolean useTxnConnection = t != null;

    Connection c = null;
    PreparedStatement pstmt = null;
    ResultSet rset = null;
    try {
      c = useTxnConnection ? t.getConnection() : dataSource.getConnection();
      pstmt = c.prepareStatement(sql);
      rset = pstmt.executeQuery();
      if (rset.next()) {
        return rset.getInt(1);
      } else {
        String m = "Always expecting 1 row from " + sql;
        throw new PersistenceException(m);
      }
    } catch (SQLException e) {
      throw new PersistenceException("Error getting sequence nextval", e);

    } finally {
      if (useTxnConnection) {
        closeResources(rset, pstmt, null);
      } else {
        closeResources(rset, pstmt, c);
      }
    }
  }

  private void closeResources(ResultSet rset, PreparedStatement pstmt, Connection c) {
    JdbcClose.close(rset);
    JdbcClose.close(pstmt);
    JdbcClose.close(c);
  }

}
