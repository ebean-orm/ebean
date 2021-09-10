package org.tests.model.uuidsibling;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

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

    assertThat(parent.getChildren()).hasSize(1);
    assertThat(parent.getChildren().get(0).getId()).isEqualTo(child.getId());
    assertThat(parent.getChildren().get(0).getChildSibling()).isNotNull();
    assertThat(parent.getChildren().get(0).getChildSibling().getId()).isEqualTo(childSibling.getId());
  }

  private void assertAfter(Long parentId, UUID childId, Long childSiblingId) {
    assertThat(USibParent.find.byId(parentId)).isNull();
    assertThat(USibChild.find.byId(childId)).isNull();
    assertThat(USibChildSibling.find.byId(childSiblingId)).isNull();
  }
}
