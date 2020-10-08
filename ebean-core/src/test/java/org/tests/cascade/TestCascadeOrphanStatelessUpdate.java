package org.tests.cascade;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import io.ebean.text.json.JsonContext;
import org.junit.Test;

import java.util.List;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;

public class TestCascadeOrphanStatelessUpdate extends BaseTestCase {

  @Test
  public void update() {

    final CORoot orig = setup();

    CORoot root = DB.find(CORoot.class, orig.getId());
    assertNotNull(root);
    assertThat(root.getOne().getChildren()).hasSize(3);

    final JsonContext jsonContext = DB.json();
    String asJson = jsonContext.toJson(root);

    final CORoot deserialized = jsonContext.toBean(CORoot.class, asJson);

    final COOne one = deserialized.getOne();
    COOneMany removed0 = one.getChildren().remove(0);
    COOneMany removed1 = one.getChildren().remove(1);

    one.getChildren().add(new COOneMany("m3"));
    one.getChildren().add(new COOneMany("m4"));

    DB.update(deserialized);

    CORoot saved = DB.find(CORoot.class, orig.getId());
    assertNotNull(saved);
    assertThat(saved.getOne().getChildren()).hasSize(3);

    List<COOneMany> softDeleted = DB.find(COOneMany.class)
      .setIncludeSoftDeletes()
      .where()
      .idIn(asList(removed0.getId(), removed1.getId()))
      .findList();

    assertThat(softDeleted).hasSize(2);
    assertThat(softDeleted.stream().map(COOneMany::getId).collect(toList()))
      .contains(removed0.getId(), removed1.getId());
  }

  private CORoot setup() {
    COOne one = new COOne("one");
    one.getChildren().add(new COOneMany("m0"));
    one.getChildren().add(new COOneMany("m1"));
    one.getChildren().add(new COOneMany("m2"));

    CORoot root = new CORoot("r0", one);
    DB.save(root);

    return root;
  }
}
