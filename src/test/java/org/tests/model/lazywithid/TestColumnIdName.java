package org.tests.model.lazywithid;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import io.ebean.Ebean;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class TestColumnIdName extends BaseTestCase {
  @Test
  public void test() {
    DB.truncate(Tune.class);

    Tune tune = new Tune();
    tune.getLoonies().add(new Looney("Taz"));
    Ebean.save(tune);

    final List<Tune> fetchedCollection = Ebean.find(Tune.class).findList();

    assertEquals(1, fetchedCollection.size());
    assertEquals(1, fetchedCollection.get(0).getLoonies().size());
    assertEquals("Taz", fetchedCollection.get(0).getLoonies().get(0).getName());
  }
}
