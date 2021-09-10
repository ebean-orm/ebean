package org.multitenant.partition;

import io.ebean.BaseTestCase;
import io.ebean.Database;
import io.ebean.DatabaseFactory;
import io.ebean.config.DatabaseConfig;
import io.ebean.config.TenantMode;
import io.ebean.test.LoggedSql;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class MultiTenantPartitionTest extends BaseTestCase {

  private static final String[] names = {"Ace", "Base", "Case", "Dae", "Eva"};

  static List<MtTenant> tenants() {
    List<MtTenant> tenants = new ArrayList<>();
    for (int i = 0; i < 5; i++) {
      tenants.add(new MtTenant("ten_"+i, names[i], names[i]+"@foo.com".toLowerCase()));
    }
    return tenants;
  }

  private static final Database server = init();
  static {
    server.saveAll(tenants());
  }

  @AfterAll
  public static void shutdown() {
    server.shutdown();
  }

  @Test
  public void start() {

    UserContext.set("rob", "ten_1");

    LoggedSql.start();
    MtContent content = new MtContent("first title");
    server.save(content);

    List<String> sql = LoggedSql.collect();
    assertSql(sql.get(0)).contains("insert into mt_content (title, body, when_modified, when_created, version, tenant_id) values (?,?,?,?,?,?)");

    content.setBody("some body");
    server.save(content);

    sql = LoggedSql.collect();
    assertSql(sql.get(0)).contains("update mt_content set body=?, when_modified=?, version=? where id=? and tenant_id=? and version=?");

    server.delete(content);

    sql = LoggedSql.collect();
    assertSql(sql.get(0)).contains("delete from mt_content where id=? and tenant_id=? and version=?");

    LoggedSql.stop();
  }

  @Test
  public void deleteById() {

    UserContext.set("fred", "ten_2");

    MtContent content = new MtContent("first title");
    server.save(content);

    LoggedSql.start();
    int rows = server.delete(MtContent.class, content.getId());

    List<String> sql = LoggedSql.stop();
    assertSql(sql.get(0)).contains("delete from mt_content where id=? and tenant_id=?");
    assertThat(rows).isEqualTo(1);

    rows = server.delete(MtContent.class, 99999);
    assertThat(rows).isEqualTo(0);
  }

  @Test
  public void deleteByIds() {

    UserContext.set("fred", "ten_2");

    MtContent a = newContent("title a");
    MtContent b = newContent("title b");


    List<Long> ids = Arrays.asList(a.getId(), b.getId(), 99998L);

    LoggedSql.start();
    int rows = server.deleteAll(MtContent.class, ids);
    assertThat(rows).isEqualTo(2);

    List<String> sql = LoggedSql.stop();
    assertSql(sql.get(0)).contains("delete from mt_content where id=? and tenant_id=?");
  }

  private MtContent newContent(String title) {
    MtContent content = new MtContent(title);
    server.save(content);
    return content;
  }


  private static Database init() {

    DatabaseConfig config = new DatabaseConfig();

    config.setName("h2multitenant");
    config.loadFromProperties();
    config.setDdlGenerate(true);
    config.setDdlRun(true);
    config.setDdlExtra(false);
    config.setRegister(false);
    config.setDefaultServer(false);
    config.setCurrentTenantProvider(new CurrentTenant());
    config.setTenantMode(TenantMode.PARTITION);

    config.getClasses().add(MtTenant.class);
    config.getClasses().add(MtContent.class);

    return DatabaseFactory.create(config);
  }
}
