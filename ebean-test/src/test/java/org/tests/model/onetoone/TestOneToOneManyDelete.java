package org.tests.model.onetoone;

import io.ebean.DB;
import org.junit.jupiter.api.Test;

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

    DB.save(top);

    DB.delete(top);

  }
}
