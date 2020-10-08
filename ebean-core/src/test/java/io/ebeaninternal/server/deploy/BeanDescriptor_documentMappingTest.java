package io.ebeaninternal.server.deploy;

import io.ebean.BaseTestCase;
import io.ebeanservice.docstore.api.mapping.DocPropertyAdapter;
import io.ebeanservice.docstore.api.mapping.DocPropertyMapping;
import io.ebeanservice.docstore.api.mapping.DocumentMapping;
import org.tests.model.basic.Order;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class BeanDescriptor_documentMappingTest extends BaseTestCase {


  @Test
  public void docMapping() {

    BeanDescriptor<Order> desc = getBeanDescriptor(Order.class);

    DocumentMapping documentMapping = desc.getDocMapping();

    DocPropertyMapping properties = documentMapping.getProperties();

    assertThat(properties).isNotNull();
  }

  @Test
  public void docMapping_visitor() {

    BeanDescriptor<Order> desc = getBeanDescriptor(Order.class);

    DocumentMapping documentMapping = desc.getDocMapping();

    DocPropertyMapping properties = documentMapping.getProperties();

    assertThat(properties).isNotNull();

    TDVisitor tdVisitor = new TDVisitor();
    documentMapping.visit(tdVisitor);

    assertThat(tdVisitor.sb.toString()).isEqualTo("{status,orderDate,shipDate, object{customer:id,name,}customerName, nested{details: [id,orderQty,shipQty,unitPrice,cretime,updtime,]}cretime,updtime,}");
  }

  class TDVisitor extends DocPropertyAdapter {

    StringBuilder sb = new StringBuilder();

    @Override
    public void visitProperty(DocPropertyMapping property) {
      sb.append(property.getName() + ",");
    }

    @Override
    public void visitBegin() {

      sb.append("{");
    }

    @Override
    public void visitEnd() {
      sb.append("}");
    }

    @Override
    public void visitBeginObject(DocPropertyMapping property) {
      sb.append(" object{" + property.getName() + ":");
    }

    @Override
    public void visitEndObject(DocPropertyMapping property) {
      sb.append("}");
    }

    @Override
    public void visitBeginList(DocPropertyMapping property) {
      sb.append(" nested{" + property.getName() + ": [");
    }

    @Override
    public void visitEndList(DocPropertyMapping property) {
      sb.append("]}");
    }
  }

}
