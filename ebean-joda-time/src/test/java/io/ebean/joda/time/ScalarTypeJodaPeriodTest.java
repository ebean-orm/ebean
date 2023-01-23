package io.ebean.joda.time;

import org.joda.time.Period;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ScalarTypeJodaPeriodTest {

  private ScalarTypeJodaPeriod scalarType = new ScalarTypeJodaPeriod();

  @Test
  public void getLength() {
    assertThat(scalarType.length()).isEqualTo(50);
  }

  @Test
  public void formatAndParse() {

    Period original = Period.years(1).plusMonths(2).plusDays(4)
      .plusHours(12).plusMinutes(19).plusSeconds(20);

    String value = scalarType.formatValue(original);
    assertThat(value).isEqualTo("P1Y2M4DT12H19M20S");

    Period period = scalarType.parse(value);
    assertThat(period).isEqualTo(original);
  }


  @Test
  public void convertFromDbString() {

    Period original = Period.years(1).plusMonths(2).plusDays(4)
      .plusHours(23).plusMinutes(19).plusSeconds(20).plusMillis(987);

    String stringVal = scalarType.convertToDbString(original);
    Period period = scalarType.convertFromDbString(stringVal);

    assertThat(period).isEqualTo(original);
  }

}
