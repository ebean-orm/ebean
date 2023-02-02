package io.ebeaninternal.server.core;

import io.ebean.DB;
import io.ebean.meta.MetricReportGenerator;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

public class HtmlMetricTest {


  @Test
  public void testReport() throws IOException {
    MetricReportGenerator generator = DB.getDefault().metaInfo().createReportGenerator();

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    generator.writeReport(baos);

    assertThat(baos.toString(StandardCharsets.UTF_8)).contains("<title>");
  }
}
