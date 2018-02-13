package org.tests.model.onetoone;

import io.ebean.Ebean;
import org.junit.Test;

public class TestOneToOneManyDelete {

  @Test
  public void test() {

    OtoThTop top = new OtoThTop();
    top.setTopp("top");
    for (int i = 0; i < 4; i++) {

      OtoThMany many = new OtoThMany();
      many.setMany("many "+i);

      OtoThOne one = new OtoThOne();
      one.setOne(true);
      one.setMany(many);

      many.setOne(one);

      top.getManies().add(many);
    }

    Ebean.save(top);

    Ebean.delete(top);

  }
}
