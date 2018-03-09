package io.ebean.config;

import io.ebean.config.dbplatform.h2.H2Platform;
import io.ebean.config.dbplatform.sqlserver.SqlServer17Platform;
import org.junit.Test;

import static org.assertj.core.api.StrictAssertions.assertThat;

public class MatchingNamingConventionTest {

  private MatchingNamingConvention namingConvention;

  public MatchingNamingConventionTest() {
    this.namingConvention = new MatchingNamingConvention();
    this.namingConvention.setDatabasePlatform(new H2Platform());
  }

  @Test
  public void getColumnFromProperty_when_allQuoted() {

    SqlServer17Platform platform = new SqlServer17Platform();

    PlatformConfig config = new PlatformConfig();
    config.setAllQuotedIdentifiers(true);
    platform.configure(config);

    NamingConvention nc = new MatchingNamingConvention();
    nc.setDatabasePlatform(platform);

    assertThat(nc.getColumnFromProperty(null, "bridgetabUserId")).isEqualTo("[bridgetabUserId]");
    assertThat(nc.getColumnFromProperty(null, "order")).isEqualTo("[order]");
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
