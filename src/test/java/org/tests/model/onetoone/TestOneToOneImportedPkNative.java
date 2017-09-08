package org.tests.model.onetoone;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.EbeanServer;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TestOneToOneImportedPkNative extends BaseTestCase {

  @Test
  public void native_with_o2oAndImportedPrimaryKey() {

    EbeanServer server = Ebean.getDefaultServer();
    server.find(OtoBMaster.class).delete();

    OtoBMaster one = new OtoBMaster();
    one.setName("hello");
    Ebean.save(one);

    OtoBMaster m = server.findNative(OtoBMaster.class, "select * from oto_bmaster").findOne();

    assertThat(m.getId()).isEqualTo(one.getId());
    assertThat(m.getName()).isEqualTo(one.getName());
    assertThat(m.getChild()).isNull();

    OtoBMaster m2 = server.find(OtoBMaster.class, one.getId());
    assertThat(m2.getId()).isEqualTo(one.getId());
    assertThat(m2.getName()).isEqualTo(one.getName());
    assertThat(m2.getChild()).isNull();
  }

}
