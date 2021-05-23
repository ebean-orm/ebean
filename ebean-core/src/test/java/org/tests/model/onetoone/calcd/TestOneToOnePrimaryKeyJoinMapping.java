package org.tests.model.onetoone.calcd;

import io.ebean.DB;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TestOneToOnePrimaryKeyJoinMapping {

  @Test
  public void test() {

    CalcDInput inputs = new CalcDInput("input0");

    CalcDData data = new CalcDData("data0");
    inputs.setData(data);
    inputs.save();

    CalcDInput found = DB.find(CalcDInput.class, inputs.getId());

    assertThat(found).isNotNull();
    assertThat(found.getName()).isEqualTo("input0");
    assertThat(found.getData().getName()).isEqualTo("data0");

  }
}
