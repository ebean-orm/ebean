package org.tests.query.other;

import io.ebean.Ebean;
import io.ebean.TransactionalTestCase;
import org.junit.Test;
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

    Ebean.save(e);

    MNonUpdPropEntity unique = Ebean.find(MNonUpdPropEntity.class).where()
      .eq("nonEnum", MNonEnum.END)
      .eq("name", "TheNonEnumIsEnd")
      .findOne();

    assertThat(unique).isNotNull();
  }

}
