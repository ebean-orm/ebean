package org.tests.unidirectional;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import org.tests.model.basic.UTDetail;
import org.tests.model.basic.UTMaster;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TestUniDirectional extends BaseTestCase {

  @Test
  public void test() {

    UTMaster m = new UTMaster();
    m.setName("mast1");
    m.addDetail(new UTDetail("d11", 10, 20.50));
    m.addDetail(new UTDetail("d12", 3, 5.50));
    m.addDetail(new UTDetail("d13", 5, 1.0));

    DB.save(m);

    assertNotNull(m.getId());
    assertNotNull(m.getVersion());

    List<UTDetail> details = m.getDetails();
    for (UTDetail utDetail : details) {
      assertNotNull(utDetail.getId());
      assertNotNull(utDetail.getVersion());
    }

    UTDetail d4 = new UTDetail("d14", 2, 3.0);
    m.addDetail(d4);

    DB.save(m);

    assertNotNull(d4.getId());
    Integer d4ver = d4.getVersion();
    assertNotNull(d4ver);

    d4.setName("d14Upd");
    DB.save(d4);
    Integer d4ver2 = d4.getVersion();
    assertNotNull(d4ver2);
    assertEquals((d4ver + 1), d4ver2.intValue());

    DB.delete(d4);
  }
}
