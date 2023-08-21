package org.tests.query;

import io.ebean.DB;
import io.ebean.test.LoggedSql;
import io.ebean.xtest.BaseTestCase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Order;
import org.tests.model.basic.Person;
import org.tests.model.basic.ResetBasicData;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestQuerySubquery extends BaseTestCase {

    private Long robId;

    @BeforeEach
    public void setup() {
        ResetBasicData.reset();

        Person rob = new Person();
        rob.setName("Rob");
        rob.setSurname("Test");
        DB.save(rob);
        robId = rob.getId();

        DB.getDefault().pluginApi().cacheManager().clearAll();
    }

    @AfterEach
    public void cleanup() {
        DB.delete(Person.class, robId);
    }

    @Test
    public void testWithoutSubquery() {

        Person contact = DB.find(Person.class)
                .where()
                .idEq(robId)
                .findOne();

        LoggedSql.start();
        List<Order> orders = DB.find(Order.class)
                .alias("t0")
                .where()
                .eq("customer.name", contact.getName())
                .findList();

        assertThat(orders).hasSize(3);
        assertThat(LoggedSql.stop())
                .hasSize(1)
                .first().asString()
                .contains("--bind("+ contact.getName() + ")")
                .contains("kcustomer_id where t1.name = ?");
    }

    @Test
    public void testEqSubqueryWithIdEq() {
        LoggedSql.start();
        List<Order> orders = DB.find(Order.class)
                .alias("t0")
                .where()
                .eq("customer.name", DB.find(Person.class).select("name").where().idEq(robId).query())
                .findList();

        assertThat(orders).hasSize(3);
        assertThat(LoggedSql.stop())
                .hasSize(1)
                .first().asString()
                .contains("--bind("+ robId + ")")
                .contains("name = (select t0.NAME");
    }

    @Test
    public void testEqSubqueryWithSetId() {
        LoggedSql.start();

        List<Order> orders = DB.find(Order.class)
                .alias("t0")
                .where()
                .eq("customer.name", DB.find(Person.class).select("name").setId(robId))
                .findList();

        assertThat(orders).hasSize(3);
        assertThat(LoggedSql.stop())
                .hasSize(1)
                .first().asString()
                .contains("--bind("+ robId + ")")
                .contains("name = (select t0.NAME");
    }

    @Test
    public void testEqSubqueryWithEqId() {
        LoggedSql.start();

        List<Order> orders = DB.find(Order.class)
                .alias("t0")
                .where()
                .eq("customer.name", DB.find(Person.class).select("name").where().eq("id", robId).query())
                .findList();

        assertThat(orders).hasSize(3);
        assertThat(LoggedSql.stop())
                .hasSize(1)
                .first().asString()
                .contains("--bind("+ robId + ")")
                .contains("name = (select t0.NAME");
    }

}
