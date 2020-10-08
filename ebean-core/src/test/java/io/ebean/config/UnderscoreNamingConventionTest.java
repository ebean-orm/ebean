package io.ebean.config;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class UnderscoreNamingConventionTest {

  private UnderscoreNamingConvention namingConvention = new UnderscoreNamingConvention();

  @Test
  public void simple() {
    String col = namingConvention.getColumnFromProperty(null, "helloThere");
    assertThat(col).isEqualTo("hello_there");

    assertThat(namingConvention.toCamelFromUnderscore(col)).isEqualTo("helloThere");
  }

  @Test
  public void with_suffix_Id() {
    String col = namingConvention.getColumnFromProperty(null, "helloId");
    assertThat(col).isEqualTo("hello_id");

    assertThat(namingConvention.toCamelFromUnderscore(col)).isEqualTo("helloId");

    col = namingConvention.getColumnFromProperty(null, "helloThereId");
    assertThat(col).isEqualTo("hello_there_id");

    assertThat(namingConvention.toCamelFromUnderscore(col)).isEqualTo("helloThereId");
  }

  @Test
  public void with_suffix_ID() {
    String col = namingConvention.getColumnFromProperty(null, "helloID");
    assertThat(col).isEqualTo("hello_id");

    assertThat(namingConvention.toCamelFromUnderscore(col)).isEqualTo("helloId");

    col = namingConvention.getColumnFromProperty(null, "helloThereID");
    assertThat(col).isEqualTo("hello_there_id");

    assertThat(namingConvention.toCamelFromUnderscore(col)).isEqualTo("helloThereId");
  }

  @Test
  public void getColumnFromProperty() {

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
