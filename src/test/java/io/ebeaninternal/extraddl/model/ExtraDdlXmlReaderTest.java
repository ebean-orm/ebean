package io.ebeaninternal.extraddl.model;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;

public class ExtraDdlXmlReaderTest {

  @Test
  public void read() throws Exception {

    ExtraDdl read = ExtraDdlXmlReader.read("/extra-ddl.xml");
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

}
