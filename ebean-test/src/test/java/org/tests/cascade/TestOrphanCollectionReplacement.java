package org.tests.cascade;

import io.ebean.DB;
import io.ebean.xtest.BaseTestCase;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

class TestOrphanCollectionReplacement extends BaseTestCase {

  @Test
  void replaceCollection_whenOrphan_expect_forcedInsert() {
    long parentId;
    { // setup
      List<COOneMany> children = new ArrayList<>();
      children.add(new COOneMany("c0"));
      children.add(new COOneMany("c1"));

      COOne parent = new COOne("p0");
      parent.setChildren(children);

      DB.save(parent);
      parentId = parent.getId();
    }

    { // act
      COOne fetchedParent = DB.find(COOne.class, parentId);
      assert fetchedParent != null;

      COOneMany role = new COOneMany("c2");

      List<COOneMany> filtered = fetchedParent.getChildren().stream().filter(r -> "c0".equals(r.getName())).collect(Collectors.toList());

      List<COOneMany> updatedRoles = new ArrayList<>();
      updatedRoles.addAll(filtered);
      updatedRoles.addAll(List.of(role));
      fetchedParent.setChildren(updatedRoles);

      DB.save(fetchedParent);
    }

    COOne fetchedUser2 = DB.find(COOne.class, parentId);
    requireNonNull(fetchedUser2);
    assertEquals(2, fetchedUser2.getChildren().size());
  }
}
