package org.tests.text.json;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.text.json.JsonIOException;
import io.ebean.text.json.JsonReadOptions;
import io.ebean.text.json.JsonWriteOptions;

import org.tests.inheritance.abstrakt.AbstractBaseBlock;
import org.tests.inheritance.abstrakt.Block;
import org.tests.model.basic.Order;
import org.tests.model.basic.ResetBasicData;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

/**
 * This test case demonstrates, what happens in the lifetime of json models, and how the JSON-migration works.
 *
 * (it is a good idea, to keep a version information from the beginning.)
 *
 * @author Roland Praml, FOCONIS AG
 *
 */
public class TestJsonVersioning extends BaseTestCase {

  @Before
  public void prepare() {
    ResetBasicData.reset();
    Ebean.getServerCacheManager().getBeanCache(Order.class).clear();
  }
  @Test
  public void testWriteVersion() throws IOException {

    Order o = Ebean.find(Order.class).setId(1).fetch("shipments").findOne();

    JsonWriteOptions jsonOpts = new JsonWriteOptions();
    jsonOpts.setVersionWriter(new ExampleJsonVersionWriter());
    String s = Ebean.json().toJson(o, jsonOpts);

    s = s.replaceAll("\\d{5,}", "XXXX"); // replaces all timestamps
    assertThat(s).isEqualTo("{\"_v\":23,\"id\":1,\"status\":\"NEW\",\"orderDate\":XXXX,\"shipDate\":null,"
        + "\"customer\":{\"_v\":1,\"id\":1},\"customerName\":\"Rob\","
        + "\"shipments\":[{\"_v\":17,\"id\":1,\"shipTime\":XXXX,\"cretime\":XXXX,\"updtime\":XXXX,\"version\":1}],"
        + "\"cretime\":XXXX,\"updtime\":XXXX,\"totalAmount\":null,\"totalItems\":null}");

  }

  @Test
  public void testMigrateVersion22_15() throws IOException {

    String s = "{\"_v\":22,\"id\":101,\"status\":\"NEW\",\"orderDate\":1519772400000,\"shipDate\":null,"
        + "\"customer\":{\"_v\":1,\"id\":101},\"customerName\":\"Roland\","
        + "\"shipmetns\":[{\"_v\":15,\"id\":101,\"shipTime\":1519772400000,\"createdtime\":1519012300,\"updatedtime\":1519012400,\"version\":1}],"
        + "\"cretime\":1519772400000,\"updtime\":1519772400000,\"totalAmount\":null,\"totalItems\":null}";

    JsonReadOptions options = new JsonReadOptions();
    options.setVersionMigrationHandler(new ExampleJsonVersionMigrationHandler());
    Order o = Ebean.json().toBean(Order.class, s, options);
    assertThat(o.getCustomerName()).isEqualTo("Roland");
    assertThat(o.getShipments()).hasSize(1);
    assertThat(o.getShipments().get(0).getCretime().getTime()).isEqualTo(1519012300000L);
    assertThat(o.getShipments().get(0).getUpdtime().getTime()).isEqualTo(1519012400000L);

  }

  @Test
  public void testMigrateVersion23_15() throws IOException {

    String s = "{\"_v\":23,\"id\":101,\"status\":\"NEW\",\"orderDate\":1519772400000,\"shipDate\":null,"
        + "\"customer\":{\"_v\":1,\"id\":101},\"customerName\":\"Roland\","
        + "\"shipments\":[{\"_v\":15,\"id\":101,\"shipTime\":1519772400000,\"createdtime\":1519012300,\"updatedtime\":1519012400,\"version\":1}],"
        + "\"cretime\":1519772400000,\"updtime\":1519772400000,\"totalAmount\":null,\"totalItems\":null}";

    JsonReadOptions options = new JsonReadOptions();
    options.setVersionMigrationHandler(new ExampleJsonVersionMigrationHandler());
    Order o = Ebean.json().toBean(Order.class, s, options);
    assertThat(o.getCustomerName()).isEqualTo("Roland");
    assertThat(o.getShipments()).hasSize(1);
    assertThat(o.getShipments().get(0).getCretime().getTime()).isEqualTo(1519012300000L);
    assertThat(o.getShipments().get(0).getUpdtime().getTime()).isEqualTo(1519012400000L);

  }

  @Test
  public void testMigrateVersion23_16() throws IOException {

    String s = "{\"_v\":23,\"id\":101,\"status\":\"NEW\",\"orderDate\":1519772400000,\"shipDate\":null,"
        + "\"customer\":{\"_v\":1,\"id\":101},\"customerName\":\"Roland\","
        + "\"shipments\":[{\"_v\":16,\"id\":101,\"shipTime\":1519772400000,\"createdtime\":1519012300000,\"updtime\":1519012400000,\"version\":1}],"
        + "\"cretime\":1519772400000,\"updtime\":1519772400000,\"totalAmount\":null,\"totalItems\":null}";

    JsonReadOptions options = new JsonReadOptions();
    options.setVersionMigrationHandler(new ExampleJsonVersionMigrationHandler());
    Order o = Ebean.json().toBean(Order.class, s, options);
    assertThat(o.getCustomerName()).isEqualTo("Roland");
    assertThat(o.getShipments()).hasSize(1);
    assertThat(o.getShipments().get(0).getCretime().getTime()).isEqualTo(1519012300000L);
    assertThat(o.getShipments().get(0).getUpdtime().getTime()).isEqualTo(1519012400000L);

  }

