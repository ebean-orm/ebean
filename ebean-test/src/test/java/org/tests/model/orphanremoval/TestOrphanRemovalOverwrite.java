package org.tests.model.orphanremoval;

import io.ebean.DB;
import io.ebean.test.LoggedSql;
import org.junit.jupiter.api.Test;

import java.util.List;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class TestOrphanRemovalOverwrite {

  @Test
  void testOverwritingMapping() {
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

  @Test
  void clearAddAll() {
    OmBeanListChild c1 = new OmBeanListChild("c1");
    OmBeanListChild c2 = new OmBeanListChild("c2");

    OmBeanListParent parent = new OmBeanListParent();
    parent.getChildren().add(c1);
    parent.getChildren().add(c2);
    parent.save();


    OmBeanListChild c3 = new OmBeanListChild("c3");
    c3.setId(c1.getId());

    OmBeanListParent p1 = DB.find(OmBeanListParent.class, parent.getId());
    List<OmBeanListChild> children = p1.getChildren();
    children.clear();
    children.add(c3);

    LoggedSql.start();
    DB.save(p1);

    List<String> sql = LoggedSql.stop();
    assertThat(sql).hasSize(7);
    assertThat(sql.get(0)).contains("delete from om_bean_list_child where id=?");
    assertThat(sql.get(1)).contains(" -- bind");
    assertThat(sql.get(2)).contains(" -- bind");
    assertThat(sql.get(3)).contains(" -- executeBatch()");
    assertThat(sql.get(4)).contains(" insert into om_bean_list_child");
    assertThat(sql.get(5)).contains(" -- bind");
    assertThat(sql.get(6)).contains(" -- executeBatch()");
  }
}
