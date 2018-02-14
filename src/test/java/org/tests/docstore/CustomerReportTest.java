package org.tests.docstore;


import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;
import org.tests.model.basic.Customer;
import org.tests.model.basic.Product;
import org.tests.model.basic.ResetBasicData;
import org.tests.model.docstore.CustomerReport;
import org.tests.model.docstore.EbeanJsonDeserializer;
import org.tests.model.docstore.ProductReport;
import org.tests.model.docstore.Report;
import org.tests.model.docstore.ReportComment;
import org.tests.model.docstore.ReportEntity;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.KeyDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.cfg.HandlerInstantiator;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;
import com.fasterxml.jackson.databind.jsontype.TypeResolverBuilder;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.util.ClassUtil;

import io.ebean.BaseTestCase;
import io.ebean.text.json.JsonReadOptions;
import io.ebean.text.json.JsonWriteOptions;

public class CustomerReportTest extends BaseTestCase {

  @Test
  public void testToJson() throws Exception {
    ResetBasicData.reset();

    String json = server().json().toJson(getCustomerReport());
    assertThat(json).isEqualTo("{\"dtype\":\"CR\",\"friends\":[{\"id\":2},{\"id\":3}],\"customer\":{\"id\":1}}");

    JsonWriteOptions opts = new JsonWriteOptions();
    opts.setWriteVersion((writer, desc) -> {
      int version = 0;
      if (Report.class.isAssignableFrom(desc.getBeanType())) {
        version = 3;
      }
      writer.writeNumberField("_bv", version);
    });
    json = server().json().toJson(getCustomerReport(),opts);
    assertThat(json).isEqualTo("{\"_bv\":3,\"dtype\":\"CR\",\"friends\":[{\"_bv\":0,\"id\":2},{\"_bv\":0,\"id\":3}],\"customer\":{\"_bv\":0,\"id\":1}}");
  }

  @Test
  public void testMigration() {
    String json = "{\"_bv\":2,\"dtype\":\"CustomerReport\",\"friends\":[{\"_bv\":0,\"id\":2},{\"_bv\":0,\"id\":3}],\"customer\":{\"_bv\":0,\"id\":1}}";
    JsonReadOptions readOpts = new JsonReadOptions();
    readOpts.setVersionMigrationHandler((reader, desc) -> {
      JsonParser parser = reader.getParser();

      assertThat(parser.nextToken()).isEqualTo(JsonToken.FIELD_NAME);
      assertThat(parser.getCurrentName()).isEqualTo("_bv");
      int version = parser.nextIntValue(-1);
      if (version == 2) {
        parser.nextToken();
        ObjectNode node = reader.getObjectMapper().readTree(parser);
        if ("CustomerReport".equals(node.get("dtype").asText())) {
          node.put("dtype", "CR");
        }
        JsonParser newParser = node.traverse();
        assertThat(newParser.nextToken()).isEqualTo(JsonToken.START_OBJECT);
        return reader.forJson(newParser, false);
      }
      return reader;
    });
    Report report = server().json().toBean(Report.class, json, readOpts);
    assertThat(report).isInstanceOf(CustomerReport.class);
  }

  @Test
  public void testFromJson() throws Exception {
    ResetBasicData.reset();
    String json = "{\"dtype\":\"CR\",\"friends\":[{\"id\":2},{\"id\":3}],\"customer\":{\"id\":1}}";

    JsonReadOptions opts = new JsonReadOptions();
    opts.setEnableLazyLoading(true);
    CustomerReport report = server().json().toBean(CustomerReport.class, json, opts);

    assertThat(report.getCustomer().getName()).isEqualTo("Rob");

    assertThat(report.getFriends().get(0).getName()).isEqualTo("Cust NoAddress");
    assertThat(report.getFriends().get(1).getName()).isEqualTo("Fiona");
  }

  @Test
  public void testEmbeddedDocs() throws Exception {
    ResetBasicData.reset();

    CustomerReport report = getCustomerReport();
    report.getEmbeddedReports().add(getProductReport());

    String json = server().json().toJson(report);

    assertThat(json).isEqualTo("{\"dtype\":\"CR\","
        + "\"embeddedReports\":[{\"dtype\":\"PR\",\"title\":\"This is a good product\",\"product\":{\"id\":1}}],"
        + "\"friends\":[{\"id\":2},{\"id\":3}],"
        + "\"customer\":{\"id\":1}}");

    JsonReadOptions opts = new JsonReadOptions();
    opts.setEnableLazyLoading(true);
    report = server().json().toBean(CustomerReport.class, json, opts);
    ProductReport ar = (ProductReport) report.getEmbeddedReports().get(0);
    assertThat(ar.getTitle()).isEqualTo("This is a good product");
    assertThat(ar.getProduct().getName()).isEqualTo("Chair");
  }

