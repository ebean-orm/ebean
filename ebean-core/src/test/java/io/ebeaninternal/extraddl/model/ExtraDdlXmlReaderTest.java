package io.ebeaninternal.extraddl.model;

import io.ebean.annotation.Platform;
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

    String ddl = ExtraDdlXmlReader.buildExtra(Platform.H2, false);

    assertThat(ddl).contains("create or replace view order_agg_vw");
    assertThat(ddl).contains("-- h2 and postgres script");
    assertThat(ddl).doesNotContain(" -- oracle only script");
  }

  @Test
  public void buildExtra_when_oracle() {

    String ddl = ExtraDdlXmlReader.buildExtra(Platform.ORACLE, false);

    assertThat(ddl).contains("create or replace view order_agg_vw");
    assertThat(ddl).doesNotContain("-- h2 and postgres script");
    assertThat(ddl).contains(" -- oracle only script");
  }

  @Test
  public void buildExtra_when_mysql() {

    String ddl = ExtraDdlXmlReader.buildExtra(Platform.MYSQL, false);

    assertThat(ddl).contains("create or replace view order_agg_vw");
    assertThat(ddl).doesNotContain("-- h2 and postgres script");
    assertThat(ddl).doesNotContain(" -- oracle only script");
  }

}
