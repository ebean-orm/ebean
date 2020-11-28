package io.ebean.config;

import io.ebean.config.dbplatform.h2.H2Platform;
import io.ebean.config.dbplatform.sqlserver.SqlServer17Platform;
import org.junit.Test;
import org.tests.model.basic.Customer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class MatchingNamingConventionTest {

  private MatchingNamingConvention namingConvention;

  public MatchingNamingConventionTest() {
    this.namingConvention = new MatchingNamingConvention();
    this.namingConvention.setDatabasePlatform(new H2Platform());
  }

  private MatchingNamingConvention createMatchingNamingConventionAllQuoted() {
    SqlServer17Platform platform = new SqlServer17Platform();

    PlatformConfig config = new PlatformConfig();
    config.setAllQuotedIdentifiers(true);
    platform.configure(config);

    MatchingNamingConvention nc = new MatchingNamingConvention();
    nc.setDatabasePlatform(platform);
    return nc;
  }

  @Test
  public void getColumnFromProperty_when_allQuoted() {

    MatchingNamingConvention nc = createMatchingNamingConventionAllQuoted();

    assertThat(nc.getColumnFromProperty(null, "bridgetabUserId")).isEqualTo("[bridgetabUserId]");
    assertThat(nc.getColumnFromProperty(null, "order")).isEqualTo("[order]");
  }

  @Test
  public void getTableNameByConvention_when_allQuoted() {

    MatchingNamingConvention nc = createMatchingNamingConventionAllQuoted();

    final TableName tableName = nc.getTableNameByConvention(Customer.class);
    assertEquals("[Customer]", tableName.getName());
    assertNull(tableName.getCatalog());
    assertNull(tableName.getSchema());
  }


  @Test
  public void getSequenceName() {
    MatchingNamingConvention nc = createMatchingNamingConventionAllQuoted();
    assertEquals("Customer_seq", nc.getSequenceName("[Customer]", null));
  }

  @Test
  public void getColumnFromProperty() {

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
