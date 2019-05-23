package org.tests.update;

import static org.junit.Assert.assertTrue;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import org.junit.Test;
import org.tests.update.objects.SiblingA;
import org.tests.update.objects.Parent;
import org.tests.update.objects.SiblingB;
import org.tests.update.objects.Child;

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


  @Test
  public void testFetchChildSaveParent() {

    long dId = createCAndD();
    modifyPropertyToTrue2(dId);

    Parent parent = Ebean.find(Parent.class, dId);
    assert parent != null;
    // Fails here because D was not saved even though C has cascade = ALL
    assertTrue(parent.getChild().isTestProperty());
  }

  private void modifyPropertyToTrue2(long dId) {
    Child child = Ebean.find(Child.class, dId);
    assert child != null;

    child.setTestProperty(true);
    final Parent parent = child.getParent();
    Ebean.save(parent);
  }

  private long createCAndD() {
    Parent parent = new Parent();
    Child child = new Child();
    parent.setChild(child);

    Ebean.save(parent);
    return parent.getChild().getId();
  }
}
