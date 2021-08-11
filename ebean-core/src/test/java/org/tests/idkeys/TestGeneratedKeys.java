package org.tests.idkeys;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assume.assumeTrue;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.junit.Test;
import org.tests.idkeys.db.GenKeyIdentity;
import org.tests.idkeys.db.GenKeySequence;

import io.ebean.BaseTestCase;
import io.ebean.Transaction;
import io.ebean.annotation.ForPlatform;
import io.ebean.annotation.Platform;
import io.ebean.config.dbplatform.IdType;
import io.ebeaninternal.api.SpiEbeanServer;

public class TestGeneratedKeys extends BaseTestCase {

  @Test
  @ForPlatform(Platform.H2)
  public void testSequence() throws SQLException {
    assumeTrue(idType() == IdType.SEQUENCE);

    SpiEbeanServer server = spiEbeanServer();

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
    try (Statement stm = tx.getConnection().createStatement()) {
      ResultSet rs;

      switch (spiEbeanServer().getDatabasePlatform().getPlatform()) {
        case H2 :
          rs = stm.executeQuery("select currval('" + sequence + "')");
          rs.next();
          return rs.getLong(1);

        case DB2 :
          rs = stm.executeQuery("values previous value for " + sequence);
          rs.next();
          return rs.getLong(1);

        case MARIADB :
          rs = stm.executeQuery("select previous value for " + sequence);
          rs.next();
          return rs.getLong(1);

        case SQLSERVER :
          rs = stm.executeQuery(
              "select current_value from sys.sequences where name = '"
                  + sequence + "'");
          rs.next();
          return rs.getLong(1);

        default :
          throw new UnsupportedOperationException("reading sequence value from "
              + spiEbeanServer().getDatabasePlatform().getPlatform()
              + " is not supported.");
      }
    }
  }

  @Test
  public void testIdentity() throws SQLException {
    assumeTrue(idType() == IdType.SEQUENCE);

    try (Transaction tx = server().beginTransaction()) {

      GenKeyIdentity al = new GenKeyIdentity();
      al.setDescription("my description");
      server().save(al);

      // For JDBC batching we won't get the id until after
      // the batch has been flushed explicitly or via commit
      // assertNotNull(al.getId());

      tx.commit();

      assertNotNull(al.getId());
    }
  }
  
  @Test
  @ForPlatform({Platform.H2, Platform.MARIADB, Platform.SQLSERVER, Platform.DB2})
  public void testGeneratedKeys() throws SQLException {
    assumeTrue(idType() == IdType.SEQUENCE);

    SpiEbeanServer server = spiEbeanServer();

    try (Transaction tx = server.beginTransaction()) {
      // bigger than increment
      for (int i = 1; i < 52; i++) {
        GenKeySequence gks = new GenKeySequence();
        gks.setDescription("my description " + i);
        server.save(gks);
        assertEquals(Long.valueOf(i), gks.getId());
      }
    }
  }

}
