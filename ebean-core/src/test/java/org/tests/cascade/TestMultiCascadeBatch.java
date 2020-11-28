package org.tests.cascade;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import io.ebean.Ebean;
import io.ebean.Transaction;
import org.ebeantest.LoggedSqlCollector;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.tests.model.site.DataContainer;
import org.tests.model.site.Site;
import org.tests.model.site.SiteAddress;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestMultiCascadeBatch extends BaseTestCase {

  private Transaction txn;

  @Before
  public void before() {
    txn = Ebean.beginTransaction();
  }

  @After
  public void after() {
    if (txn != null) {
      txn.end();
    }
  }

  @Test
  public void save() {

    Site grandparent = new Site();
    grandparent.setName("grandparent");
    Site parent = new Site();
    parent.setName("parent");
    Site child = new Site();
    child.setName("child");
    grandparent.setChildren(Collections.singletonList(parent));
    parent.setChildren(Collections.singletonList(child));

    LoggedSqlCollector.start();
    grandparent.save();

    final List<String> sql = LoggedSqlCollector.stop();

    final List<Site> list = DB.find(Site.class).where()
      .idIn(grandparent.getId(), parent.getId(), child.getId())
      .findList();

    assertThat(list).hasSize(3);

    assertThat(sql).hasSize(5);
    assertSql(sql.get(0)).contains("insert into site (id, name");
    assertSql(sql.get(1)).contains("insert into site (id, name");
    assertSqlBind(sql.get(2));
    assertThat(sql.get(3)).contains("insert into site (id, name");
    assertSqlBind(sql.get(4));
  }

  @Test
  public void testMultipleCascadeInsideTransaction() {

    final Site mainSite = new Site();
    mainSite.setName("mainSite");
    Ebean.save(mainSite);

    // create child including data
    final Site childSite = new Site();
    childSite.setName("childSite");

    final SiteAddress childAddress = new SiteAddress();
    childAddress.setCity("Some city");
    childAddress.setStreet("some street");
    childAddress.setZipCode("12345");
    childSite.setSiteAddress(childAddress);

    final DataContainer dataContainer = new DataContainer();
    dataContainer.setContent("container content");
    childSite.setDataContainer(dataContainer);

    mainSite.getChildren().add(childSite);
    mainSite.setName("a different name");

    Ebean.save(mainSite);
  }

}
