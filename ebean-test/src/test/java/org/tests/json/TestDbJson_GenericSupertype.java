package org.tests.json;

import io.ebean.DB;
import io.ebean.xtest.BaseTestCase;
import org.junit.jupiter.api.Test;
import org.tests.model.json.EJsonGenericBean;
import org.tests.model.json.StringJacksonType;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test that @DbJson fields in a generic superclass are deserialized to the
 * correct concrete type (not LinkedHashMap) for the concrete subclass.
 * <p>
 * Issue: https://github.com/ebean-orm/ebean/issues/3817
 */
public class TestDbJson_GenericSupertype extends BaseTestCase {

  @Test
  public void insertAndFind_genericSupertypeField() {
    var bean = new EJsonGenericBean();
    bean.setName("generic-test");
    bean.setJsonData(new StringJacksonType("hello"));

    DB.save(bean);

    var found = DB.find(EJsonGenericBean.class, bean.getId());

    assertThat(found).isNotNull();
    assertThat(found.getJsonData()).isInstanceOf(StringJacksonType.class);
    assertThat(found.getJsonData().getValue()).isEqualTo("hello");
  }
}
