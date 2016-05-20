package com.avaje.tests.query;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.Query;
import com.avaje.tests.model.basic.Order;
import com.avaje.tests.model.basic.ResetBasicData;
import com.avaje.tests.model.m2m.Role;
import org.junit.Assert;
import org.junit.Test;

public class TestQueryIsNull extends BaseTestCase {


    @Test
    public void queryShouldContainIsNullOnColumn() {
        ResetBasicData.reset();

        Query<Order> query = Ebean.find(Order.class).where().isNull("customerName").query();
        query.findList();

        Assert.assertTrue(query.getGeneratedSql().contains("name is null"));
    }

    @Test
    public void queryShouldLeftJoinForOneToManyInDisjunction() {
        ResetBasicData.reset();

        Query<Order> query = Ebean.find(Order.class).where().disjunction().isNull("details").endJunction().query();
        query.findList();

        Assert.assertTrue(query.getGeneratedSql().contains("left outer join"));
        Assert.assertTrue(query.getGeneratedSql().contains("is null"));
    }

    @Test
    public void queryShouldLeftJoinForOneToManyRelation() {
        ResetBasicData.reset();

        Query<Order> query = Ebean.find(Order.class).where().isNull("details").query();
        query.findList();

        Assert.assertTrue(query.getGeneratedSql().contains("left outer join"));
        Assert.assertTrue(query.getGeneratedSql().contains("is null"));

    }

    @Test
    public void queryShouldLeftJoinForManyToManyRelation() {
        ResetBasicData.reset();

        Query<Role> query = Ebean.find(Role.class).where().isNull("permissions").query();
        query.findList();

        Assert.assertTrue(query.getGeneratedSql().contains("left outer join"));
        Assert.assertTrue(query.getGeneratedSql().contains("is null"));
    }
}
