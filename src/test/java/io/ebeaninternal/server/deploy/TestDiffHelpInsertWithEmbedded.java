package io.ebeaninternal.server.deploy;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.EbeanServer;
import io.ebean.bean.EntityBean;
import io.ebeaninternal.api.SpiEbeanServer;
import io.ebeaninternal.api.json.SpiJsonWriter;
import org.junit.Test;
import org.tests.model.embedded.EMain;
import org.tests.model.embedded.Eembeddable;

import java.io.IOException;
import java.io.StringWriter;

import static org.assertj.core.api.Assertions.assertThat;

public class TestDiffHelpInsertWithEmbedded extends BaseTestCase {

  private EbeanServer server;

  private BeanDescriptor<EMain> emainDesc;

  public TestDiffHelpInsertWithEmbedded() {
    server = Ebean.getServer(null);
    SpiEbeanServer spiServer = (SpiEbeanServer) server;
    emainDesc = spiServer.getBeanDescriptor(EMain.class);
  }

  @Test
  public void simple() throws IOException {

    EMain emain1 = createEMain();

    String asJson = asInsertJson((EntityBean) emain1);

    assertThat(asJson).contains("{\"name\":\"foo\",\"version\":13,\"embeddable\":{\"description\":\"bar\"}}");

  }

  private String asInsertJson(EntityBean emain1) throws IOException {
    StringWriter buffer = new StringWriter();

    SpiJsonWriter jsonWriter = spiEbeanServer().jsonExtended().createJsonWriter(buffer);

    emainDesc.jsonWriteForInsert(jsonWriter, emain1);
    jsonWriter.flush();

    return buffer.toString();
  }

  @Test
  public void scalarPropertyAsNull() throws IOException {

    EMain emain1 = createEMain();
    emain1.setName(null);

    String asJson = asInsertJson((EntityBean) emain1);

    assertThat(asJson).contains("{\"version\":13,\"embeddable\":{\"description\":\"bar\"}}");
  }

  @Test
  public void embeddedAsNull() throws IOException {

    EMain emain1 = createEMain();
    emain1.setEmbeddable(null);

    String asJson = asInsertJson((EntityBean) emain1);

    assertThat(asJson).contains("{\"name\":\"foo\",\"version\":13}");
  }


  @Test
  public void embeddedPropertiesAsNull() throws IOException {

    EMain emain1 = createEMain();
    emain1.getEmbeddable().setDescription(null);

    String asJson = asInsertJson((EntityBean) emain1);

    assertThat(asJson).contains("{\"name\":\"foo\",\"version\":13,\"embeddable\":{}}");
  }

  private EMain createEMain() {

    EMain emain = new EMain();
    emain.setName("foo");
    emain.setVersion(13l);

    Eembeddable embeddable = new Eembeddable();
    embeddable.setDescription("bar");
    emain.setEmbeddable(embeddable);

    return emain;
  }

}
