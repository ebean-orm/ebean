package org.tests.model.virtualprop.ext;

import io.ebean.DB;
import io.ebean.Database;
import io.ebean.bean.EntityBean;
import io.ebean.plugin.BeanType;
import io.ebean.test.LoggedSql;
import io.ebeaninternal.server.deploy.BeanProperty;
import org.junit.jupiter.api.Test;
import org.tests.model.virtualprop.AbstractVirtualBase;
import org.tests.model.virtualprop.VirtualBase;
import org.tests.model.virtualprop.VirtualBaseA;

import java.lang.reflect.Field;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * Demo, how to use virtual properties. Note: that there is
 *
 * @author Roland Praml, FOCONIS AG
 */
public class TestVirtualProps {

  // must be called BEFORE DB-Init.
  static String e2 = Extension3.foo() + Extension2.foo() + Extension1.foo();
  static Database db = DB.getDefault();

  /*
  private static Database createDb() {
    DatabaseConfig config = new DatabaseConfig();
    config.setName("h2");
    config.loadFromProperties();
    config.setDdlExtra(false);
    config.setPackages(List.of("org.tests.model.virtualprop"));
    return DatabaseFactory.create(config);
  }*/

  @Test
  void testCreate() throws NoSuchFieldException, IllegalAccessException {
    System.out.println(e2);
    //DB.getDefault(); // Init database to start parser
    VirtualBase base = new VirtualBase();
    base.setData("Foo");

    db.save(base);

    Field f = AbstractVirtualBase.class.getDeclaredField("_ebean_extension_storage");
    f.setAccessible(true);
    EntityBean[] extension_storage = (EntityBean[]) f.get(base);
    assertThat(extension_storage[0]).isInstanceOf(Extension1.class);
    assertThat(extension_storage[1]).isInstanceOf(Extension2.class);
    assertThat(extension_storage[2]).isInstanceOf(Extension3.class);

    VirtualBase found = db.find(VirtualBase.class).where().isNull("virtualExtendOne").findOne();
    assertThat(found).isNotNull();

    found = db.find(VirtualBase.class).where().isNotNull("virtualExtendOne").findOne();
    assertThat(found).isNull();

    BeanType<VirtualBase> bt = db.pluginApi().beanType(VirtualBase.class);
    BeanProperty prop = (BeanProperty) bt.property("virtualExtendOne");


    found = db.find(VirtualBase.class).where().isNull("virtualExtendOne").findOne();
    VirtualExtendOne ext = new VirtualExtendOne();
    ext.setData("bar");

    prop.pathSet(found, ext);
    db.save(found);

    Extension3 other = Extension3.get(found);
    assertThat(other.getVirtualExtendOne().getData()).isEqualTo("bar");
    other.setFirstName("test");

    Extension2 many = Extension2.get(found);
    assertThat(many.getVirtualExtendManyToManys()).isEmpty();
    other.getVirtualExtendOne().setData("faz");
    db.save(found);

    found = db.find(VirtualBase.class).where().eq("virtualExtendOne.data", "faz").findOne();
    assertThat(found).isNotNull();

    List<Object> attr = db.find(VirtualBase.class).fetch("virtualExtendOne", "data").findSingleAttributeList();
    assertThat(attr).containsExactly("faz");

    attr = db.find(VirtualBase.class).select("firstName").findSingleAttributeList();
    assertThat(attr).containsExactly("Your name is Foo");
    VirtualExtendOne oneFound = (VirtualExtendOne) prop.pathGet(found);
    assertThat(oneFound.getData()).isEqualTo("faz");

    db.delete(oneFound); // cleanup
  }

