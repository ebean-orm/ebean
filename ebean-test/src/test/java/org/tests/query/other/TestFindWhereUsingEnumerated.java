package org.tests.query.other;

import io.ebean.DB;
import io.ebean.xtest.base.TransactionalTestCase;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.MNonEnum;
import org.tests.model.basic.MNonUpdPropEntity;

import static org.assertj.core.api.Assertions.assertThat;

public class TestFindWhereUsingEnumerated extends TransactionalTestCase {

  @Test
  public void test() {

    MNonUpdPropEntity e = new MNonUpdPropEntity();
    e.setNonEnum(MNonEnum.END);
    e.setName("TheNonEnumIsEnd");
    e.setNote("note");

    DB.save(e);

    MNonUpdPropEntity unique = DB.find(MNonUpdPropEntity.class).where()
      .eq("nonEnum", MNonEnum.END)
      .eq("name", "TheNonEnumIsEnd")
      .findOne();

    assertThat(unique).isNotNull();
  }

}
