package io.ebean.config;

import org.junit.Test;

import static org.assertj.core.api.StrictAssertions.assertThat;

public class MatchingNamingConventionTest {

  private MatchingNamingConvention namingConvention = new MatchingNamingConvention();

  @Test
  public void getColumnFromProperty() throws Exception {

    String fkCol = "bridgetab_userId";
    String col = namingConvention.getColumnFromProperty(null, fkCol);
    assertThat(col).isEqualTo(fkCol);
  }

  @Test
  public void getForeignKey() {

    String fk = namingConvention.getForeignKey("billingAddress", "id");
    assertThat(fk).isEqualTo("billingAddressId");

    fk = namingConvention.getForeignKey("billingAddress", "remoteIdProperty");
    assertThat(fk).isEqualTo("billingAddressRemoteIdProperty");
  }
}
