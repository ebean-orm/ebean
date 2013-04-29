package com.avaje.tests.singleTableInheritance;

import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.tests.singleTableInheritance.model.PalletLocation;
import com.avaje.tests.singleTableInheritance.model.PalletLocationExternal;
import com.avaje.tests.singleTableInheritance.model.Zone;
import com.avaje.tests.singleTableInheritance.model.ZoneExternal;

public class TestInheritQuery extends BaseTestCase {

  @Test
  public void test() {

    ZoneExternal zone = new ZoneExternal();
    zone.setAttribute("ABC");
    Ebean.save(zone);

    PalletLocationExternal location = new PalletLocationExternal();
    location.setZone(zone);
    location.setAttribute("123");
    Ebean.save(location);

    // This line should work too:
    List<PalletLocation> locations = Ebean.find(PalletLocation.class).where().eq("zone", zone)
        .findList();
    // List<PalletLocation> locations =
    // Ebean.find(PalletLocation.class).where().eq("zone.id",
    // zone.getId()).findList();

    Assert.assertNotNull(locations);
    Assert.assertEquals(1, locations.size());
    PalletLocation rereadLoc = locations.get(0);
    Assert.assertTrue(rereadLoc instanceof PalletLocation);
    Zone rereadZone = rereadLoc.getZone();
    Assert.assertNotNull(rereadZone);
    Assert.assertTrue(rereadZone instanceof ZoneExternal);
  }
}