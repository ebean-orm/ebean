package org.tests.softdelete;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import org.tests.model.softdelete.ESoftDelMid;
import org.junit.Test;

import static org.assertj.core.api.StrictAssertions.assertThat;

public class TestSoftDeleteOptionalRelationship extends BaseTestCase {

  @Test
  public void testFindWhenNullRelationship() {

    ESoftDelMid mid1 = new ESoftDelMid(null, "mid1");
    Ebean.save(mid1);

    ESoftDelMid bean = Ebean.find(ESoftDelMid.class)
      .setId(mid1.getId())
      .fetch("top")
      .findOne();

    assertThat(bean).isNotNull();
    assertThat(bean.getTop()).isNull();
  }


}