  @Test
  void testCreateMany() {

    VirtualBase base1 = new VirtualBase();
    base1.setData("Foo");
    db.save(base1);

    VirtualBase base2 = new VirtualBase();
    base2.setData("Bar");
    db.save(base2);

    VirtualExtendManyToMany many1 = new VirtualExtendManyToMany();
    many1.setData("Alex");
    db.save(many1);

    VirtualExtendManyToMany many2 = new VirtualExtendManyToMany();
    many2.setData("Roland");
    db.save(many2);

    BeanType<VirtualBase> bt = db.pluginApi().beanType(VirtualBase.class);
    BeanProperty prop = (BeanProperty) bt.property("virtualExtendManyToManys");
    List<VirtualExtendManyToMany> list = (List<VirtualExtendManyToMany>) prop.pathGet(base1);
    assertThat(list).isEmpty();
    list.add(many1);
    LoggedSql.start();
    db.save(base1);
    List<String> sql = LoggedSql.stop();
    assertThat(sql).hasSize(3);
    assertThat(sql.get(0)).contains("insert into kreuztabelle (virtual_base_id, virtual_extend_many_to_many_id) values (?, ?)");
    assertThat(sql.get(1)).contains("-- bind");
    assertThat(sql.get(2)).contains("-- executeBatch() size:1 sql:insert into kreuztabelle (virtual_base_id, virtual_extend_many_to_many_id) values (?, ?)");

    many2.getBases().add(base1);
    LoggedSql.start();
    db.save(many2);
    sql = LoggedSql.stop();
    assertThat(sql).hasSize(3);


    LoggedSql.start();
    VirtualBase found = db.find(VirtualBase.class, base1.getId());
    list = (List<VirtualExtendManyToMany>) prop.pathGet(found);
    assertThat(list).hasSize(2).containsExactlyInAnyOrder(many1, many2);
    sql = LoggedSql.stop();
    assertThat(sql).hasSize(2);
//    assertThat(sql.get(0)).contains("select t0.id, t0.data, concat('Your name is ', t0.data), t1.id from virtual_base t0 left join virtual_extend_one t1 on t1.id = t0.id where t0.id = ?");
    assertThat(sql.get(1)).startsWith("select int_.virtual_base_id, t0.id, t0.data from virtual_extend_many_to_many t0 left join kreuztabelle int_ on int_.virtual_extend_many_to_many_id = t0.id where (int_.virtual_base_id) ");
    DB.find(VirtualBase.class).delete();
    DB.find(VirtualExtendManyToMany.class).delete();
  }

  @Test
  void testCreateDelete() {

    VirtualBase base = new VirtualBase();
    base.setData("Master");
    db.save(base);

    VirtualExtendOne extendOne = new VirtualExtendOne();
    extendOne.setBase(base);
    extendOne.setData("Extended");
    db.save(extendOne);

    VirtualBase found = db.find(VirtualBase.class, base.getId());

    LoggedSql.start();
    db.delete(found);
    List<String> sql = LoggedSql.stop();

    assertThat(sql).hasSize(3);
    assertThat(sql.get(0)).contains("delete from virtual_extend_one where id = ?"); // delete OneToOne - why 'id=?' and not 'id = ?'
    assertThat(sql.get(1)).contains("delete from kreuztabelle where virtual_base_id = ?"); // intersection table
    assertThat(sql.get(2)).contains("delete from virtual_base where id=?"); // delete entity itself

  }

  @Test
  void testInheritance() {

    VirtualBaseA base = new VirtualBaseA();
    base.setData("Master");
    Extension1.get(base).setExt("ext");
    //db.save(base);

    Extension4.get(base).setExtA("extA");
    db.save(base);

    LoggedSql.start();
    VirtualBaseA found = db.find(VirtualBaseA.class).where().eq("extA", "extA").findOne();
    List<String> sql = LoggedSql.stop();

    assertThat(sql).hasSize(1);
    assertThat(sql.get(0)).contains("select t0.kind, t0.id, t0.data, t0.num, t0.ext, t0.ext_a, t0.nums from virtual_base_inherit t0 where t0.kind = 'A' and t0.ext_a = ?");
  }
}
