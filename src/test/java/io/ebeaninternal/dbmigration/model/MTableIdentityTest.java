package io.ebeaninternal.dbmigration.model;

import io.ebean.annotation.IdentityGenerated;
import io.ebean.config.dbplatform.IdType;
import io.ebeaninternal.dbmigration.migration.CreateTable;
import io.ebeaninternal.dbmigration.migration.IdentityType;
import io.ebeaninternal.server.deploy.IdentityMode;
import io.ebeaninternal.server.deploy.meta.DeployIdentityMode;
import org.junit.Test;

import java.math.BigInteger;

import static org.junit.Assert.*;

public class MTableIdentityTest {

  @Test
  public void toCreateTable() {

    IdentityMode mode = new IdentityMode(IdType.SEQUENCE, IdentityGenerated.BY_DEFAULT, 11, 12, 13, "foo_seq");
    CreateTable tab = new CreateTable();
    MTableIdentity.toCreateTable(mode, tab);

    assertEquals(BigInteger.valueOf(11), tab.getIdentityStart());
    assertEquals(BigInteger.valueOf(12), tab.getIdentityIncrement());
    assertEquals(BigInteger.valueOf(13), tab.getIdentityCache());
    assertEquals("foo_seq", tab.getSequenceName());
    assertEquals("by_default", tab.getIdentityGenerated());
    assertEquals(IdentityType.SEQUENCE, tab.getIdentityType());
  }

  @Test
  public void toCreateTable_when_auto() {

    IdentityMode mode = new IdentityMode(IdType.AUTO, IdentityGenerated.AUTO, 11, 12, 13, "foo_seq");
    CreateTable tab = new CreateTable();
    MTableIdentity.toCreateTable(mode, tab);

    assertNull(tab.getIdentityGenerated());
    assertNull(tab.getIdentityType());
  }

  @Test
  public void toCreateTable_when_PlatformDefault_expect_typeNull() {

    DeployIdentityMode deploy = DeployIdentityMode.auto();
    deploy.setPlatformType(IdType.SEQUENCE);

    CreateTable tab = new CreateTable();
    MTableIdentity.toCreateTable(new IdentityMode(deploy), tab);

    assertNull(tab.getIdentityType());
  }

  @Test
  public void toCreateTable_when_notPlatformDefault_expectType() {

    DeployIdentityMode deploy = DeployIdentityMode.auto();
    deploy.setIdType(IdType.SEQUENCE);

    CreateTable tab = new CreateTable();
    MTableIdentity.toCreateTable(new IdentityMode(deploy), tab);

    assertEquals(IdentityType.SEQUENCE, tab.getIdentityType());
  }


  @Test
  public void fromCreateTable_when_empty() {

    CreateTable tab = new CreateTable();
    final IdentityMode mode = MTableIdentity.fromCreateTable(tab);
    assertEquals(IdType.AUTO, mode.getIdType());
    assertEquals(IdentityGenerated.AUTO, mode.getGenerated());
    assertEquals(0, mode.getStart());
    assertEquals(0, mode.getIncrement());
    assertEquals(0, mode.getCache());
    assertNull(mode.getSequenceName());
  }

  @Test
  public void fromCreateTable_when_allSet() {

    CreateTable tab = new CreateTable();
    tab.setIdentityType(IdentityType.SEQUENCE);
    tab.setIdentityGenerated("always");
    tab.setSequenceName("foo_seq");
    tab.setIdentityStart(BigInteger.valueOf(5));
    tab.setIdentityIncrement(BigInteger.valueOf(7));
    tab.setIdentityCache(BigInteger.valueOf(9));

    final IdentityMode mode = MTableIdentity.fromCreateTable(tab);
    assertEquals(IdType.SEQUENCE, mode.getIdType());
    assertEquals(IdentityGenerated.ALWAYS, mode.getGenerated());
    assertEquals(5, mode.getStart());
    assertEquals(7, mode.getIncrement());
    assertEquals(9, mode.getCache());
    assertEquals("foo_seq", mode.getSequenceName());
  }

  @Test
  public void fromCreateTable_expect_useNewPropertiesWin() {

    CreateTable tab = new CreateTable();
    // old properties lose
    tab.setSequenceInitial(BigInteger.valueOf(42));
    tab.setSequenceAllocate(BigInteger.valueOf(43));

    // new properties win
    tab.setIdentityStart(BigInteger.valueOf(5));
    tab.setIdentityIncrement(BigInteger.valueOf(7));

    final IdentityMode mode = MTableIdentity.fromCreateTable(tab);
    assertEquals(5, mode.getStart());
    assertEquals(7, mode.getIncrement());
  }

  @Test
  public void fromCreateTable_when_seqInitAndSeqAll() {

    CreateTable tab = new CreateTable();
    tab.setSequenceInitial(BigInteger.valueOf(42));
    tab.setSequenceAllocate(BigInteger.valueOf(43));

    final IdentityMode mode = MTableIdentity.fromCreateTable(tab);
    assertEquals(42, mode.getStart());
    assertEquals(43, mode.getIncrement());
  }
}
