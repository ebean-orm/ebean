package io.ebeaninternal.extraddl.model;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ExtraDdlXmlReaderTest {

  @Test
  public void read(){

    ExtraDdl read = ExtraDdlXmlReader.read();
    assertNotNull(read);
  }

  @Test
  public void buildExtra_when_h2() {

    String ddl = ExtraDdlXmlReader.buildExtra("h2", false);

    assertThat(ddl).contains("create or replace view order_agg_vw");
    assertThat(ddl).contains("-- h2 and postgres script");
    assertThat(ddl).doesNotContain(" -- oracle only script");
  }

  @Test
  public void buildExtra_when_oracle() {

    String ddl = ExtraDdlXmlReader.buildExtra("oracle", false);

    assertThat(ddl).contains("create or replace view order_agg_vw");
    assertThat(ddl).doesNotContain("-- h2 and postgres script");
    assertThat(ddl).contains(" -- oracle only script");
  }

  @Test
  public void buildExtra_when_mysql() {

    String ddl = ExtraDdlXmlReader.buildExtra("mysql", false);

    assertThat(ddl).contains("create or replace view order_agg_vw");
    assertThat(ddl).doesNotContain("-- h2 and postgres script");
    assertThat(ddl).doesNotContain(" -- oracle only script");
  }

  @Test
  public void matchPlatform() {

    assertTrue(ExtraDdlXmlReader.matchPlatform("h2", "h2"));
    assertTrue(ExtraDdlXmlReader.matchPlatform("h2", "mysql,h2"));
    assertTrue(ExtraDdlXmlReader.matchPlatform("h2", "mysql,h2,"));
    assertTrue(ExtraDdlXmlReader.matchPlatform("h2", "mysql , h2 ,"));
    assertTrue(ExtraDdlXmlReader.matchPlatform("h2", "mysql , h2, oracle"));
    assertTrue(ExtraDdlXmlReader.matchPlatform("h2", "mysql , h2, oracle"));

    assertTrue(ExtraDdlXmlReader.matchPlatform("sqlserver17", "sqlserver17"));
    assertTrue(ExtraDdlXmlReader.matchPlatform("sqlserver16", "sqlserver16"));
  }

  @Test
  public void matchPlatform_sqlserver17_matchAlsoToGenericName() {

    assertTrue(ExtraDdlXmlReader.matchPlatform("sqlserver17", "sqlserver"));
  }

  @Test
  public void matchPlatform_sqlserver16_matchAlsoToGenericName() {

    assertTrue(ExtraDdlXmlReader.matchPlatform("sqlserver16", "sqlserver"));
  }

  @Test
  public void matchPlatform_sqlserver_nonMatch() {

    assertFalse(ExtraDdlXmlReader.matchPlatform("sqlserver16", "sqlserver17"));
    assertFalse(ExtraDdlXmlReader.matchPlatform("sqlserver17", "sqlserver16"));
  }

}
