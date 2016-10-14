package com.avaje.ebean.config;

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

}