  @Test
  @Ignore
  public void testEntity() throws Exception {
    ObjectMapper mapper = (ObjectMapper) server().getPluginApi().getServerConfig().getObjectMapper();
    mapper.setHandlerInstantiator(new HandlerInstantiator() {

      @Override
      public TypeResolverBuilder<?> typeResolverBuilderInstance(MapperConfig<?> config, Annotated annotated,
          Class<?> builderClass) {
        return null;
      }

      @Override
      public TypeIdResolver typeIdResolverInstance(MapperConfig<?> config, Annotated annotated, Class<?> resolverClass) {
        return null;
      }

      @Override
      public JsonSerializer<?> serializerInstance(SerializationConfig config, Annotated annotated, Class<?> serClass) {
        return null;
      }

      @Override
      public KeyDeserializer keyDeserializerInstance(DeserializationConfig config, Annotated annotated,
          Class<?> keyDeserClass) {
        return null;
      }

      @SuppressWarnings({ "rawtypes", "unchecked" })
      @Override
      public JsonDeserializer<?> deserializerInstance(DeserializationConfig config, Annotated annotated,
          Class<?> deserClass) {
        JsonDeserializer<?> ser = (JsonDeserializer<?>) ClassUtil.createInstance(deserClass, config.canOverrideAccessModifiers());
        if (ser instanceof EbeanJsonDeserializer) {
          ((EbeanJsonDeserializer)ser).setBeanType(annotated.getType().getRawClass());
        }
        return ser;
      }
    });
    ResetBasicData.reset();

    CustomerReport report = getCustomerReport();
    report.getEmbeddedReports().add(getProductReport());
    ReportComment comment1 = new ReportComment();
    comment1.setComment("First comment of customer 1");
    comment1.setAuthor(server().getReference(Customer.class, 1));

    ReportComment comment2 = new ReportComment();
    comment2.setComment("First comment of customer 2");
    comment2.setAuthor(server().getReference(Customer.class, 2));

    report.getComments().add(comment1);
    report.getComments().add(comment2);


    ReportEntity entity = new ReportEntity();
    entity.setReport(report);

    comment1 = report.getComments().get(0);
    comment2 = report.getComments().get(1);
    assertThat(report.getParentBean()).isSameAs(entity);
    assertThat(comment1.getParentBean()).isSameAs(report);
    assertThat(comment2.getParentBean()).isSameAs(report);


    String json = server().json().toJson(entity);
    System.out.println("JSON:" + json);
    assertThat(entity.getVersion()).isNull();
    server().save(entity);
    assertThat(entity.getVersion()).isEqualTo(1);
    server().save(entity);
    assertThat(entity.getVersion()).isEqualTo(1);

    entity.getReport().setTitle("blubbblubb");
    server().save(entity);
    assertThat(entity.getVersion()).isEqualTo(2);

    entity = server().find(ReportEntity.class, entity.getId());
    Report report2 = entity.getReport();
    assertThat(report2.getTitle()).isEqualTo("blubbblubb");

    comment1 = report2.getComments().get(0);
    comment2 = report2.getComments().get(1);
    assertThat(report2.getParentBean()).isSameAs(entity);
    assertThat(comment1.getParentBean()).isSameAs(report2);
    assertThat(comment2.getParentBean()).isSameAs(report2);

    // Test dbjson list reports
    Report report3 = new CustomerReport();
    report3.setTitle("report3");

    ReportEntity entity2 = new ReportEntity();
    entity2.getReports().add(report3);

    server().save(entity2);
    entity2 = server().find(ReportEntity.class, entity2.getId());
    server().refresh(entity2);

    List<Report> reportList = entity2.getReports();
    assertEquals(1, reportList.size());
    assertEquals("report3", reportList.get(0).getTitle());

    reportList.get(0).setTitle("changed title");
    server().save(entity2);

    entity2 = server().find(ReportEntity.class, entity2.getId());
    server().refresh(entity2);

    assertEquals("changed title", entity2.getReports().get(0).getTitle());

    assertEquals("reports", entity2.getReports().get(0).getPropertyName());
    assertEquals(0, entity2.getReports().get(0).getAdditionalKey());

    // Test dbjson map reportMap
    Report report4 = new CustomerReport();
    report4.setTitle("report4");

    ReportEntity entity3 = new ReportEntity();
    entity3.getReportMap().put("first", report4);

    server().save(entity3);
    entity3 = server().find(ReportEntity.class, entity3.getId());
    server().refresh(entity3);

    Map<String, Report> reportMap = entity3.getReportMap();
    assertEquals(1, reportMap.size());
    assertEquals("report4", reportMap.get("first").getTitle());

    reportMap.get("first").setTitle("changed title");
    server().save(entity3);

    entity3 = server().find(ReportEntity.class, entity3.getId());
    server().refresh(entity3);

    Report firstReport = entity3.getReportMap().get("first");
    assertEquals("changed title", firstReport.getTitle());

    assertEquals("reportMap", firstReport.getPropertyName());
    assertEquals("first", firstReport.getAdditionalKey());
  }

  private CustomerReport getCustomerReport() {
    Customer customer = server().getReference(Customer.class, 1);
    Customer friend1 = server().getReference(Customer.class, 2);
    Customer friend2 = server().getReference(Customer.class, 3);

    CustomerReport report = new CustomerReport();

    report.setCustomer(customer);
    report.getFriends().addAll(Arrays.asList(friend1, friend2));
    return report;
  }

  private ProductReport getProductReport() {
    Product product = server().getReference(Product.class, 1);

    ProductReport report = new ProductReport();
    report.setTitle("This is a good product");
    report.setProduct(product);
    return report;
  }
}
