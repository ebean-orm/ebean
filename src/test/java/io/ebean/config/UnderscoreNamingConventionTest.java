package io.ebean.config;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class UnderscoreNamingConventionTest {

  private UnderscoreNamingConvention namingConvention = new UnderscoreNamingConvention();

  @Test
  public void getColumnFromProperty() throws Exception {

    String fkCol = "bridgetab_user_id";

    String col = namingConvention.getColumnFromProperty(null, fkCol);
    assertThat(col).isEqualTo(fkCol);
  }

  @Test
  public void getForeignKey() {

    String fk = namingConvention.getForeignKey("billing_address", "id");
    assertThat(fk).isEqualTo("billing_address_id");

    fk = namingConvention.getForeignKey("billing_address", "remoteIdProperty");
    assertThat(fk).isEqualTo("billing_address_remote_id_property");
  }
}
