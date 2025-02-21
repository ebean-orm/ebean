package org.tests.compositekeys;

import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
import io.ebean.Query;
import io.ebean.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.tests.compositekeys.db.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Test some of the Avaje core functionality in conjunction with composite keys like
 * <ul>
 * <li>write</li>
 * <li>find</li>
 * </ul>
 */
public class TestCore extends BaseTestCase {

  @BeforeEach
  public void setUp() throws Exception {
    DB.createUpdate(Item.class, "delete from Item").execute();
    DB.createUpdate(Region.class, "delete from Region").execute();
    DB.createUpdate(Type.class, "delete from Type").execute();
    DB.createUpdate(SubType.class, "delete from SubType").execute();

    try (Transaction tx = server().beginTransaction()) {
      SubType subType = new SubType();
      SubTypeKey subTypeKey = new SubTypeKey();
      subTypeKey.setSubTypeId(1);
      subType.setKey(subTypeKey);
      subType.setDescription("ANY SUBTYPE");
      server().save(subType);

      Type type = new Type();
      TypeKey typeKey = new TypeKey();
      typeKey.setCustomer(1);
      typeKey.setType(10);
      type.setKey(typeKey);
      type.setDescription("Type Old-Item - Customer 1");
      type.setSubType(subType);
      server().save(type);

      type = new Type();
      typeKey = new TypeKey();
      typeKey.setCustomer(2);
      typeKey.setType(10);
      type.setKey(typeKey);
      type.setDescription("Type Old-Item - Customer 2");
      type.setSubType(subType);
      server().save(type);

      Region region = new Region();
      RegionKey regionKey = new RegionKey();
      regionKey.setCustomer(1);
      regionKey.setType(500);
      region.setKey(regionKey);
      region.setDescription("Region West - Customer 1");
      server().save(region);

      region = new Region();
      regionKey = new RegionKey();
      regionKey.setCustomer(2);
      regionKey.setType(500);
      region.setKey(regionKey);
      region.setDescription("Region West - Customer 2");
      server().save(region);

      Item item = new Item();
      ItemKey itemKey = new ItemKey();
      itemKey.setCustomer(1);
      itemKey.setItemNumber("ITEM1");
      item.setKey(itemKey);
      item.setUnits("P");
      item.setDescription("Fancy Car - Customer 1");
      item.setRegion(500);
      item.setType(10);
      server().save(item);

      item = new Item();
      itemKey = new ItemKey();
      itemKey.setCustomer(2);
      itemKey.setItemNumber("ITEM1");
      item.setKey(itemKey);
      item.setUnits("P");
      item.setDescription("Another Fancy Car - Customer 2");
      item.setRegion(500);
      item.setType(10);
      server().save(item);

      tx.commit();
    }
  }

  @Test
  public void testFind() {

    List<Item> items = server().find(Item.class).findList();

    assertNotNull(items);
    assertEquals(2, items.size());

    Query<Item> qItems = server().find(Item.class);
//        qItems.where(Expr.eq("key.customer", Integer.valueOf(1)));

    // I want to discourage the direct use of Expr
    qItems.where().eq("key.customer", 1);
    items = qItems.findList();

    assertNotNull(items);
    assertEquals(1, items.size());
  }

  /**
   * This partially loads the item and then lazy loads the ManyToOne assoc
   */
  @Test
  public void testDoubleLazyLoad() {

    ItemKey itemKey = new ItemKey();
    itemKey.setCustomer(2);
    itemKey.setItemNumber("ITEM1");

    Item item = server().find(Item.class).select("description").where().idEq(itemKey).findOne();
    assertNotNull(item);
    assertNotNull(item.getUnits());
    assertEquals("P", item.getUnits());

    Type type = item.getEType();
    assertNotNull(type);
    assertNotNull(type.getDescription());

    SubType subType = type.getSubType();
    assertNotNull(subType);
    assertNotNull(subType.getDescription());
  }
  @Test
  public void testEmbeddedWithOrder() {

    List<Item> items = server().find(Item.class).orderBy("auditInfo.created asc, type asc").findList();

    assertNotNull(items);
    assertEquals(2, items.size());
  }

  @Test
  public void testFindAndOrderByEType() {

    List<Item> items = server().find(Item.class).orderBy("eType").findList();

    assertNotNull(items);
    assertEquals(2, items.size());
  }
}
