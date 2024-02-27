package org.tests.json;

import io.ebean.DB;
import io.ebean.DataIntegrityException;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.tests.model.json.EBasicJsonMap;

import java.util.Map;

class TestDbJsonLength {


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

    SoftAssertions softly = new SoftAssertions();

    softly.assertThatThrownBy(() -> {
      DB.save(bean);
    }).isInstanceOf(DataIntegrityException.class);


    if (bean.getId() != null) {
      // we expect, that we could NOT save the bean, but this is not true for sqlServer.
      // we will get a javax.persistence.PersistenceException: Error loading on org.tests.model.json.EBasicJsonMap.content
      // when we try to load the bean back from DB
      DB.find(EBasicJsonMap.class, bean.getId());
    }

    softly.assertAll();

  }

}