  @Test
  public void testMigrateVersion23_17() throws IOException {

    String s = "{\"_v\":23,\"id\":101,\"status\":\"NEW\",\"orderDate\":1519772400000,\"shipDate\":null,"
        + "\"customer\":{\"_v\":1,\"id\":101},\"customerName\":\"Roland\","
        + "\"shipments\":[{\"_v\":17,\"id\":101,\"shipTime\":1519772400000,\"cretime\":1519012300000,\"updtime\":1519012400000,\"version\":1}],"
        + "\"cretime\":1519772400000,\"updtime\":1519772400000,\"totalAmount\":null,\"totalItems\":null}";

    JsonReadOptions options = new JsonReadOptions();
    options.setVersionMigrationHandler(new ExampleJsonVersionMigrationHandler());
    Order o = Ebean.json().toBean(Order.class, s, options);
    assertThat(o.getCustomerName()).isEqualTo("Roland");
    assertThat(o.getShipments()).hasSize(1);
    assertThat(o.getShipments().get(0).getCretime().getTime()).isEqualTo(1519012300000L);
    assertThat(o.getShipments().get(0).getUpdtime().getTime()).isEqualTo(1519012400000L);

  }

  @Test(expected = JsonIOException.class)
  public void testMigrateVersion23_18() throws IOException {

    String s = "{\"_v\":23,\"id\":101,\"status\":\"NEW\",\"orderDate\":1519772400000,\"shipDate\":null,"
        + "\"customer\":{\"_v\":1,\"id\":101},\"customerName\":\"Roland\","
        + "\"shipments\":[{\"_v\":18,\"id\":101,\"shipTime\":1519772400000,\"cretime\":1519772400000,\"updtime\":1519772400000,\"version\":1}],"
        + "\"cretime\":1519772400000,\"updtime\":1519772400000,\"totalAmount\":null,\"totalItems\":null}";

    JsonReadOptions options = new JsonReadOptions();
    options.setVersionMigrationHandler(new ExampleJsonVersionMigrationHandler());
    Ebean.json().toBean(Order.class, s, options);

  }

  @Test
  public void testWriteVersionInherit() throws IOException {

    Block block = new Block();
        block.setId(1);
    block.setName("Test");

    JsonWriteOptions jsonOpts = new JsonWriteOptions();
    jsonOpts.setVersionWriter(new ExampleJsonVersionWriter());
    String s = Ebean.json().toJson(block, jsonOpts);

    assertThat(s).isEqualTo("{\"_v\":3,\"case_type\":\"2\",\"id\":1,\"name\":\"Test\"}");

  }

  @Test
  public void testReadVersionReadConcrete() throws IOException {

    String s = "{\"_v\":3,\"case_type\":\"2\",\"id\":1,\"name\":\"Test\"}";
    JsonReadOptions options = new JsonReadOptions();
    options.setVersionMigrationHandler(new ExampleJsonVersionMigrationHandler());
    Block block = Ebean.json().toBean(Block.class, s, options);


    assertThat(block.getName()).isEqualTo("Test");

  }

  @Test
  public void testReadVersionReadAbstract() throws IOException {

    String s = "{\"_v\":3,\"case_type\":\"2\",\"id\":1,\"name\":\"Test\"}";
    JsonReadOptions options = new JsonReadOptions();
    options.setVersionMigrationHandler(new ExampleJsonVersionMigrationHandler());
    AbstractBaseBlock block = Ebean.json().toBean(AbstractBaseBlock.class, s, options);

    assertThat(block).isInstanceOf(Block.class);
    assertThat(block.getName()).isEqualTo("Test");

  }

  @Test
  public void testReadVersionReadAbstractOldVersion() throws IOException {

    String s = "{\"_v\":2,\"case_type\":\"2\",\"id\":1,\"xxx\":\"Test\"}";
    JsonReadOptions options = new JsonReadOptions();
    options.setVersionMigrationHandler(new ExampleJsonVersionMigrationHandler());
    AbstractBaseBlock block = Ebean.json().toBean(AbstractBaseBlock.class, s, options);

    assertThat(block).isInstanceOf(Block.class);
    assertThat(block.getName()).isEqualTo("Test");

  }

  @Test
  public void testReadVersionReadAbstractOlderVersion() throws IOException {

    String s = "{\"_v\":1,\"id\":1,\"xxx\":\"Test\"}";
    JsonReadOptions options = new JsonReadOptions();
    options.setVersionMigrationHandler(new ExampleJsonVersionMigrationHandler());
    AbstractBaseBlock block = Ebean.json().toBean(AbstractBaseBlock.class, s, options);

    assertThat(block).isInstanceOf(Block.class);
    assertThat(block.getName()).isEqualTo("Test");

  }

  @Test
  public void testReadVersionReadAbstractNoVersion() throws IOException {

    String s = "{\"id\":1,\"xxx\":\"Test\"}";
    JsonReadOptions options = new JsonReadOptions();
    options.setVersionMigrationHandler(new ExampleJsonVersionMigrationHandler());
    AbstractBaseBlock block = Ebean.json().toBean(AbstractBaseBlock.class, s, options);

    assertThat(block).isInstanceOf(Block.class);
    assertThat(block.getName()).isEqualTo("Test");

  }
}
