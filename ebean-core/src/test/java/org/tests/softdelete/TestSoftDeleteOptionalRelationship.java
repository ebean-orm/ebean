package org.tests.softdelete;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import io.ebean.Ebean;
import org.tests.model.softdelete.ESoftDelMid;
import io.ebean.Finder;
import org.junit.Test;
import org.tests.model.softdelete.ESoftDelY;
import org.tests.model.softdelete.ESoftDelZ;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

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

  @Test
  public void testFindNullWhenMultiple() {
    UUID uuid = UUID.randomUUID();
    {
      ESoftDelZ z = new ESoftDelZ();
      z.setUuid(uuid);
      DB.save(z);

      ESoftDelY y = new ESoftDelY();
      y.setOrganization(z);
      y.setX(null);
      DB.save(y);
    }

    Finder<Long, ESoftDelY> finder = new Finder<>(ESoftDelY.class);
    ESoftDelY bean = finder
      .query()
      .where()
      .eq("organization.uuid", uuid)
      .isNull("x")
      .findOne();

    assertThat(bean).isNotNull();
  }


}
