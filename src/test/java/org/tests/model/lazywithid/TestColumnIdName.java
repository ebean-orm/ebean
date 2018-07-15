package org.tests.model.lazywithid;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;

public class TestColumnIdName extends BaseTestCase {
  @Test
  public void test() {

    Tune tune = new Tune();
    tune.getLoonies().add(new Looney("Taz"));
    Ebean.save(tune);

    final List<Tune> fetchedCollection = Ebean.find(Tune.class).findList();

    assertEquals(1, fetchedCollection.size());
    assertEquals(1, fetchedCollection.get(0).getLoonies().size());
    assertEquals("Taz", fetchedCollection.get(0).getLoonies().get(0).getName());
  }
}
