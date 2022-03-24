package org.tests.singleTableInheritance;

import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
import org.junit.jupiter.api.Test;
import org.tests.singleTableInheritance.model.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TestInheritQuery extends BaseTestCase {

  @Test
  public void test() {
    ZoneExternal zone = new ZoneExternal();
    zone.setAttribute("ABC");
    DB.save(zone);

    PalletLocationExternal location = new PalletLocationExternal();
    location.setZone(zone);
    location.setAttribute("123");
    DB.save(location);

    // This line should work too:
    List<PalletLocation> locations = DB.find(PalletLocation.class).where().eq("zone", zone)
      .findList();
    // List<PalletLocation> locations =
    // DB.find(PalletLocation.class).where().eq("zone.id",
    // zone.getId()).findList();

    assertNotNull(locations);
    assertEquals(1, locations.size());
    PalletLocation rereadLoc = locations.get(0);
    assertTrue(rereadLoc instanceof PalletLocation);
    Zone rereadZone = rereadLoc.getZone();
    assertNotNull(rereadZone);
    assertTrue(rereadZone instanceof ZoneExternal);
  }

  @SuppressWarnings("unlikely-arg-type")
  @Test
  public void testDiscriminator_bug417() {


    DB.deleteAll(DB.find(Warehouse.class).findList());
    DB.deleteAll(DB.find(PalletLocation.class).findList());
    DB.deleteAll(DB.find(Zone.class).findList());
    DB.deleteAll(DB.find(ZoneInternal.class).findList());
    DB.deleteAll(DB.find(ZoneInternal.class).findList());

    ZoneInternal zoneInt = new ZoneInternal();
    zoneInt.setAttribute("some zone 1");
    DB.save(zoneInt);

    ZoneExternal zoneExt = new ZoneExternal();
    zoneExt.setAttribute("some zone 2");
    DB.save(zoneExt);

    // queries of Zone and subclasses as root node of query

    // query abstract class on attribute (root of heirarchy)
    List<Zone> zones = DB.find(Zone.class).where().startsWith("attribute", "some zone").findList();
    // select t0.type c0, t0.ID c1, t0.attribute c2, t0.attribute c3 from zones t0 where t0.attribute like ? ; --bind(some zone%)
    assertEquals(2, zones.size());
    assertTrue(zones.contains(zoneInt));
    assertTrue(zones.contains(zoneExt));

    // query internal zones only
    // discriminator is in WHERE clause where it belongs
    List<ZoneInternal> internalZones = DB.find(ZoneInternal.class).where().startsWith("attribute", "some zone").findList();
    // select t0.type c0, t0.ID c1, t0.attribute c2 from zones t0 where t0.type = 'INT'  and t0.attribute like ? ; --bind(some zone%)
    assertEquals(1, internalZones.size());
    assertTrue(internalZones.contains(zoneInt));
    assertFalse(internalZones.contains(zoneExt));
    assertTrue(internalZones.get(0) instanceof ZoneInternal);

    // query external zones only
    List<ZoneExternal> externalZones = DB.find(ZoneExternal.class).where().startsWith("attribute", "some zone").findList();
    // select t0.type c0, t0.ID c1, t0.attribute c2 from zones t0 where t0.type = 'EXT'  and t0.attribute like ? ; --bind(some zone%)
    assertEquals(1, externalZones.size());
    assertTrue(externalZones.contains(zoneExt));
    assertFalse(externalZones.contains(zoneInt));
    assertTrue(externalZones.get(0) instanceof ZoneExternal);

    // parents with children of Zones and subclasses

    Warehouse wh = new Warehouse();
    wh.setOfficeZone(zoneInt); // many-to-one
    wh.getShippingZones().add(zoneExt); // many-to-many
    DB.save(wh);

    // JOIN clause, no discriminator
    // parent with many-to-one, doesn't put in discriminator, why not, PK sufficient?
    // eager join
    Warehouse wh2 = DB.find(Warehouse.class, wh.getId());
    assertNotNull(wh2);
    assertEquals(wh.getId(), wh2.getId());
    assertEquals(wh.getOfficeZone(), wh2.getOfficeZone());
    assertEquals(wh.getOfficeZone().getAttribute(), wh2.getOfficeZone().getAttribute());

    // before the fix, next assertion runs this lazy query:

    //   select t0.ID c0, t1.type c1, t1.ID c2 from warehouses t0
    //    left join WarehousesShippingZones t1z_ on t1z_.warehouseId = t0.ID
    //    left join zones t1 on t1.ID = t1z_.shippingZoneId
    //    where t1.type = 'EXT' // this should be in the join clause
    //      and t0.ID = ?
    //    order by t0.ID; --bind(1)

    // this works here because we have at least one shipping zone

    assertEquals(1, wh2.getShippingZones().size());
    assertTrue(wh2.getShippingZones().contains(zoneExt));

    // set optional concrete to null to set stage for failure
    wh.setOfficeZone(null);
    DB.save(wh);

    // no discriminator here
    wh2 = DB.find(Warehouse.class)
      .where().eq("id", wh.getId())
      .findOne();

    assertNotNull(wh2);
    // discriminator is used here, should be in join
    // assuming this "manual" fetch is equivalent to autofetch (i.e., autofetch should work the same way)
    // before Daryl's fix
    // select t0.ID c0, t1.type c1, t1.ID c2, t1.attribute c3 from warehouses t0 left join zones t1 on t1.ID = t0.officeZoneId  where t1.type = 'INT'  and t0.ID = ?
    // todo: after Daryl's fix, not sure if this is proper, no discriminator at all, isn't PK/FK sufficient?
    // select t0.ID c0, t1.type c1, t1.ID c2, t1.attribute c3 from warehouses t0 left join zones t1 on t1.ID = t0.officeZoneId  where t0.ID = ? ; --bind(1)
    wh2 = DB.find(Warehouse.class)
      .fetch("officeZone")
      .where().eq("id", wh.getId())
      .findOne();
    // key assertion #1 - fails due to left join with discriminator in WHERE
    assertNotNull(wh2);

    // clear children to set the stage for left join failure
    wh.getShippingZones().clear();
    DB.save(wh);

    wh2 = DB.find(Warehouse.class, wh.getId());
    assertNotNull(wh2);
    assertEquals(wh.getId(), wh2.getId());
    assertEquals(0, wh.getShippingZones().size());

    // query with lazy load of abstract children
    wh = DB.find(Warehouse.class)
      .where().eq("id", wh.getId())
      .findOne();
    assertNotNull(wh);
    assertEquals(wh.getId(), wh2.getId());
    assertEquals(0, wh.getShippingZones().size());

    // query with fetch of abstract children
    wh = DB.find(Warehouse.class)
      .fetch("shippingZones")
      .where().eq("id", wh.getId())
      .findOne();
    // key assertion #2 - fails due to left join with discriminator in WHERE
    assertNotNull(wh);
    assertEquals(wh.getId(), wh2.getId());
    assertEquals(0, wh.getShippingZones().size());


  }
}
