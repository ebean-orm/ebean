package org.tests.idkeys;

import io.ebean.BaseTestCase;
import io.ebean.Transaction;
import io.ebean.annotation.ForPlatform;
import io.ebean.annotation.Platform;
import io.ebean.config.dbplatform.IdType;
import io.ebeaninternal.api.SpiEbeanServer;
import org.junit.Test;
import org.tests.idkeys.db.GenKeyIdentity;
import org.tests.idkeys.db.GenKeySeqA;
import org.tests.idkeys.db.GenKeySeqB;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assume.assumeTrue;

public class TestGeneratedKeys extends BaseTestCase {

  @Test
  @ForPlatform(Platform.H2) // readSequenceValue is H2 specific
  public void testGenKeySeqA() throws SQLException {
    assumeTrue(idType() == IdType.SEQUENCE);
    SpiEbeanServer server = spiEbeanServer();

    try (Transaction tx = server.beginTransaction()) {

      long sequenceStart = readSequenceValue(tx, GenKeySeqA.SEQUENCE_NAME);

      GenKeySeqA al = new GenKeySeqA();
      al.setDescription("my description");
      server.save(al);

      long sequenceCurrent = readSequenceValue(tx, GenKeySeqA.SEQUENCE_NAME);

      assertNotNull(al.getId());
      assertFalse(sequenceStart == sequenceCurrent);
      assertEquals(sequenceStart + 20, sequenceCurrent);
    }

  }

  @Test
  @ForPlatform(Platform.H2) // readSequenceValue is H2 specific
  public void testGenKeySeqB() throws SQLException {
    assumeTrue(idType() == IdType.SEQUENCE);
    SpiEbeanServer server = spiEbeanServer();

    try (Transaction tx = server.beginTransaction()) {

      long sequenceStart = readSequenceValue(tx, GenKeySeqB.SEQUENCE_NAME);

      GenKeySeqB al = new GenKeySeqB();
      al.setDescription("my description");
      server.save(al);

      long sequenceCurrent = readSequenceValue(tx, GenKeySeqB.SEQUENCE_NAME);

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

    if (idType() != IdType.IDENTITY) {
      // only run this test when SEQUENCE is being used
      return;
    }

    try (Transaction tx = server().beginTransaction()) {

      GenKeyIdentity al = new GenKeyIdentity();
      al.setDescription("my description");
      server().save(al);

      // For JDBC batching we won't get the id until after
      // the batch has been flushed explicitly or via commit
      //assertNotNull(al.getId());

      tx.commit();

      assertNotNull(al.getId());
    }
  }

}
