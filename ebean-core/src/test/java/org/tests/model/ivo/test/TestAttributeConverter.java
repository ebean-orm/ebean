package org.tests.model.ivo.test;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import org.junit.Test;
import org.tests.model.ivo.ESomeConvertType;
import org.tests.model.ivo.Money;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestAttributeConverter extends BaseTestCase {

  @Test
  public void insertUpdateDelete() {

    ESomeConvertType bean = new ESomeConvertType("one", new Money(20.1));
    DB.save(bean);

    ESomeConvertType found = DB.find(ESomeConvertType.class, bean.getId());
    found.setMoney(new Money("40"));
    DB.save(found);

    final List<ESomeConvertType> list40 = DB.find(ESomeConvertType.class)
      .where()
      .eq("money", new Money("40"))
      .findList();

    assertThat(list40).hasSize(1);

    final List<ESomeConvertType> listGt19 = DB.find(ESomeConvertType.class)
      .where()
      .gt("money", new Money("19"))
      .findList();

    assertThat(listGt19).hasSize(1);

    DB.delete(found);
  }
}
