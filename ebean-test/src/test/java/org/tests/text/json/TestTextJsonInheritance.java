package org.tests.text.json;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.ebean.DB;
import io.ebean.text.json.JsonContext;
import io.ebean.xtest.BaseTestCase;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.*;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestTextJsonInheritance extends BaseTestCase {

  @Test
  public void parseJson_when_inheritanceType_outOfOrderDtype() {

    String fom = "{\"id\":90,\"name\":\"Frank\",\"vehicle\":{\"id\":42,\"licenseNumber\":\"T100\",\"capacity\":99.0,\"dtype\":\"T\"}}";

    VehicleDriver driver1 = DB.json().toBean(VehicleDriver.class, fom);
    assertThat(driver1.getVehicle()).isInstanceOf(Truck.class);
    assertThat(driver1.getVehicle().getLicenseNumber()).isEqualTo("T100");
  }

  @Test
  public void test() {

    setupData();

    List<Vehicle> list = DB.find(Vehicle.class).setAutoTune(false).findList();

    assertEquals(2, list.size());

    JsonContext jsonContext = DB.json();
    String jsonString = jsonContext.toJson(list);

    List<Vehicle> rebuiltList = jsonContext.toList(Vehicle.class, jsonString);

    assertEquals(2, rebuiltList.size());
  }

  @Test
  @Disabled("Run manually")
  public void testPerformance() throws IOException {
    for (int i = 0; i < 100000; i++) {
      Truck c = new Truck();
      c.setLicenseNumber("L " + i);
      c.setCapacity(20D);
      DB.save(c);
    }
    DB.update(Truck.class).set("capacity", 40D).update();
    List<Vehicle> list = DB.find(Vehicle.class).findList();
    assertThat(list).hasSize(100000);
    JsonContext jsonContext = DB.json();
    JsonFactory jsonFactory = new JsonFactory();
    for (int i = 0; i < 1; i++) {
      long start = System.nanoTime();
      try (OutputStream out = new GZIPOutputStream(new FileOutputStream("temp.json.gz"));
           JsonGenerator gen = jsonFactory.createGenerator(out, JsonEncoding.UTF8)) {
        jsonContext.toJson(list, gen);
        start = System.nanoTime() - start;
        System.out.println("Serializing " + 100_000_000_000_000L / start + " Entities/s");
      }
    }

    ObjectMapper mapper = new ObjectMapper();
    for (int i = 0; i < 10; i++) {
      long start = System.nanoTime();
      try (InputStream in = new GZIPInputStream(new FileInputStream("temp.json.gz"));
           JsonParser parser = mapper.createParser(in)) {
        parser.nextToken();
        Vehicle v;
        List<Vehicle> batch = new ArrayList<>();
        List<Object> batchId = new ArrayList<>();
        while ((v = read(parser)) != null) {
          batchId.add(DB.beanId(v));
          batch.add(v);
          if (batch.size() == 100) {
            Map<Object, Vehicle> vDb = DB.find(Vehicle.class).where().idIn(batchId).findMap();
            for (Vehicle vehicle : batch) {
              Vehicle db = vDb.get(vehicle.getId());
              DB.getDefault().pluginApi().mergeBeans(vehicle, db, null);
            }
            DB.saveAll(vDb.values());
            batch.clear();
            batchId.clear();
          }
        }
        //jsonContext.toList(Vehicle.class, parser);
        start = System.nanoTime() - start;
        System.out.println("DeSerializing " + 100_000_000_000_000L / start + " Entities/s");
      }
    }
  }

  private Vehicle read(JsonParser parser) throws IOException {
    JsonToken token = parser.currentToken();
    if (token == null || token == JsonToken.START_ARRAY || token == JsonToken.END_OBJECT) {
      // first invocation
      token = parser.nextToken();
    }
    if (token == JsonToken.START_OBJECT) {
      Vehicle ret = DB.json().toBean(Vehicle.class, parser);
      return ret;
    }
    return null;
  }

  private void setupData() {
    DB.createUpdate(CarAccessory.class, "delete from CarAccessory").execute();
    DB.createUpdate(CarFuse.class, "delete from CarFuse").execute();
    DB.createUpdate(Trip.class, "delete from trip").execute();
    DB.createUpdate(VehicleDriver.class, "delete from vehicleDriver").execute();
    DB.createUpdate(Vehicle.class, "delete from vehicle").execute();

    Car c = new Car();
    c.setLicenseNumber("C6788");
    c.setDriver("CarDriver");
    DB.save(c);

    Truck t = new Truck();
    t.setLicenseNumber("T1098");
    t.setCapacity(20D);
    DB.save(t);
  }
}
