package io.ebeaninternal.dbmigration;

import org.junit.jupiter.api.Test;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;

public class MChecksumTest {
  
  @Test
  public void calculate() {
    File file = new File("src/test/resources/dbmigration/index/1.0__hello.sql");
    assertThat(file).exists();

    assertThat(MChecksum.calculate(file)).isEqualTo(907060870);
  }

  @Test
  public void calculateWithSpecialChars() {
    File file = new File("src/test/resources/dbmigration/index-special-chars/1.0__hello.sql");
    assertThat(file).exists();

    assertThat(MChecksum.calculate(file)).isEqualTo(1859426839);
  }
}
