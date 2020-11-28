package org.tests.model.uuidsibling;

import org.junit.Test;

import java.util.UUID;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;

public class SoftDeleteCascadeTest {

  @Test
  public void testDeleteParent() {
    // Insert + link records
    USibParent parent = new USibParent();
    parent.save();

    USibChild child = new USibChild(parent);
    child.save();

    USibChildSibling childSibling = new USibChildSibling(child);
    childSibling.save();

    Long parentId = parent.getId();
    UUID childId = child.getId();
    Long childSiblingId = childSibling.getId();

    assertBefore(parent, child, childSibling);
    parent.delete();
    assertAfter(parentId, childId, childSiblingId);
  }

  private void assertBefore(USibParent parent, USibChild child, USibChildSibling childSibling) {

    parent.refresh();

    assertThat("Parent should have one child loaded",
      parent.getChildren().size(),
      is(1)
    );

    assertThat("Parent should have correct child loaded",
      parent.getChildren().get(0).getId(),
      is(child.getId())
    );

    assertThat("Child that was loaded should have its sibling available",
      parent.getChildren().get(0).getChildSibling(),
      not(nullValue())
    );

    assertThat(
      "Child that was loaded should have loaded correct sibling",
      parent.getChildren().get(0).getChildSibling().getId(),
      is(childSibling.getId())
    );
  }

  private void assertAfter(Long parentId, UUID childId, Long childSiblingId) {
    assertThat(USibParent.find.byId(parentId), is(nullValue()));
    assertThat(USibChild.find.byId(childId), is(nullValue()));
    assertThat(USibChildSibling.find.byId(childSiblingId), is(nullValue()));
  }
}
