package org.tests.json.include;

import io.ebean.DB;
import io.ebean.FetchPath;
import io.ebean.annotation.Transactional;
import io.ebean.config.JsonConfig;
import io.ebean.text.PathProperties;
import io.ebean.text.json.JsonWriteOptions;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Contact;
import org.tests.model.basic.ResetBasicData;

import static org.assertj.core.api.Assertions.assertThat;

public class TestJsonImplicitLoaded {

  @Test
  @Transactional
  public void testToBeanToJson() throws Exception {
    ResetBasicData.reset();


    FetchPath path = PathProperties.parse("*");
    Contact bean = DB.find(Contact.class).setId(1).apply(path).findOne();

    JsonWriteOptions options = new JsonWriteOptions();
    options.setInclude(JsonConfig.Include.NON_NULL);
    options.setPathProperties(path);
    options.setIncludeLoadedImplicit(false);

    String asJson = DB.json().toJson(bean, options);
    assertThat(asJson).contains("customer\":{\"id\":1}"); // hold only ID

    bean.getCustomer().getName(); // lazy-load bean;
    asJson = DB.json().toJson(bean, options);
    assertThat(asJson).contains("customer\":{\"id\":1}"); // expect the same result


  }

}
