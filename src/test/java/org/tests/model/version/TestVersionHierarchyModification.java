package org.tests.model.version;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TestVersionHierarchyModification extends BaseTestCase {

  @Before
  public void setup() {
    final VersionParent parent = new VersionParent();
    parent.setName("vParent");

    final VersionChild child1 = new VersionChild();
    child1.setName("vChild1");
    parent.getChildren().add(child1);

    final VersionToy toy11 = new VersionToy();
    toy11.setName("vToy1.1");
    child1.getToys().add(toy11);

    final VersionToy toy12 = new VersionToy();
    toy12.setName("vToy1.2");
    child1.getToys().add(toy12);

    final VersionChild child2 = new VersionChild();
    child2.setName("vChild2");
    parent.getChildren().add(child2);

    final VersionToy toy21 = new VersionToy();
    toy21.setName("vToy2.1");
    child2.getToys().add(toy21);

    final VersionToy toy22 = new VersionToy();
    toy22.setName("vToy2.2");
    child2.getToys().add(toy22);

    Ebean.save(parent);
  }

  @After
  public void cleanUp() {
    Ebean.find(VersionParent.class).delete();
  }

  @Test
  public void testMoveDown() {
    VersionParent parent = Ebean.find(VersionParent.class).findOne();
    assertThat(parent).isNotNull();
    assertThat(parent.getChildren()).hasSize(2);

    VersionChild firstChild = parent.getChildren().get(0);
    VersionChild secondChild = parent.getChildren().get(1);

    assertThat(firstChild.getToys()).hasSize(2);
    assertThat(secondChild.getToys()).hasSize(2);
    assertThat(firstChild.getToys()).extracting(VersionToy::getName).containsExactly("vToy1.1", "vToy1.2");
    assertThat(secondChild.getToys()).extracting(VersionToy::getName).containsExactly("vToy2.1", "vToy2.2");

    final VersionToy toyToMove = firstChild.getToys().get(0);
    firstChild.getToys().remove(toyToMove);
    toyToMove.setChild(secondChild);
    secondChild.getToys().add(1, toyToMove);

    Ebean.save(parent);

    parent = Ebean.find(VersionParent.class).findOne();
    assertThat(parent).isNotNull();
    assertThat(parent.getChildren()).hasSize(2);

    firstChild = parent.getChildren().get(0);
    secondChild = parent.getChildren().get(1);

    assertThat(firstChild.getToys()).hasSize(1);
    assertThat(secondChild.getToys()).hasSize(3);
    assertThat(firstChild.getToys()).extracting(VersionToy::getName).containsExactly("vToy1.2");
    assertThat(secondChild.getToys()).extracting(VersionToy::getName).containsExactly("vToy2.1", "vToy1.1", "vToy2.2");
  }

  @Test
  public void testMoveUp() {
    VersionParent parent = Ebean.find(VersionParent.class).findOne();
    assertThat(parent).isNotNull();
    assertThat(parent.getChildren()).hasSize(2);

    VersionChild firstChild = parent.getChildren().get(0);
    VersionChild secondChild = parent.getChildren().get(1);

    assertThat(firstChild.getToys()).hasSize(2);
    assertThat(secondChild.getToys()).hasSize(2);
    assertThat(firstChild.getToys()).extracting(VersionToy::getName).containsExactly("vToy1.1", "vToy1.2");
    assertThat(secondChild.getToys()).extracting(VersionToy::getName).containsExactly("vToy2.1", "vToy2.2");

    final VersionToy toyToMove = secondChild.getToys().get(0);
    secondChild.getToys().remove(toyToMove);
    toyToMove.setChild(firstChild);
    firstChild.getToys().add(1, toyToMove);

    Ebean.save(parent);

    parent = Ebean.find(VersionParent.class).findOne();
    assertThat(parent).isNotNull();
    assertThat(parent.getChildren()).hasSize(2);

    firstChild = parent.getChildren().get(0);
    secondChild = parent.getChildren().get(1);

    assertThat(secondChild.getToys()).hasSize(1);
    assertThat(firstChild.getToys()).hasSize(3);
    assertThat(secondChild.getToys()).extracting(VersionToy::getName).containsExactly("vToy2.2");
    assertThat(firstChild.getToys()).extracting(VersionToy::getName).containsExactly("vToy1.1", "vToy2.1", "vToy1.2");
  }

}
