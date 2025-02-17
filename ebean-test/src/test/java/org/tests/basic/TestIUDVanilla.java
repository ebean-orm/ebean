package org.tests.basic;

import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.EBasicVer;
import org.tests.model.basic.UTMaster;

import jakarta.persistence.OptimisticLockException;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public class TestIUDVanilla extends BaseTestCase {

  @Test
  public void test() {

    EBasicVer e0 = new EBasicVer("vanilla");

    e0.save();

    assertNotNull(e0.getId());
    assertNotNull(e0.getLastUpdate());

    Instant lastUpdate0 = e0.getLastUpdate();

    e0.setName("modified");
    e0.save();

    Instant lastUpdate1 = e0.getLastUpdate();
    assertNotNull(lastUpdate1);
    assertNotSame(lastUpdate0, lastUpdate1);

    EBasicVer e2 = DB.getDefault().createEntityBean(EBasicVer.class);

    e2.setId(e0.getId());
    e2.setLastUpdate(lastUpdate1);

    e2.setName("forcedUpdate");
    e2.update();

    EBasicVer e3 = new EBasicVer("ModNoOCC");
    e3.setId(e0.getId());

    e3.update();

    e3.setName("ModAgain");
    e3.setDescription("Banana");

    e3.update();

  }

  @Test
  public void stateless_noOCC() {

    EBasicVer e0 = new EBasicVer("vanilla");
    e0.save();

    EBasicVer e3 = new EBasicVer("ModNoOCC");
    e3.setId(e0.getId());
    e3.setLastUpdate(e0.getLastUpdate());

    e3.update();

    e3.setName("ModAgain");
    //e3.setDescription("Banana");

    e3.update();
  }

  @Test
  public void modifyVersion_expect_optimisticLock() {
    UTMaster e0 = new UTMaster("save me");
    e0.save();

    // for this case we know 42 should throw OptimisticLockException
    e0.setVersion(42);
    assertThrows(OptimisticLockException.class, () -> e0.update());
  }

  @Test
  public void testOptimisticLockException() {
    UTMaster e0 = new UTMaster("optLock");
    DB.save(e0);

    e0 = DB.find(UTMaster.class).where().eq("name", "optLock").findOne();
    UTMaster e1 = DB.find(UTMaster.class).where().eq("name", "optLock").findOne();

    int oldVersion = e0.getVersion();
    e0.setDescription("foo");
    e0.save();
    assertThat(e0.getVersion()).isGreaterThan(oldVersion);

    e1.setDescription("bar");
    oldVersion = e1.getVersion();
    assertThrows(OptimisticLockException.class, e1::save);
    // after optimisticLockExecption, a restore of version is expected
    assertThat(e1.getVersion()).isEqualTo(oldVersion);
    // and subsequent saves must fail
    assertThrows(OptimisticLockException.class, e1::save);
  }

}
