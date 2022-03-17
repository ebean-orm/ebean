package io.ebeaninternal.dbmigration.ddlgeneration.platform;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;


public class DB2ColumnOptionsParserTest {

  private final SoftAssertions softly = new SoftAssertions();

  @AfterEach
  void assertAll() {
    softly.assertAll();
  }
  @Test
  public void testParser() {
    DB2ColumnOptionsParser p = new DB2ColumnOptionsParser("blob(64M) inline length 200 logged compact");
    softly.assertThat(p.getType()).isEqualTo("blob(64M)");
    softly.assertThat(p.getInlineLength()).isEqualTo("inline length 200");
    softly.assertThat(p.isLogged()).isTrue();
    softly.assertThat(p.isCompact()).isTrue();
    
    p = new DB2ColumnOptionsParser("blob(64M) inline length 200 not logged not compact");
    softly.assertThat(p.getType()).isEqualTo("blob(64M)");
    softly.assertThat(p.getInlineLength()).isEqualTo("inline length 200");
    softly.assertThat(p.isLogged()).isFalse();
    softly.assertThat(p.isCompact()).isFalse();

    p = new DB2ColumnOptionsParser("blob(64M) inline length 200");
    softly.assertThat(p.getType()).isEqualTo("blob(64M)");
    softly.assertThat(p.getInlineLength()).isEqualTo("inline length 200");
    softly.assertThat(p.isLogged()).isTrue();
    softly.assertThat(p.isCompact()).isFalse();

    p = new DB2ColumnOptionsParser("blob(64M)");
    softly.assertThat(p.getType()).isEqualTo("blob(64M)");
    softly.assertThat(p.getInlineLength()).isNull();
    softly.assertThat(p.isLogged()).isTrue();
    softly.assertThat(p.isCompact()).isFalse();
  }
}
