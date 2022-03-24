package org.tests.o2m.lazy;

import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestLazyManyViaPath extends BaseTestCase {

  @Test
  public void test() {
    OmlBar bar = new OmlBar();
    List<OmlFoo> fooList = new ArrayList<>();
    fooList.add(createNewFooWithBar(bar));
    bar.setFooList(fooList);

    DB.save(bar);

    OmlFoo fooFromDb = DB
      .find(OmlFoo.class)
      .where()
      .eq("id", bar.getFooList().get(0).getId())
      .findOne();

    // This works
    List<OmlFoo> foosList = fooFromDb.getBar().getFooList();
    assertThat(foosList).hasSize(1);
    assertThat(fooList).hasSize(1);

    OmlBaz bazFromDb = DB
      .find(OmlBaz.class)
      .where()
      .eq("id", bar.getFooList().get(0).getBazList().get(0).getId())
      .findOne();
    assert bazFromDb != null;

    // This does not work and gives the exception
    List<OmlFoo> foosList1 = bazFromDb.getFoo().getBar().getFooList();
    assertThat(foosList1.size()).isEqualTo(1);
  }

  private static OmlFoo createNewFooWithBar(OmlBar bar) {
    OmlFoo foo = new OmlFoo(new OmlBaz());
    foo.setBar(bar);
    return foo;
  }

}
