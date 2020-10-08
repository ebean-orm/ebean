package io.ebeaninternal.api;

import io.ebean.annotation.Platform;
import org.junit.Test;

import static org.junit.Assert.*;

public class PlatformMatchTest {


  @Test
  public void match() {

    assertTrue(PlatformMatch.matchPlatform(Platform.H2, "h2"));
    assertTrue(PlatformMatch.matchPlatform(Platform.H2, "mysql,h2"));
    assertTrue(PlatformMatch.matchPlatform(Platform.H2, "mysql,h2,"));
    assertTrue(PlatformMatch.matchPlatform(Platform.H2, "mysql , h2 ,"));
    assertTrue(PlatformMatch.matchPlatform(Platform.H2, "mysql , h2, oracle"));
    assertTrue(PlatformMatch.matchPlatform(Platform.H2, "mysql , h2, oracle"));

    assertTrue(PlatformMatch.matchPlatform(Platform.SQLSERVER, "sqlserver"));
    assertTrue(PlatformMatch.matchPlatform(Platform.SQLSERVER17, "sqlserver"));
    assertTrue(PlatformMatch.matchPlatform(Platform.SQLSERVER16, "sqlserver"));

    assertTrue(PlatformMatch.matchPlatform(Platform.POSTGRES, "postgres"));
    assertTrue(PlatformMatch.matchPlatform(Platform.POSTGRES9, "postgres"));
  }

  @Test
  public void match_sqlserver17_matchAlsoToGenericName() {
    assertTrue(PlatformMatch.matchPlatform(Platform.SQLSERVER17, "sqlserver"));
    assertTrue(PlatformMatch.matchPlatform(Platform.SQLSERVER17, "sqlserver17"));
  }

  @Test
  public void matchPlatform_sqlserver16_matchAlsoToGenericName() {
    assertTrue(PlatformMatch.matchPlatform(Platform.SQLSERVER16, "sqlserver"));
    assertTrue(PlatformMatch.matchPlatform(Platform.SQLSERVER16, "sqlserver16"));
  }

  @Test
  public void matchPlatform_sqlserver_nonMatch() {
    assertFalse(PlatformMatch.matchPlatform(Platform.SQLSERVER16, "sqlserver17"));
    assertFalse(PlatformMatch.matchPlatform(Platform.SQLSERVER17, "sqlserver16"));
    assertFalse(PlatformMatch.matchPlatform(Platform.SQLSERVER, "sqlserver16"));
    assertFalse(PlatformMatch.matchPlatform(Platform.SQLSERVER, "sqlserver17"));
  }
}
