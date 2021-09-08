package org.tests.inheritance;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import io.ebean.Database;
import org.junit.jupiter.api.Test;
import org.tests.inheritance.model.CalculationResult;
import org.tests.inheritance.model.Configurations;
import org.tests.inheritance.model.GroupConfiguration;
import org.tests.inheritance.model.ProductConfiguration;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TestInheritanceJoins extends BaseTestCase {

  @Test
  public void testAssocOne() {

    Database server = DB.getDefault();

    ProductConfiguration pc = new ProductConfiguration();
    pc.setName("PC1");
    server.save(pc);

    GroupConfiguration gc = new GroupConfiguration();
    gc.setName("GC1");
    server.save(gc);

    CalculationResult r = new CalculationResult();
    r.setCharge(100.0);
    r.setProductConfiguration(pc);
    r.setGroupConfiguration(gc);
    server.save(r);
  }

  @Test
  public void assocOne_when_null() {

    Database server = DB.getDefault();

    GroupConfiguration gc = new GroupConfiguration();
    gc.setName("GC1");
    server.save(gc);

    CalculationResult r = new CalculationResult();
    r.setCharge(100.0);

    // @ManyToOne with inheritance and null
    r.setProductConfiguration(null);
    r.setGroupConfiguration(gc);
    server.save(r);

    CalculationResult result = server.find(CalculationResult.class, r.getId());

    GroupConfiguration group = result.getGroupConfiguration();
    assertEquals(group.getId(), gc.getId());
  }

  @Test
  public void testAssocOneWithNullAssoc() {

		/* Ensures the fetch join to a property with inheritance work as a left join */

    Database server = DB.getDefault();

    final ProductConfiguration pc = new ProductConfiguration();
    pc.setName("PC1");
    server.save(pc);

    CalculationResult r = new CalculationResult();
    final Double charge = 100.0;
    r.setCharge(charge);
    r.setProductConfiguration(pc);
    r.setGroupConfiguration(null);
    server.save(r);
  }

  @Test
  public void testAssocMany() {
    Configurations configurations = new Configurations();

    Database server = DB.getDefault();

    server.save(configurations);


    final GroupConfiguration gc = new GroupConfiguration("GC1");
    configurations.addGroupConfiguration(gc);


    server.save(gc);


    Configurations configurationsQueried = server.find(Configurations.class, configurations.getId());

    List<GroupConfiguration> groups = configurationsQueried.getGroupConfigurations();

    assertTrue(!groups.isEmpty());
  }

  @Test
  public void testAssocManyWithNoneRelated() {
    Configurations configurations = new Configurations();

    Database server = DB.getDefault();

    server.save(configurations);

    Configurations configurationsQueried = server.find(Configurations.class).fetch("groupConfigurations").where().idEq(configurations.getId()).findOne();

    assertNotNull(configurationsQueried);
  }

  @Test
  public void testMultipleAssocManyWithSameInheritanceBase() {
    Database database = DB.getDefault();

    Configurations configurations = new Configurations();
    database.save(configurations);

    GroupConfiguration gc = new GroupConfiguration();
    gc.setConfigurations(configurations);
    database.save(gc);

    ProductConfiguration pc = new ProductConfiguration();
    pc.setConfigurations(configurations);
    database.save(pc);

    Configurations configurationsQueried = database.find(Configurations.class).fetch("groupConfigurations").fetch("productConfigurations").where().idEq(configurations.getId()).findOne();

    assertEquals(Collections.singletonList(gc), configurationsQueried.getGroupConfigurations());
    assertEquals(Collections.singletonList(pc), configurationsQueried.getProductConfigurations());
  }

}
