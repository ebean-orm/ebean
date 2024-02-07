package org.tests.model.orphanremoval;

import org.junit.jupiter.api.Test;

import java.util.List;

import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TestOrphanRemovalOverwrite {

  @Test
  public void testOverwritingMapping() {
    OmBeanListParent parent = new OmBeanListParent();
    parent.save();

    // Refreshing/querying sets the modifyListening flag on the association's BeanList
    parent.refresh();

    List<OmBeanListChild> childList = singletonList(new OmBeanListChild("child1"));
    // Adding the children to the BeanList causes _ebean_getIdentity to be invoked before the children have been persisted and
    // have Ids.
    parent.setChildren(childList);

    // Give the children Ids
    parent.save();

    // Refreshing here generates new objects for the associated children that are referred to by the parent.
    parent.refresh();

    OmBeanListChild child = childList.get(0);
    assertNotNull(child.getId());

    OmBeanListChild refreshedChild = parent.getChildren().get(0);
    assertEquals(child.getId(), refreshedChild.getId());
    assertEquals(child, refreshedChild);
    assertEquals(childList, parent.getChildren());
  }
}
