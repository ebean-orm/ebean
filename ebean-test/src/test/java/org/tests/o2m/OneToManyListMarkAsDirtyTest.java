package org.tests.o2m;

import io.ebean.DB;
import io.ebean.xtest.BaseTestCase;
import org.junit.jupiter.api.Test;
import org.tests.model.orphanremoval.OmBeanListChild;
import org.tests.model.orphanremoval.OmBeanListParent;
import java.time.Instant;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

class OneToManyListMarkAsDirtyTest extends BaseTestCase {
  @Test
  void usingNewListWithNonDirtyParent_expect_orphanDeleted_works() {
    // setup
    var parent = new OmBeanListParent();
    var a_b = new OmBeanListChild("b");
    parent.getChildren().add(a_b);
    DB.save(parent);

    // act
    var secondParent = DB.find(OmBeanListParent.class, parent.getId());
    secondParent.setChildren2(new ArrayList<>()); // <!-- HERE - setting a new ArrayList rather than using clear()
    var a_c = new OmBeanListChild("b");
    secondParent.getChildren().add(a_c);
    // working here as secondParent itself is not dirty
    DB.save(secondParent);

    var refreshedPlanSecondCapacity = DB.find(OmBeanListParent.class, parent.getId());
    assertThat(refreshedPlanSecondCapacity.getChildren().size()).isEqualTo(1);
    assertThat(refreshedPlanSecondCapacity.getChildren().get(0).getId()).isEqualTo(a_c.getId());
    // Q: is this expected? A: Rob Bygrave - Yes it is expected
    assertThat(refreshedPlanSecondCapacity.getVersion()).isEqualTo(parent.getVersion());
    assertThat(refreshedPlanSecondCapacity.getWhenModified()).isEqualTo(parent.getWhenModified());
  }

  @Test
  void usingNewListWithParentDirty_expect_orphanDeleted_fails2() {
    // setup
    var parent = new OmBeanListParent();
    var a_b = new OmBeanListChild("b");
    parent.getChildren().add(a_b);
    DB.save(parent);

    // act
    var secondParent = DB.find(OmBeanListParent.class, parent.getId());
    secondParent.setChildren2(new ArrayList<>()); // <!-- HERE: using new ArrayList with orphanRemoval
    var a_c = new OmBeanListChild("c");
    secondParent.getChildren().add(a_c);
    // force version increase
    DB.markAsDirty(secondParent);
    DB.save(secondParent);

    var refreshedPlanSecondCapacity = DB.find(OmBeanListParent.class, parent.getId());
    assertThat(refreshedPlanSecondCapacity.getChildren().size()).isEqualTo(1);
    assertThat(refreshedPlanSecondCapacity.getChildren().get(0).getId()).isEqualTo(a_c.getId());
    assertThat(refreshedPlanSecondCapacity.getVersion()).isGreaterThan(parent.getVersion());
    assertThat(refreshedPlanSecondCapacity.getWhenModified()).isAfter(parent.getWhenModified());
  }

  @Test
  void usingClearWithParentDirty_expect_orphanDeleted_works() {
    // setup
    var parent = new OmBeanListParent();
    var a_b = new OmBeanListChild("b");
    parent.getChildren().add(a_b);
    DB.save(parent);

    // act
    var secondParent = DB.find(OmBeanListParent.class, parent.getId());
    secondParent.getChildren().clear(); // <!-- HERE: Using clear() with orphanRemoval
    var a_c = new OmBeanListChild("c");
    secondParent.getChildren().add(a_c);
    // force version increase
    DB.markAsDirty(secondParent);
    DB.save(secondParent);

    var refreshedParent = DB.find(OmBeanListParent.class, parent.getId());
    assertThat(refreshedParent.getChildren().size()).isEqualTo(1);
    assertThat(refreshedParent.getChildren().get(0).getId()).isEqualTo(a_c.getId());
    assertThat(refreshedParent.getVersion()).isGreaterThan(parent.getVersion());
    assertThat(refreshedParent.getWhenModified()).isAfter(parent.getWhenModified());
  }

  @Test
  void usingNewListWithParentDirty_expect_orphanDeleted_fails() {
    // setup
    var parent = new OmBeanListParent();
    var a_b = new OmBeanListChild("b");
    parent.getChildren().add(a_b);
    DB.save(parent);

    // act
    var secondParent = DB.find(OmBeanListParent.class, parent.getId());
    secondParent.setChildren2(new ArrayList<>()); // <!-- HERE: new ArrayList with orphanRemoval
    var a_c = new OmBeanListChild("c");
    secondParent.getChildren().add(a_c);
    secondParent.setWhenCreated(Instant.now());
    DB.save(secondParent);

    var refreshedParent = DB.find(OmBeanListParent.class, parent.getId());
    assertThat(refreshedParent.getChildren().size()).isEqualTo(1);
    assertThat(refreshedParent.getChildren().get(0).getId()).isEqualTo(a_c.getId());
    assertThat(refreshedParent.getVersion()).isGreaterThan(parent.getVersion());
    assertThat(refreshedParent.getWhenModified()).isAfter(parent.getWhenModified());
  }
}
