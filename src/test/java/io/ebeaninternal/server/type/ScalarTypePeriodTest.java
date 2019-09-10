package io.ebeaninternal.server.type;

import org.junit.Test;

import java.time.Period;

import static org.assertj.core.api.Assertions.assertThat;

public class ScalarTypePeriodTest {

  private ScalarTypePeriod scalarType = new ScalarTypePeriod();

  @Test
  public void getLength() {
    assertThat(scalarType.getLength()).isEqualTo(20);
  }

  @Test
  public void formatAndParse() {

    Period original = Period.of(1,2, 4);

    String value = scalarType.formatValue(original);
    assertThat(value).isEqualTo("P1Y2M4D");

    Period period = scalarType.parse(value);
    assertThat(period).isEqualTo(original);
  }


  @Test
  public void convertFromDbString() {

    Period original = Period.of(1, 2, 4);

    String stringVal = scalarType.convertToDbString(original);
    Period period = scalarType.convertFromDbString(stringVal);

    assertThat(period).isEqualTo(original);
  }

}
