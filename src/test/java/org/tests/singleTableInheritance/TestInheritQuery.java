package org.tests.singleTableInheritance;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import org.tests.singleTableInheritance.model.PalletLocation;
import org.tests.singleTableInheritance.model.PalletLocationExternal;
import org.tests.singleTableInheritance.model.Warehouse;
import org.tests.singleTableInheritance.model.Zone;
import org.tests.singleTableInheritance.model.ZoneExternal;
import org.tests.singleTableInheritance.model.ZoneInternal;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

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

  @SuppressWarnings("unlikely-arg-type")
  @Test
  public void testDiscriminator_bug417() {


    Ebean.deleteAll(Ebean.find(Warehouse.class).findList());
    Ebean.deleteAll(Ebean.find(PalletLocation.class).findList());
    Ebean.deleteAll(Ebean.find(Zone.class).findList());
    Ebean.deleteAll(Ebean.find(ZoneInternal.class).findList());
    Ebean.deleteAll(Ebean.find(ZoneInternal.class).findList());

    ZoneInternal zoneInt = new ZoneInternal();
    zoneInt.setAttribute("some zone 1");
    Ebean.save(zoneInt);

    ZoneExternal zoneExt = new ZoneExternal();
    zoneExt.setAttribute("some zone 2");
    Ebean.save(zoneExt);

    // queries of Zone and subclasses as root node of query

    // query abstract class on attribute (root of heirarchy)
    List<Zone> zones = Ebean.find(Zone.class).where().startsWith("attribute", "some zone").findList();
    // select t0.type c0, t0.ID c1, t0.attribute c2, t0.attribute c3 from zones t0 where t0.attribute like ? ; --bind(some zone%)
    Assert.assertEquals(2, zones.size());
    Assert.assertTrue(zones.contains(zoneInt));
    Assert.assertTrue(zones.contains(zoneExt));

    // query internal zones only
    // discriminator is in WHERE clause where it belongs
    List<ZoneInternal> internalZones = Ebean.find(ZoneInternal.class).where().startsWith("attribute", "some zone").findList();
    // select t0.type c0, t0.ID c1, t0.attribute c2 from zones t0 where t0.type = 'INT'  and t0.attribute like ? ; --bind(some zone%)
    Assert.assertEquals(1, internalZones.size());
    Assert.assertTrue(internalZones.contains(zoneInt));
    Assert.assertFalse(internalZones.contains(zoneExt));
    Assert.assertTrue(internalZones.get(0) instanceof ZoneInternal);

    // query external zones only
    List<ZoneExternal> externalZones = Ebean.find(ZoneExternal.class).where().startsWith("attribute", "some zone").findList();
    // select t0.type c0, t0.ID c1, t0.attribute c2 from zones t0 where t0.type = 'EXT'  and t0.attribute like ? ; --bind(some zone%)
    Assert.assertEquals(1, externalZones.size());
    Assert.assertTrue(externalZones.contains(zoneExt));
    Assert.assertFalse(externalZones.contains(zoneInt));
    Assert.assertTrue(externalZones.get(0) instanceof ZoneExternal);

    // parents with children of Zones and subclasses

    Warehouse wh = new Warehouse();
    wh.setOfficeZone(zoneInt); // many-to-one
    wh.getShippingZones().add(zoneExt); // many-to-many
    Ebean.save(wh);

    // JOIN clause, no discriminator
    // parent with many-to-one, doesn't put in discriminator, why not, PK sufficient?
    // eager join
    Warehouse wh2 = Ebean.find(Warehouse.class, wh.getId());
    Assert.assertNotNull(wh2);
    Assert.assertEquals(wh.getId(), wh2.getId());
    Assert.assertEquals(wh.getOfficeZone(), wh2.getOfficeZone());
    Assert.assertEquals(wh.getOfficeZone().getAttribute(), wh2.getOfficeZone().getAttribute());

    // before the fix, next assertion runs this lazy query:

    //   select t0.ID c0, t1.type c1, t1.ID c2 from warehouses t0
    //    left join WarehousesShippingZones t1z_ on t1z_.warehouseId = t0.ID
    //    left join zones t1 on t1.ID = t1z_.shippingZoneId
    //    where t1.type = 'EXT' // this should be in the join clause
    //      and t0.ID = ?
    //    order by t0.ID; --bind(1)

    // this works here because we have at least one shipping zone

    Assert.assertEquals(1, wh2.getShippingZones().size());
    Assert.assertTrue(wh2.getShippingZones().contains(zoneExt));

    // set optional concrete to null to set stage for failure
    wh.setOfficeZone(null);
    Ebean.save(wh);

    // no discriminator here
    wh2 = Ebean.find(Warehouse.class)
      .where().eq("id", wh.getId())
      .findOne();

    Assert.assertNotNull(wh2);
    // discriminator is used here, should be in join
    // assuming this "manual" fetch is equivalent to autofetch (i.e., autofetch should work the same way)
    // before Daryl's fix
    // select t0.ID c0, t1.type c1, t1.ID c2, t1.attribute c3 from warehouses t0 left join zones t1 on t1.ID = t0.officeZoneId  where t1.type = 'INT'  and t0.ID = ?
    // todo: after Daryl's fix, not sure if this is proper, no discriminator at all, isn't PK/FK sufficient?
    // select t0.ID c0, t1.type c1, t1.ID c2, t1.attribute c3 from warehouses t0 left join zones t1 on t1.ID = t0.officeZoneId  where t0.ID = ? ; --bind(1)
    wh2 = Ebean.find(Warehouse.class)
      .fetch("officeZone")
      .where().eq("id", wh.getId())
      .findOne();
    // key assertion #1 - fails due to left join with discriminator in WHERE
    Assert.assertNotNull(wh2);

    // clear children to set the stage for left join failure
    wh.getShippingZones().clear();
    Ebean.save(wh);

    wh2 = Ebean.find(Warehouse.class, wh.getId());
    Assert.assertNotNull(wh2);
    Assert.assertEquals(wh.getId(), wh2.getId());
    Assert.assertEquals(0, wh.getShippingZones().size());

    // query with lazy load of abstract children
    wh = Ebean.find(Warehouse.class)
      .where().eq("id", wh.getId())
      .findOne();
    Assert.assertNotNull(wh);
    Assert.assertEquals(wh.getId(), wh2.getId());
    Assert.assertEquals(0, wh.getShippingZones().size());

    // query with fetch of abstract children
    wh = Ebean.find(Warehouse.class)
      .fetch("shippingZones")
      .where().eq("id", wh.getId())
      .findOne();
    // key assertion #2 - fails due to left join with discriminator in WHERE
    Assert.assertNotNull(wh);
    Assert.assertEquals(wh.getId(), wh2.getId());
    Assert.assertEquals(0, wh.getShippingZones().size());


  }
}
