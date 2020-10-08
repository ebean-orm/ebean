package org.tests.update;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import org.junit.Ignore;
import org.junit.Test;
import org.tests.update.objects.Child;
import org.tests.update.objects.Parent;
import org.tests.update.objects.SiblingA;
import org.tests.update.objects.SiblingB;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestUpdateCircularSave extends BaseTestCase {

  @Test
  public void testCircularCascade() {

    long aId = createA();
    modifyPropertyToTrue(aId);

    SiblingA siblingA = Ebean.find(SiblingA.class, aId);
    assert siblingA != null;
    assertTrue(siblingA.getSiblingB().isTestProperty());
  }

  private void modifyPropertyToTrue(long aId) {
    SiblingA siblingA = Ebean.find(SiblingA.class, aId);
    assert siblingA != null;

    final SiblingB siblingB = siblingA.getSiblingB();
    siblingB.setTestProperty(true);
    // Will get optimistic lock as version is increased twice on B
    Ebean.save(siblingB);
  }

  private long createA() {
    SiblingA siblingA = new SiblingA();
    SiblingB siblingB = new SiblingB();
    siblingA.setSiblingB(siblingB);

    Ebean.save(siblingA);
    return siblingA.getId();
  }

  @Ignore
  @Test
  public void testFetchChildModifyChildSaveParent() {

    long childId = createParentAndChild().getChild().getId();

    Child child = Ebean.find(Child.class, childId);
    assert child != null;

    child.setTestProperty(true);
    final Parent parent = child.getParent();
    Ebean.save(parent);

    assertChildModified(childId);
  }

  private void assertChildModified(long childId) {
    Parent parent = Ebean.find(Parent.class, childId);
    assert parent != null;
    // Fails here because D was not saved even though C has cascade = ALL
    assertTrue(parent.getChild().isTestProperty());
    assertEquals(parent.getChild().getVersion(), 2L);
  }

  private Parent createParentAndChild() {
    Parent parent = new Parent();
    Child child = new Child();
    parent.setChild(child);

    Ebean.save(parent);
    return parent;
  }


  @Test
  public void testFetchParentModifyChildSaveParent() {
    long parentId = createParentAndChild().getId();

    Parent parent = Ebean.find(Parent.class, parentId);
    assert parent != null;

    parent.getChild().setTestProperty(true);
    Ebean.save(parent);

    assertChildModified(parent.getChild().getId());
  }
}
