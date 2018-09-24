package io.ebeaninternal.server.persist.dml;

import io.ebean.bean.EntityBean;
import io.ebean.util.JdbcClose;
import io.ebeaninternal.api.SpiTransaction;
import io.ebeaninternal.server.core.Message;
import io.ebeaninternal.server.core.PersistRequestBean;
import io.ebeaninternal.server.deploy.BeanDescriptor;
import io.ebeaninternal.server.persist.DmlUtil;

import javax.persistence.OptimisticLockException;
import javax.persistence.PersistenceException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Insert bean handler.
 */
public class InsertHandler extends DmlHandler {

  /**
   * The associated InsertMeta data.
   */
  private final InsertMeta meta;

  /**
   * Set to true when the key is concatenated.
   */
  private final boolean concatinatedKey;

  /**
   * Flag set when using getGeneratedKeys.
   */
  private boolean useGeneratedKeys;

  /**
   * A SQL Select used to fetch back the Id where generatedKeys is not
   * supported.
   */
  private String selectLastInsertedId;

  /**
   * Create to handle the insert execution.
   */
  public InsertHandler(PersistRequestBean<?> persist, InsertMeta meta) {
    super(persist, meta.isEmptyStringToNull());
    this.meta = meta;
    this.concatinatedKey = meta.isConcatenatedKey();
  }

  @Override
  public boolean isUpdate() {
    return false;
  }

  /**
   * Generate and bind the insert statement.
   */
  @Override
  public void bind() throws SQLException {

    BeanDescriptor<?> desc = persistRequest.getBeanDescriptor();
    EntityBean bean = persistRequest.getEntityBean();

    Object idValue = desc.getId(bean);

    boolean withId = !DmlUtil.isNullOrZero(idValue);

    // check to see if we are going to use generated keys
    if (!withId) {
      if (concatinatedKey) {
        // expecting a concatenated key that can
        // be built from supplied AssocOne beans
        withId = meta.deriveConcatenatedId(persistRequest);
      } else if (meta.supportsGetGeneratedKeys()) {
        // Identity with getGeneratedKeys
        useGeneratedKeys = true;
      } else {
        // use a query to get the last inserted id
        selectLastInsertedId = meta.getSelectLastInsertedId();
      }
    }

    SpiTransaction t = persistRequest.getTransaction();

    // get the appropriate sql
    sql = meta.getSql(withId, persistRequest.isPublish());

    PreparedStatement pstmt;
    if (persistRequest.isBatched()) {
      pstmt = getPstmt(t, sql, persistRequest, useGeneratedKeys);
    } else {
      pstmt = getPstmt(t, sql, useGeneratedKeys);
    }
    dataBind = bind(pstmt);
    meta.bind(this, bean, withId, persistRequest.isPublish());
    if (persistRequest.isBatched()) {
      batchedPstmt.registerInputStreams(dataBind.getInputStreams());
    }
    logSql(sql);
  }

  /**
   * Check with useGeneratedKeys to get appropriate PreparedStatement.
   */
  @Override
  protected PreparedStatement getPstmt(SpiTransaction t, String sql, boolean useGeneratedKeys) throws SQLException {
    Connection conn = t.getInternalConnection();
    if (useGeneratedKeys) {
      return conn.prepareStatement(sql, meta.getIdentityDbColumns());

    } else {
      return conn.prepareStatement(sql);
    }
  }

  /**
   * Execute the insert in a normal non batch fashion. Additionally using
   * getGeneratedKeys if required.
   */
  @Override
  public int execute() throws SQLException, OptimisticLockException {
    int rowCount = dataBind.executeUpdate();
    if (useGeneratedKeys) {
      // get the auto-increment value back and set into the bean
      getGeneratedKeys();

    } else if (selectLastInsertedId != null) {
      // fetch back the Id using a query
      fetchGeneratedKeyUsingSelect();
    }

    checkRowCount(rowCount);
    return rowCount;
  }

  /**
   * For non batch insert with generated keys.
   */
  private void getGeneratedKeys() throws SQLException {

    ResultSet rset = dataBind.getPstmt().getGeneratedKeys();
    try {
      setGeneratedKey(rset);
    } finally {
      JdbcClose.close(rset);
    }
  }

  private void setGeneratedKey(ResultSet rset) throws SQLException {
    if (rset.next()) {
      Object idValue = rset.getObject(1);
      if (idValue != null) {
        persistRequest.setGeneratedKey(idValue);
      }

    } else {
      throw new PersistenceException(Message.msg("persist.autoinc.norows"));
    }
  }

  /**
   * For non batch insert with DBs that do not support getGeneratedKeys. Use a
   * SQL select to fetch back the Id value.
   */
  private void fetchGeneratedKeyUsingSelect() throws SQLException {

    Connection conn = transaction.getConnection();

    PreparedStatement stmt = null;
    ResultSet rset = null;
    try {
      stmt = conn.prepareStatement(selectLastInsertedId);
      rset = stmt.executeQuery();
      setGeneratedKey(rset);
    } finally {
      JdbcClose.close(rset);
      JdbcClose.close(stmt);
    }
  }

}
