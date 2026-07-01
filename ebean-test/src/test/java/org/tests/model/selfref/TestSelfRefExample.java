package org.tests.model.selfref;

import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
import io.ebean.Query;
import org.junit.jupiter.api.Test;

import jakarta.persistence.PersistenceException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestSelfRefExample extends BaseTestCase {

  @Test
  public void test() {

    try {
      DB.sqlUpdate("delete from self_ref_example").execute();
    } catch (PersistenceException e) {
      logger.debug("TestSelfRefExample skipped - MySql not deleting based on constraints");
      return;
    }

    SelfRefExample e1 = new SelfRefExample("test1", null);
    SelfRefExample e2 = new SelfRefExample("test1", e1);
    SelfRefExample e3 = new SelfRefExample("test2", e2);
    SelfRefExample e4 = new SelfRefExample("test2", e1);
    SelfRefExample e5 = new SelfRefExample("test2", e4);
    SelfRefExample e6 = new SelfRefExample("test2", e2);
    SelfRefExample e7 = new SelfRefExample("test3", e3);
    SelfRefExample e8 = new SelfRefExample("test3", e7);

    DB.save(e1);
    DB.save(e2);
    DB.save(e3);
    DB.save(e4);
    DB.save(e5);
    DB.save(e6);
    DB.save(e7);
    DB.save(e8);

    Query<SelfRefExample> examples = DB.find(SelfRefExample.class).setLazyLoadBatchSize(1);
    List<SelfRefExample> list = examples.where().eq("name", "test1").orderBy().asc("id").findList();

    assertThat(list).hasSize(2);

    //The first Example is e1. e2 is the first child of e1. e3 is the first child of e2.
    SelfRefExample e1Fetched = list.get(0);
    SelfRefExample e2Fetched = list.get(1);

    assertThat(e1Fetched.getId()).isEqualTo(e1.getId());
    assertThat(e2Fetched.getId()).isEqualTo(e2.getId());

    // Now we can't guarantee the order of loaded children so use findChild

    List<SelfRefExample> e1Children = e1Fetched.getChildren();
    SelfRefExample e2Searched = findChild(e1Children, e2.getId());
    assertThat(e2Fetched.getChildren()).hasSize(2);
    assertThat(e2Fetched.getChildren()).extracting("id").contains(e3.getId(), e6.getId());


    SelfRefExample e3Searched = findChild(e2Searched.getChildren(), e3.getId());
    assertThat(e3Searched).isNotNull();
    assertThat(e3Searched.getChildren()).extracting("id").contains(e7.getId());

    // If we get all the items, you can see the structure goes down a fair bit further.
    Query<SelfRefExample> examples2 = DB.createQuery(SelfRefExample.class).orderBy("id asc");
    List<SelfRefExample> list2 = examples2.findList();

    assertEquals(e1.getId(), list2.get(0).getId());
    assertEquals(e2.getId(), list2.get(0).getChildren().get(0).getId());
    assertEquals(e3.getId(), list2.get(0).getChildren().get(0).getChildren().get(0).getId());
    assertEquals(e7.getId(), list2.get(0).getChildren().get(0).getChildren().get(0).getChildren().get(0).getId());
  }

  private SelfRefExample findChild(List<SelfRefExample> e1Children, Long id) {
    for (SelfRefExample e1Child : e1Children) {
      if (e1Child.getId().equals(id)) {
        return e1Child;
      }
    }
    return null;
  }
}
