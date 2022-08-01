package org.tests.model.virtualprop.ext;

import io.ebean.DB;
import io.ebean.Query;
import io.ebean.plugin.BeanType;
import io.ebean.plugin.Property;
import io.ebean.test.LoggedSql;
import io.ebeaninternal.server.deploy.BeanProperty;
import org.junit.jupiter.api.Test;
import org.tests.model.virtualprop.VirtualBase;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * @author Roland Praml, FOCONIS AG
 */
public class TestVirtualProps {

  @Test
  void testCreate() {

    DB.getDefault(); // Init database to start parser
    VirtualBase base = new VirtualBase();
    base.setData("Foo");

    DB.save(base);

    VirtualBase found = DB.find(VirtualBase.class).where().isNull("virtualExtendOne").findOne();
    assertThat(found).isNotNull();

    found = DB.find(VirtualBase.class).where().isNotNull("virtualExtendOne").findOne();
    assertThat(found).isNull();

    BeanType<VirtualBase> bt = DB.getDefault().pluginApi().beanType(VirtualBase.class);
    BeanProperty prop = (BeanProperty) bt.property("virtualExtendOne");


    found = DB.find(VirtualBase.class).where().isNull("virtualExtendOne").findOne();
    VirtualExtendOne ext = new VirtualExtendOne();
    ext.setData("bar");
    prop.pathSet(found, ext);
    DB.save(found);


    found = DB.find(VirtualBase.class).where().eq("virtualExtendOne.data", "bar").findOne();
    assertThat(found).isNotNull();

    List<Object> attr = DB.find(VirtualBase.class).fetch("virtualExtendOne", "data").findSingleAttributeList();
    assertThat(attr).containsExactly("bar");

    VirtualExtendOne  oneFound = (VirtualExtendOne) prop.pathGet(found);
    assertThat(oneFound.getData()).isEqualTo("foo");

  }

  @Test
  void testCreateDelete()  {
    DB.getDefault(); // Init database to start parser
    VirtualBase base = new VirtualBase();
    base.setData("Master");
    DB.save(base);

    VirtualExtendOne extendOne = new VirtualExtendOne();
    extendOne.setBase(base);
    extendOne.setData("Extended");
    DB.save(extendOne);

    VirtualBase found = DB.find(VirtualBase.class, base.getId());

    LoggedSql.start();
    DB.delete(found);
    List<String> sql = LoggedSql.stop();
    assertThat(sql).hasSize(2);
    assertThat(sql.get(0)).contains("delete from virtual_extend_one where id = ?");
    assertThat(sql.get(1)).contains("delete from virtual_base where id=?");

  }
}
