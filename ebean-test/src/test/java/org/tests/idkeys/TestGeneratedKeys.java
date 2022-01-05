package org.tests.idkeys;

import io.ebean.BaseTestCase;
import io.ebean.Transaction;
import io.ebean.annotation.ForPlatform;
import io.ebean.annotation.Platform;
import io.ebean.config.dbplatform.IdType;
import io.ebeaninternal.api.SpiEbeanServer;
import org.junit.jupiter.api.Test;
import org.tests.idkeys.db.GenKeyIdentity;
import org.tests.idkeys.db.GenKeySeqA;
import org.tests.idkeys.db.GenKeySeqB;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

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
    String sql;
    switch (spiEbeanServer().databasePlatform().getPlatform().base()) {
      case H2 :
        sql = "select currval('" + sequence + "')";
        break;

      case DB2 :
        sql = "values previous value for " + sequence;

        break;
      case SQLSERVER :
        sql = "select current_value from sys.sequences where name = '" + sequence + "'";
        break;

      case MARIADB :
        throw new UnsupportedOperationException("reading sequence value outside of the current connection is not supported. "
            + "See https://mariadb.com/kb/en/previous-value-for-sequence_name/#description");
        
      default :
        throw new UnsupportedOperationException("reading sequence value from "
            + spiEbeanServer().databasePlatform().getPlatform()
            + " is not supported.");

    }
    try (Statement stm = tx.connection().createStatement()) {
      ResultSet rs = stm.executeQuery(sql);
      rs.next();
      return rs.getLong(1);
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
  
  @Test
  @ForPlatform({Platform.H2, Platform.MARIADB, Platform.SQLSERVER, Platform.DB2})
  public void testGeneratedKeys() throws SQLException {
    assumeTrue(idType() == IdType.SEQUENCE);

    SpiEbeanServer server = spiEbeanServer();
    List<Long> idList = new ArrayList<>(52);

    try (Transaction tx = server.beginTransaction()) {
      // bigger than increment
      for (int i = 1; i < 52; i++) {
        GenKeySeqA gks = new GenKeySeqA();
        gks.setDescription("my description " + i);
        server.save(gks);
        assertFalse(idList.contains(gks.getId()));
        idList.add(gks.getId());
      }
    }
  }

}
