package org.tests.model.lazywithid;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestColumnIdName extends BaseTestCase {
  @Test
  public void test() {

    Tune tune = new Tune();
    tune.getLoonies().add(new Looney("Taz"));
    DB.save(tune);

    final List<Tune> fetchedCollection = DB.find(Tune.class).findList();

    assertThat(fetchedCollection).hasSize(1);
    assertThat(fetchedCollection.get(0).getLoonies()).hasSize(1);
    assertThat(fetchedCollection.get(0).getLoonies().get(0).getName()).isEqualTo("Taz");
  }
}
