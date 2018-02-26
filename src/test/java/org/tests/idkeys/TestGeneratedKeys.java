package org.tests.idkeys;

import io.ebean.BaseTestCase;
import io.ebean.Transaction;
import io.ebean.annotation.ForPlatform;
import io.ebean.annotation.Platform;
import io.ebean.config.dbplatform.IdType;
import io.ebeaninternal.api.SpiEbeanServer;
import org.junit.Test;
import org.tests.idkeys.db.GenKeyIdentity;
import org.tests.idkeys.db.GenKeySequence;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

public class TestGeneratedKeys extends BaseTestCase {

  @Test
  @ForPlatform(Platform.H2) // readSequenceValue is H2 specific
  public void testSequence() throws SQLException {
    SpiEbeanServer server = spiEbeanServer();
    IdType idType = server.getDatabasePlatform().getDbIdentity().getIdType();
    if (!IdType.SEQUENCE.equals(idType)) {
      // only run this test when SEQUENCE is being used
      return;
    }

    try (Transaction tx = server.beginTransaction()) {

      long sequenceStart = readSequenceValue(tx, GenKeySequence.SEQUENCE_NAME);

      GenKeySequence al = new GenKeySequence();
      al.setDescription("my description");
      server.save(al);


      long sequenceCurrent = readSequenceValue(tx, GenKeySequence.SEQUENCE_NAME);

      assertNotNull(al.getId());
      assertFalse(sequenceStart == sequenceCurrent);
      assertEquals(sequenceStart + 20, sequenceCurrent);
    }

  }

  private long readSequenceValue(Transaction tx, String sequence) throws SQLException {
    Statement stm = null;
    try {
      stm = tx.getConnection().createStatement();
      ResultSet rs = stm.executeQuery("select currval('" + sequence + "')");
      rs.next();

      return rs.getLong(1);
    } finally {
      if (stm != null) {
        try {
          stm.close();
        } catch (SQLException e) {
        }
      }
    }
  }

  @Test
  public void testIdentity() throws SQLException {

    SpiEbeanServer server = spiEbeanServer();
    IdType idType = server.getDatabasePlatform().getDbIdentity().getIdType();

    if (!IdType.IDENTITY.equals(idType)) {
      // only run this test when SEQUENCE is being used
      return;
    }

    try (Transaction tx = server.beginTransaction()) {

      GenKeyIdentity al = new GenKeyIdentity();
      al.setDescription("my description");
      server.save(al);

      // For JDBC batching we won't get the id until after
      // the batch has been flushed explicitly or via commit
      //assertNotNull(al.getId());

      tx.commit();

      assertNotNull(al.getId());
    }
  }

}
