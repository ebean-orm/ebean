package org.tests.json;

import io.ebean.DB;
import io.ebean.DataBindException;
import io.ebean.xtest.BaseTestCase;
import org.junit.jupiter.api.Test;
import org.tests.model.json.EBasicJsonList;
import org.tests.model.json.EBasicJsonMap;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

class TestDbJsonLength extends BaseTestCase {


    /**
   * The property 'EBasicJsonMap.content' is annotated with @DbJson(length=5000). So we assume, that we cannot save Json-objects
   * where the serialized form exceed that limit and we would expect an error on save.
   * The length check works for platforms like h2, as H2 uses a 'varchar(5000)'. So it is impossible to save such long jsons,
   * but it won't work for SqlServer, as here 'nvarchar(max)' is used. No validation happens at DB level and you might get very
   * large Json objects in your database. This mostly happens unintentionally (programming error, misconfiguration)
   * So they are in the database and they cannot be accessed by ebean any more, because there are new limits in Jackson:
   * - Max 5 Meg per string in 2.15.0
   * - Max 20 Meg per string in 2.15.1
   * see https://github.com/FasterXML/jackson-core/issues/1014
   */
  @Test
  void testLongString() {
    // s is so big, that it could not be deserialized by jackson
    String s = new String(new char[20_000_001]).replace('\0', 'x');

    EBasicJsonMap bean = new EBasicJsonMap();
    bean.setName("b1");
    bean.setContent(Map.of("string", s));

    assertThatThrownBy(() -> {
      // we expect, that we can NOT save the bean, this is ensured by the bind validator.
      DB.save(bean);
    }).isInstanceOf(DataBindException.class);

  }


  /**
   * Tests the UTF8 validation.
   */
  @Test
  void testUtf8() {

    String s = new String(new char[40]).replace('\0', '€');

    EBasicJsonList bean = new EBasicJsonList();
    bean.setName("b1");
    bean.setTags(List.of(s));

    if (isDb2()) {
      // by default, DB2 uses bytes in varchar, so an '€' symbol needs 3 bytes
      assertThatThrownBy(() -> {
        // we expect, that we can NOT save the bean, this is ensured by the bind validator.
        DB.save(bean);
      }).isInstanceOf(DataBindException.class);
    } else {
      DB.save(bean);
    }


  }



}
