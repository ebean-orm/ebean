package io.ebean.config;

import io.ebean.config.dbplatform.h2.H2Platform;
import io.ebean.config.dbplatform.sqlserver.SqlServer17Platform;
import org.junit.Test;
import org.tests.model.basic.Customer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class MatchingNamingConventionTest {

  private final MatchingNamingConvention namingConvention;

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
  public void getTableName() {
    assertThat(namingConvention.getTableName("a", "b", "c")).isEqualTo("a.b.c");
    assertThat(namingConvention.getTableName("", "b", "c")).isEqualTo("b.c");
    assertThat(namingConvention.getTableName("", "", "c")).isEqualTo("c");
    assertThat(namingConvention.getTableName("a", "", "c")).isEqualTo("a.c");
  }

  @Test
  public void getTableName_when_allQuoted() {
    MatchingNamingConvention nc = createMatchingNamingConventionAllQuoted();

    assertThat(nc.getTableName("a", "b", "c")).isEqualTo("[a].[b].[c]");
    assertThat(nc.getTableName("", "b", "c")).isEqualTo("[b].[c]");
    assertThat(nc.getTableName("", "", "c")).isEqualTo("[c]");
    assertThat(nc.getTableName("a", "", "c")).isEqualTo("[a].[c]");
  }

  @Test
  public void getM2MJoinTableName() {
    TableName t0 = new TableName("One");
    TableName t1 = new TableName("Two");
    assertThat(namingConvention.getM2MJoinTableName(t0, t1).toString()).isEqualTo("One_Two");
  }

  @Test
  public void getM2MJoinTableName_when_allQuoted() {
    MatchingNamingConvention nc = createMatchingNamingConventionAllQuoted();
    TableName t0 = new TableName("[One]");
    TableName t1 = new TableName("[Two]");
    assertThat(nc.getM2MJoinTableName(t0, t1).toString()).isEqualTo("[One_Two]");
  }

  @Test
  public void deriveM2MColumn() {
    assertThat(namingConvention.deriveM2MColumn("One", "Two")).isEqualTo("One_Two");
  }

  @Test
  public void deriveM2MColumn_when_allQuoted() {
    MatchingNamingConvention nc = createMatchingNamingConventionAllQuoted();
    assertThat(nc.deriveM2MColumn("[One]", "[Two]")).isEqualTo("[One_Two]");
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
    assertThat(nc.getSequenceName("[Customer]", null)).isEqualTo("[Customer_seq]");
  }

  @Test
  public void getSequenceName_when_quotedSchema() {
    MatchingNamingConvention nc = createMatchingNamingConventionAllQuoted();
    assertThat(nc.getSequenceName("[dbo].[Customer]", null)).isEqualTo("[dbo].[Customer_seq]");
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
