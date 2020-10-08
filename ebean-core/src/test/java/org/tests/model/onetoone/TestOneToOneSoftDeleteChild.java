package org.tests.model.onetoone;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TestOneToOneSoftDeleteChild extends BaseTestCase {

  @Test
  public void deleteChild() {


    OtoSdChild child = new OtoSdChild("c1");
    OtoSdMaster master = new OtoSdMaster("m1");
    master.setChild(child);

    Ebean.save(master);


    verifyBeforeDelete(master, child);

    Ebean.delete(child);

    verifyAfterDelete(master, child);
  }

  private void verifyBeforeDelete(OtoSdMaster parent, OtoSdChild child) {
    assertThat(OtoSdMaster.find.byId(parent.getId()).getChild().getId())
      .isEqualTo(child.getId());

    assertThat(
      OtoSdChild.find.byId(child.getId()).getMaster().getId())
      .isEqualTo(parent.getId());
  }

  private void verifyAfterDelete(OtoSdMaster parent, OtoSdChild child) {
    // After delete, finding child by id should return null
    assertThat(OtoSdChild.find.byId(child.getId()))
      .isNull();

    // After delete, getting linked child from parent should return null
    assertThat(OtoSdMaster.find.byId(parent.getId()).getChild())
      .isNull();
  }
}
