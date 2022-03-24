package org.tests.model.json;

import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
import io.ebean.test.LoggedSql;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TestJacksonPlainBean extends BaseTestCase {

  @Test
  public void insertNullStayNull() {

    // insert with jackson beans as null
    EBasicPlain bean = new EBasicPlain();
    bean.setAttr("n0");
    DB.save(bean);

    LoggedSql.start();
    bean.setAttr("n1");
    DB.save(bean);
    expectedSql(0, "update ebasic_plain set attr=?, version=? where id=? and version=?");

    bean.setPlainBean(new PlainBean("x", 1));
    DB.save(bean);
    expectedSql(0, "update ebasic_plain set plain_bean=?, version=? where id=? and version=?");

    final EBasicPlain found = DB.find(EBasicPlain.class, bean.getId());
    found.setAttr("n2");
    DB.save(found);
    if (!isMySql()) {
      expectedSql(1, "update ebasic_plain set attr=?, version=? where id=? and version=?");
    }
    LoggedSql.stop();
  }

  @Test
  public void insertUpdate() {

    DB.getDefault();
    LoggedSql.start();

    PlainBean content = new PlainBean("foo", 42);
    EBasicPlain bean = new EBasicPlain();
    bean.setAttr("attr0");
    bean.setPlainBean(content);
    bean.setPlainBean2(new PlainBean("bar", 27));

    DB.save(bean);
    if (isSqlServer()) {
      expectedSql(0, "insert into ebasic_plain (id, attr, plain_bean, plain_bean2, version) values (?,?,?,?,?)");
    } else {
      expectedSql(0, "insert into ebasic_plain (attr, plain_bean, plain_bean2, version) values (?,?,?,?)");
    }

    // inserted plainBean has not been mutated
    bean.setAttr("attr1");
    DB.save(bean);
    expectedSql(0, "update ebasic_plain set attr=?, version=? where id=? and version=?");

    // inserted plainBean has now been mutated
    content.setName("notFoo");
    bean.setAttr("attr2");
    DB.save(bean);
    expectedSql(0, "update ebasic_plain set attr=?, plain_bean=?, version=? where id=? and version=?");


    final EBasicPlain found = DB.find(EBasicPlain.class, bean.getId());

    // update mutating PlainBean only
    final PlainBean plainBean = found.getPlainBean();
    plainBean.setName("mod1");
    DB.save(found);
    expectedSql(1, "update ebasic_plain set plain_bean=?, version=? where id=? and version=?");

    // dirtyDetection = false, so not included in update
    found.getPlainBean2().setName("Modification Ignored");
    // dirtyDetection = true, mutation detected
    plainBean.setName("mod2");
    DB.save(found);
    expectedSql(0, "update ebasic_plain set plain_bean=?, version=? where id=? and version=?");

    // update bean, not mutating PlainBean
    found.setAttr("attr3");
    DB.save(found);
    expectedSql(0, "update ebasic_plain set attr=?, version=? where id=? and version=?");

    // dirtyDetection = false, set a new plainBean2 instance, included in update
    found.setPlainBean2(new PlainBean("bar", 27));
    DB.save(found);
    expectedSql(0, "update ebasic_plain set plain_bean2=?, version=? where id=? and version=?");

    LoggedSql.stop();
  }

  private void expectedSql(int i, String s) {
    assertThat(LoggedSql.collect().get(i)).contains(s);
  }

}
