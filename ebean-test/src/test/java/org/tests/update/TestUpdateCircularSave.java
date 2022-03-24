package org.tests.update;

import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.tests.update.objects.Child;
import org.tests.update.objects.Parent;
import org.tests.update.objects.SiblingA;
import org.tests.update.objects.SiblingB;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestUpdateCircularSave extends BaseTestCase {

  @Test
  public void testCircularCascade() {

    long aId = createA();
    modifyPropertyToTrue(aId);

    SiblingA siblingA = DB.find(SiblingA.class, aId);
    assert siblingA != null;
    assertTrue(siblingA.getSiblingB().isTestProperty());
  }

  private void modifyPropertyToTrue(long aId) {
    SiblingA siblingA = DB.find(SiblingA.class, aId);
    assert siblingA != null;

    final SiblingB siblingB = siblingA.getSiblingB();
    siblingB.setTestProperty(true);
    // Will get optimistic lock as version is increased twice on B
    DB.save(siblingB);
  }

  private long createA() {
    SiblingA siblingA = new SiblingA();
    SiblingB siblingB = new SiblingB();
    siblingA.setSiblingB(siblingB);

    DB.save(siblingA);
    return siblingA.getId();
  }

  @Disabled
  @Test
  public void testFetchChildModifyChildSaveParent() {

    long childId = createParentAndChild().getChild().getId();

    Child child = DB.find(Child.class, childId);
    assert child != null;

    child.setTestProperty(true);
    final Parent parent = child.getParent();
    DB.save(parent);

    assertChildModified(childId);
  }

  private void assertChildModified(long childId) {
    Parent parent = DB.find(Parent.class, childId);
    assert parent != null;
    // Fails here because D was not saved even though C has cascade = ALL
    assertTrue(parent.getChild().isTestProperty());
    assertEquals(parent.getChild().getVersion(), 2L);
  }

  private Parent createParentAndChild() {
    Parent parent = new Parent();
    Child child = new Child();
    parent.setChild(child);

    DB.save(parent);
    return parent;
  }


  @Test
  public void testFetchParentModifyChildSaveParent() {
    long parentId = createParentAndChild().getId();

    Parent parent = DB.find(Parent.class, parentId);
    assert parent != null;

    parent.getChild().setTestProperty(true);
    DB.save(parent);

    assertChildModified(parent.getChild().getId());
  }
}
