package io.ebeaninternal.server.type;

import org.joda.time.LocalDate;
import org.junit.Test;

import java.sql.Date;

import static org.assertj.core.api.Assertions.assertThat;


public class ScalarTypeJodaLocalDateTest {

  private ScalarTypeJodaLocalDate type = new ScalarTypeJodaLocalDate();

  @Test
  public void convertToMillis_convertFromMillis() {

    LocalDate localDate = new LocalDate();
    long millis = type.convertToMillis(localDate);
    LocalDate localDate1 = type.convertFromMillis(millis);

    assertThat(localDate).isEqualTo(localDate1);
  }

  @Test
  public void convertToDate_convertFromDate() {

    convertDate(new LocalDate());
    convertDate(new LocalDate(1899, 12, 1));
    convertDate(new LocalDate(1900, 1, 1));
  }

  private void convertDate(LocalDate localDate) {

    Date dateValue = type.convertToDate(localDate);
    LocalDate localDate1 = type.convertFromDate(dateValue);

    assertThat(localDate).isEqualTo(localDate1);
  }

  @Test
  public void toJdbcType() {

    LocalDate localDate = new LocalDate();
    Object jdbcType = type.toJdbcType(localDate);
    Date dateValue = type.convertToDate(localDate);

    assertThat(jdbcType).isEqualTo(dateValue);
  }

  @Test
  public void toBeanType() {

    LocalDate localDate = new LocalDate();
    Date dateValue = type.convertToDate(localDate);
    LocalDate beanType = type.toBeanType(dateValue);

    assertThat(beanType).isEqualTo(localDate);
  }

}
