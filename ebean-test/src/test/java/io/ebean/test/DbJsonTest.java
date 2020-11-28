package io.ebean.test;

import io.ebean.DB;
import org.junit.Test;
import org.test.BSimpleWithGen;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


public class DbJsonTest {

  @Test
  public void of() {

    DB.find(BSimpleWithGen.class).delete();

    BSimpleWithGen bean = new BSimpleWithGen("something");
    DB.save(bean);

    assertThat(bean.getVersion()).isEqualTo(1);

    DbJson.of(bean)
      .withPlaceholder("_")
      .replace("id", "whenModified")
      .assertContentMatches("/bean/example-bean.json");


    BSimpleWithGen bean2 = new BSimpleWithGen("other");
    DB.save(bean2);

    final List<BSimpleWithGen> beans = DB.find(BSimpleWithGen.class).findList();

    DbJson.of(beans)
      //.withPlaceholder("_")
      .replace("id", "whenModified")
      .assertContentMatches("/bean/example-list.json");

  }
}
