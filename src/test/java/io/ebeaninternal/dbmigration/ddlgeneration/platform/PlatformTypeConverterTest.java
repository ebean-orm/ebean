package io.ebeaninternal.dbmigration.ddlgeneration.platform;

import io.ebean.config.dbplatform.DbPlatformTypeMapping;
import io.ebean.config.dbplatform.h2.H2Platform;
import io.ebean.config.dbplatform.postgres.PostgresPlatform;
import io.ebeaninternal.dbmigration.ddlgeneration.platform.util.PlatformTypeConverter;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class PlatformTypeConverterTest {

  @Test
  public void convert_withSuffix_expect_suffix() {

    PostgresPlatform pg = new PostgresPlatform();
    DbPlatformTypeMapping dbTypeMap = pg.getDbTypeMap();

    PlatformTypeConverter converter = new PlatformTypeConverter(dbTypeMap);

    assertThat(converter.convert("varchar(10)")).isEqualTo("varchar(10)");
    assertThat(converter.convert("VARCHAR(10) default 'en' not null")).isEqualTo("varchar(10) default 'en' not null");
    assertThat(converter.convert("DECIMAL(10,2) DEFAULT '0.00' NOT NULL")).isEqualTo("decimal(10,2) DEFAULT '0.00' NOT NULL");
    assertThat(converter.convert("CRAZY(12,0) suffix")).isEqualTo("CRAZY(12,0) suffix");
    assertThat(converter.convert("CRAZY(12) suffix")).isEqualTo("CRAZY(12) suffix");
    assertThat(converter.convert("CRAZY suffix")).isEqualTo("CRAZY suffix");
  }

  @Test
  public void testConvert_given_postgres() throws Exception {

    PostgresPlatform pg = new PostgresPlatform();
    DbPlatformTypeMapping dbTypeMap = pg.getDbTypeMap();

    PlatformTypeConverter converter = new PlatformTypeConverter(dbTypeMap);

    assertThat(converter.convert("varchar(10)")).isEqualTo("varchar(10)");
    assertThat(converter.convert("decimal(10,2)")).isEqualTo("decimal(10,2)");
    assertThat(converter.convert("clob")).isEqualTo("text");
    assertThat(converter.convert("blob")).isEqualTo("bytea");
    assertThat(converter.convert("tinyint")).isEqualTo("smallint");
    assertThat(converter.convert("funky")).isEqualTo("funky"); // unknown
    assertThat(converter.convert("integer(8)")).isEqualTo("integer"); // removes scale

    assertThat(converter.convert("funky(")).isEqualTo("funky(");
    assertThat(converter.convert("funky()")).isEqualTo("funky()");
    assertThat(converter.convert("funky)")).isEqualTo("funky)");
  }

  @Test
  public void testConvert_given_h2() throws Exception {


    H2Platform platform = new H2Platform();
    DbPlatformTypeMapping dbTypeMap = platform.getDbTypeMap();

    PlatformTypeConverter converter = new PlatformTypeConverter(dbTypeMap);

    assertThat(converter.convert("varchar(10)")).isEqualTo("varchar(10)");
    assertThat(converter.convert("decimal(10,2)")).isEqualTo("decimal(10,2)");
    assertThat(converter.convert("clob")).isEqualTo("clob");
    assertThat(converter.convert("blob")).isEqualTo("blob");
    assertThat(converter.convert("tinyint")).isEqualTo("tinyint");
    assertThat(converter.convert("integer(8)")).isEqualTo("integer(8)");
    assertThat(converter.convert("funky")).isEqualTo("funky"); // unknown
  }

  @Test
  public void testConvertJsonTypes_given_postgres() {

    PostgresPlatform platform = new PostgresPlatform();
    DbPlatformTypeMapping dbTypeMap = platform.getDbTypeMap();

    PlatformTypeConverter converter = new PlatformTypeConverter(dbTypeMap);

    assertThat(converter.convert("jsonblob")).isEqualTo("bytea");
    assertThat(converter.convert("jsonclob")).isEqualTo("text");
    assertThat(converter.convert("jsonvarchar(200)")).isEqualTo("varchar(200)");
    assertThat(converter.convert("json")).isEqualTo("json");
    assertThat(converter.convert("jsonb")).isEqualTo("jsonb");
  }

  @Test
  public void testConvertJsonTypes_given_h2() {

    H2Platform platform = new H2Platform();
    DbPlatformTypeMapping dbTypeMap = platform.getDbTypeMap();

    PlatformTypeConverter converter = new PlatformTypeConverter(dbTypeMap);

    assertThat(converter.convert("jsonblob")).isEqualTo("blob");
    assertThat(converter.convert("jsonclob")).isEqualTo("clob");
    assertThat(converter.convert("jsonvarchar(200)")).isEqualTo("varchar(200)");
    assertThat(converter.convert("json")).isEqualTo("clob");
    assertThat(converter.convert("jsonb")).isEqualTo("clob");
  }
}
