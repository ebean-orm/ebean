package io.ebean.event;

import io.ebean.BaseTestCase;
import io.ebean.EbeanServer;
import io.ebean.EbeanServerFactory;
import io.ebean.bean.BeanCollection;
import io.ebean.common.BeanList;
import io.ebean.config.ServerConfig;
import org.junit.Test;
import org.tests.example.ModUuidGenerator;
import org.tests.model.basic.EBasic;
import org.tests.model.basic.ECustomId;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class BeanFindControllerTest extends BaseTestCase {

  @Test
  public void test() {

    System.setProperty("ebean.ignoreExtraDdl", "true");

    ServerConfig config = new ServerConfig();

    config.setName("h2otherfind");
    config.loadFromProperties();
    config.setDdlGenerate(true);
    config.setDdlRun(true);
    config.setRegister(false);
    config.setDefaultServer(false);
    config.add(new ModUuidGenerator());
    config.getClasses().add(EBasic.class);
    config.getClasses().add(ECustomId.class);

    EBasicFindController findController = new EBasicFindController();
    config.getFindControllers().add(findController);

    EbeanServer ebeanServer = EbeanServerFactory.create(config);

    System.clearProperty("ebean.ignoreExtraDdl");

    assertFalse(findController.calledInterceptFind);
    ebeanServer.find(EBasic.class, 42);
    assertTrue(findController.calledInterceptFind);

    findController.findIntercept = true;
    EBasic eBasic = ebeanServer.find(EBasic.class, 42);

    assertEquals(Integer.valueOf(47), eBasic.getId());
    assertEquals("47", eBasic.getName());

    assertFalse(findController.calledInterceptFindMany);

    List<EBasic> list = ebeanServer.find(EBasic.class).where().eq("name", "AnInvalidNameSoEmpty").findList();
    assertEquals(0, list.size());
    assertTrue(findController.calledInterceptFindMany);

    findController.findManyIntercept = true;

    list = ebeanServer.find(EBasic.class).where().eq("name", "AnInvalidNameSoEmpty").findList();
    assertEquals(1, list.size());

    eBasic = list.get(0);
    assertEquals(Integer.valueOf(47), eBasic.getId());
    assertEquals("47", eBasic.getName());

    ECustomId bean = new ECustomId("check");
    ebeanServer.save(bean);
    assertNotNull(bean.getId());
  }

  static class EBasicFindController implements BeanFindController {

    boolean findIntercept;
    boolean findManyIntercept;
    boolean calledInterceptFind;
    boolean calledInterceptFindMany;

    @Override
    public boolean isRegisterFor(Class<?> cls) {
      return EBasic.class.equals(cls);
    }

    @Override
    public boolean isInterceptFind(BeanQueryRequest<?> request) {
      calledInterceptFind = true;
      return findIntercept;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T find(BeanQueryRequest<T> request) {
      return (T) createBean();
    }

    @Override
    public boolean isInterceptFindMany(BeanQueryRequest<?> request) {
      calledInterceptFindMany = true;
      return findManyIntercept;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> BeanCollection<T> findMany(BeanQueryRequest<T> request) {

      BeanList<T> list = new BeanList<>();
      list.add((T) createBean());
      return list;
    }
  }

  private static EBasic createBean() {
    EBasic b = new EBasic();
    b.setId(47);
    b.setName("47");
    return b;
  }
}
