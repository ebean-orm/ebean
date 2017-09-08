package org.tests.inheritance;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.EbeanServer;
import org.tests.inheritance.model.CalculationResult;
import org.tests.inheritance.model.Configurations;
import org.tests.inheritance.model.GroupConfiguration;
import org.tests.inheritance.model.ProductConfiguration;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class TestInheritanceJoins extends BaseTestCase {

  @Test
  public void testAssocOne() {

    EbeanServer server = Ebean.getDefaultServer();

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

    EbeanServer server = Ebean.getDefaultServer();

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
    Assert.assertEquals(group.getId(), gc.getId());
  }

  @Test
  public void testAssocOneWithNullAssoc() {

		/* Ensures the fetch join to a property with inheritance work as a left join */

    EbeanServer server = Ebean.getServer(null);

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

    EbeanServer server = Ebean.getServer(null);

    server.save(configurations);


    final GroupConfiguration gc = new GroupConfiguration("GC1");
    configurations.add(gc);


    server.save(gc);


    Configurations configurationsQueried = server.find(Configurations.class, configurations.getId());

    List<GroupConfiguration> groups = configurationsQueried.getGroupConfigurations();

    Assert.assertTrue(!groups.isEmpty());
  }

  @Test
  public void testAssocManyWithNoneRelated() {
    Configurations configurations = new Configurations();

    EbeanServer server = Ebean.getServer(null);

    server.save(configurations);

    Configurations configurationsQueried = server.find(Configurations.class).fetch("groupConfigurations").where().idEq(configurations.getId()).findOne();

    Assert.assertNotNull(configurationsQueried);
  }

}
