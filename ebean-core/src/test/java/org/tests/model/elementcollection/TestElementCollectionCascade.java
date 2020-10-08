package org.tests.model.elementcollection;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import org.junit.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class TestElementCollectionCascade extends BaseTestCase {

  @Test
  public void test() {

    EcsmParent parent = new EcsmParent("p1");
    final List<EcsmChild> children = parent.getChildren();

    children.add(createChild("c0", 2));
    children.add(createChild("c1", 2));

    DB.save(parent);

    replaceWith(children.get(0), 22);
    replaceWith(children.get(1), 5);

    parent.setName("p1-mod");
    DB.save(parent);

    Map<String, Set<String>> childVals = new LinkedHashMap<>();

    final EcsmParent foundParent = DB.find(EcsmParent.class, parent.getId());
    final List<EcsmChild> children1 = foundParent.getChildren();
    for (EcsmChild ecsmOne : children1) {
      final Set<String> values = ecsmOne.getValues();
      values.size();
      childVals.put(ecsmOne.getName(), values);
    }

    Set<String> vals0 = childVals.get("c0");
    assertThat(vals0).hasSize(22);

    Set<String> vals1 = childVals.get("c1");
    assertThat(vals1).hasSize(5);
  }

  private void replaceWith(EcsmChild one, int count) {
    one.getValues().clear();
    createChild(one, count);
  }

  private EcsmChild createChild(String name, int count) {
    return createChild(new EcsmChild(name), count);
  }

  private EcsmChild createChild(EcsmChild one, int count) {
    final String name = one.getName();
    for (int i = 0; i < count; i++) {
      one.getValues().add(name + i);
    }
    return one;
  }
}
