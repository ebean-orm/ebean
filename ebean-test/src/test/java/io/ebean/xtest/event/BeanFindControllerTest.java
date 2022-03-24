package io.ebean.xtest.event;

import io.ebean.xtest.BaseTestCase;
import io.ebean.Database;
import io.ebean.DatabaseFactory;
import io.ebean.bean.BeanCollection;
import io.ebean.common.BeanList;
import io.ebean.config.DatabaseConfig;
import io.ebean.event.BeanFindController;
import io.ebean.event.BeanQueryRequest;
import org.junit.jupiter.api.Test;
import org.tests.example.ModUuidGenerator;
import org.tests.model.basic.EBasic;
import org.tests.model.basic.ECustomId;
import org.tests.model.controller.FindControllerMain;
import org.tests.model.controller.SoftRefA;
import org.tests.model.controller.SoftRefB;
import org.tests.model.controller.TestBeanFindController;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public class BeanFindControllerTest extends BaseTestCase {

  @Test
  public void test() {

    DatabaseConfig config = new DatabaseConfig();

    config.setName("h2otherfind");
    config.loadFromProperties();
    config.setDdlGenerate(true);
    config.setDdlRun(true);
    config.setDdlExtra(false);

    config.setRegister(false);
    config.setDefaultServer(false);
    config.add(new ModUuidGenerator());
    config.getClasses().add(EBasic.class);
    config.getClasses().add(ECustomId.class);

    EBasicFindController findController = new EBasicFindController();
    config.getFindControllers().add(findController);

    Database db = DatabaseFactory.create(config);

    assertFalse(findController.calledInterceptFind);
    db.find(EBasic.class, 42);
    assertTrue(findController.calledInterceptFind);

    findController.findIntercept = true;
    EBasic eBasic = db.find(EBasic.class, 42);

    assertEquals(Integer.valueOf(47), eBasic.getId());
    assertEquals("47", eBasic.getName());

    assertFalse(findController.calledInterceptFindMany);

    List<EBasic> list = db.find(EBasic.class).where().eq("name", "AnInvalidNameSoEmpty").findList();
    assertEquals(0, list.size());
    assertTrue(findController.calledInterceptFindMany);

    findController.findManyIntercept = true;

    list = db.find(EBasic.class).where().eq("name", "AnInvalidNameSoEmpty").findList();
    assertEquals(1, list.size());

    eBasic = list.get(0);
    assertEquals(Integer.valueOf(47), eBasic.getId());
    assertEquals("47", eBasic.getName());

    ECustomId bean = new ECustomId("check");
    db.save(bean);
    assertNotNull(bean.getId());
    db.shutdown();
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

  @Test
  public void testPostProcess() {

    Database db = prepareSoftRefs();

    final FindControllerMain controllerDbA = db.find(FindControllerMain.class, 1);
    assertThat(controllerDbA).isNotNull();
    assertThat(controllerDbA.getTarget())
      .isNotNull()
      .isInstanceOf(SoftRefA.class)
      .hasFieldOrPropertyWithValue("title", "softRefA");

    final FindControllerMain controllerDbB = db.find(FindControllerMain.class, 2);
    assertThat(controllerDbB).isNotNull();
    assertThat(controllerDbB.getTarget())
      .isNotNull()
      .isInstanceOf(SoftRefB.class)
      .hasFieldOrPropertyWithValue("title", "softRefB");

  }

  @Test
  public void testPostProcessFindMany() {
    Database db = prepareSoftRefs();

    final List<FindControllerMain> controllers = db.find(FindControllerMain.class).orderById(true).findList();

    assertThat(controllers).hasSize(2);

    final FindControllerMain controllerDbA = controllers.get(0);
    assertThat(controllerDbA.getId()).isEqualTo(1);
    assertThat(controllerDbA.getTarget())
      .isNotNull()
      .isInstanceOf(SoftRefA.class)
      .hasFieldOrPropertyWithValue("title", "softRefA");

    final FindControllerMain controllerDbB = controllers.get(1);
    assertThat(controllerDbB.getId()).isEqualTo(2);
    assertThat(controllerDbB.getTarget())
      .isNotNull()
      .isInstanceOf(SoftRefB.class)
      .hasFieldOrPropertyWithValue("title", "softRefB");

  }

  private Database prepareSoftRefs() {
    DatabaseConfig config = new DatabaseConfig();

    config.setName("h2otherfind");
    config.loadFromProperties();
    config.setDdlGenerate(true);
    config.setDdlRun(true);
    config.setDdlExtra(false);

    config.setRegister(false);
    config.setDefaultServer(false);
    config.add(new ModUuidGenerator());
    config.getClasses().add(FindControllerMain.class);
    config.getClasses().add(SoftRefA.class);
    config.getClasses().add(SoftRefB.class);

    config.getFindControllers().add(new TestBeanFindController());

    Database db = DatabaseFactory.create(config);

    final SoftRefA softRefA = new SoftRefA();
    softRefA.setTitle("softRefA");
    db.save(softRefA);

    final SoftRefB softRefB = new SoftRefB();
    softRefB.setTitle("softRefB");
    db.save(softRefB);

    final FindControllerMain main1 = new FindControllerMain();
    main1.setTargetId(softRefA.getId());
    main1.setTargetTableName("soft_ref_a");
    db.save(main1);

    final FindControllerMain main2 = new FindControllerMain();
    main2.setTargetId(softRefB.getId());
    main2.setTargetTableName("soft_ref_b");
    db.save(main2);

    return db;
  }

}
