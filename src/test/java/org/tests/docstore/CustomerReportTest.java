package org.tests.docstore;


import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;

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
import com.fasterxml.jackson.databind.util.ClassUtil;

import io.ebean.BaseTestCase;
import io.ebean.text.json.JsonReadOptions;

public class CustomerReportTest extends BaseTestCase {



  @Test
  public void testToJson() throws Exception {
    ResetBasicData.reset();



    String json = server().json().toJson(getCustomerReport());


    assertThat(json).isEqualTo("{\"dtype\":\"CR\",\"friends\":[{\"id\":2},{\"id\":3}],\"customer\":{\"id\":1}}");
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
  }

  private CustomerReport getCustomerReport() {
    Customer customer = server().getReference(Customer.class, 1);
    Customer friend1 = server().getReference(Customer.class, 2);
    Customer friend2 = server().getReference(Customer.class, 3);

    CustomerReport report = new CustomerReport();

    report.setCustomer(customer);
    report.setFriends(Arrays.asList(friend1, friend2));
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
