package org.tests.inheritance;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.RawSql;
import io.ebean.RawSqlBuilder;
import org.junit.Assert;
import org.junit.Test;
import org.tests.model.basic.Truck;
import org.tests.model.basic.Vehicle;
import org.tests.model.basic.VehicleLeaseLong;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestInheritanceRawSql extends BaseTestCase {

  @Test
  public void test() {

    Truck truck = new Truck();
    truck.setCapacity(50D);
    truck.setLicenseNumber("ASB23");

    Ebean.save(truck);


    String sql = "select dtype, id, license_number from vehicle where id = :id";
    RawSqlBuilder rawSqlBuilder = RawSqlBuilder.parse(sql);

    RawSql rawSql = rawSqlBuilder.create();

    List<Vehicle> list = Ebean.find(Vehicle.class)
      .setRawSql(rawSql)
      .setParameter("id", truck.getId())
      .findList();

    Assert.assertEquals(1, list.size());

    Vehicle vehicle2 = list.get(0);
    Assert.assertTrue(vehicle2 instanceof Truck);

    Truck truck2 = (Truck) vehicle2;
    Assert.assertEquals("ASB23", truck2.getLicenseNumber());

    // invoke lazy loading and set the capacity
    truck2.setCapacity(30D);

    // and now save
    Ebean.save(truck2);
  }

  @Test
  public void testJoinToInheritance() {

    VehicleLeaseLong longLease = new VehicleLeaseLong();
    longLease.setName("Long one");
    longLease.setBond(new BigDecimal("1000"));
    longLease.setMinDuration(100);
    longLease.setActiveEnd(LocalDate.now());

    Ebean.save(longLease);


    Truck truck = new Truck();
    truck.setCapacity(100D);
    truck.setLicenseNumber("ZK1");
    truck.setLease(longLease);

    Ebean.save(truck);


    String sql = "select v.dtype, v.id, v.license_number, l.dtype, l.id, l.name " +
      "from vehicle v " +
      "join vehicle_lease l on l.id = v.lease_id " +
      "where v.id = ?";

    RawSql rawSql = RawSqlBuilder.parse(sql)
      .tableAliasMapping("v", null)
      .tableAliasMapping("l", "lease")
      .create();

    Vehicle veh = Ebean.find(Vehicle.class)
      .setRawSql(rawSql)
      .setParameter(1, truck.getId())
      .findOne();

    assertThat(veh).isNotNull();
    assertThat(veh.getLicenseNumber()).isEqualTo("ZK1");
    assertThat(veh.getLease().getName()).isEqualTo("Long one");


    RawSql rawSql2 = RawSqlBuilder.parse(sql)
      .columnMappingIgnore("v.dtype")
      .columnMapping("v.id", "id")
      .columnMapping("v.license_number", "licenseNumber")
      .columnMappingIgnore("l.dtype")
      .columnMapping("l.id", "lease.id")
      .columnMapping("l.name", "lease.name")
      .create();

    Vehicle veh2 = Ebean.find(Vehicle.class)
      .setRawSql(rawSql2)
      .setParameter(1, truck.getId())
      .findOne();

    assertThat(veh2).isNotNull();
    assertThat(veh2.getLicenseNumber()).isEqualTo("ZK1");
    assertThat(veh2.getLease().getName()).isEqualTo("Long one");
  }

}
