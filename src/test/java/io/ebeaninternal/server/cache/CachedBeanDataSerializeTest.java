package io.ebeaninternal.server.cache;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.bean.EntityBean;
import io.ebeaninternal.server.deploy.BeanDescriptor;
import io.ebeaninternal.server.transaction.DefaultPersistenceContext;
import org.tests.model.basic.Customer;
import org.tests.model.basic.ResetBasicData;
import org.tests.model.basic.TBytesOnly;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class CachedBeanDataSerializeTest extends BaseTestCase {

  @Test
  public void write() throws IOException, ClassNotFoundException {

    Map<String, Object> map = new LinkedHashMap<>();
    map.put("name", "rob");
    map.put("some", "thing");
    map.put("whenCreated", "" + System.currentTimeMillis());

    long version = System.currentTimeMillis();
    CachedBeanData write = new CachedBeanData(null, "C", map, version);

    ByteArrayOutputStream os = new ByteArrayOutputStream();
    ObjectOutputStream oos = new ObjectOutputStream(os);

    write.writeExternal(oos);
    oos.flush();
    oos.close();

    byte[] bytes = os.toByteArray();

    ByteArrayInputStream is = new ByteArrayInputStream(bytes);
    ObjectInputStream ois = new ObjectInputStream(is);

    CachedBeanData read = new CachedBeanData();
    read.readExternal(ois);

    assertEquals(read.getVersion(), write.getVersion());
    assertEquals(read.getWhenCreated(), write.getWhenCreated());
    assertEquals(read.getDiscValue(), write.getDiscValue());
    assertEquals(read.getData(), write.getData());
  }


  @Test
  public void fullBean() throws IOException, ClassNotFoundException {

    ResetBasicData.reset();

    List<Customer> customers = Ebean.find(Customer.class)
      .orderBy().asc("id")
      .setMaxRows(1).findList();

    Customer customer = customers.get(0);

    BeanDescriptor<Customer> desc = getBeanDescriptor(Customer.class);
    CachedBeanData extract = CachedBeanDataFromBean.extract(desc, (EntityBean) customer);

    ByteArrayOutputStream os = new ByteArrayOutputStream();
    writeToStream(extract, os);

    byte[] bytes = os.toByteArray();

    CachedBeanData read = readFromStream(bytes);

    assertEquals(read.getData(), extract.getData());

    Customer loadCustomer = new Customer();
    CachedBeanDataToBean.load(desc, (EntityBean) loadCustomer, read, new DefaultPersistenceContext());

    assertEquals(loadCustomer.getVersion(), customer.getVersion());
    assertEquals(loadCustomer.getId(), customer.getId());
    assertEquals(loadCustomer.getName(), customer.getName());
    assertEquals(loadCustomer.getStatus(), customer.getStatus());
  }

  @Test
  public void beanWithByteArray() throws IOException, ClassNotFoundException {

    String stringContent = "ThisIsSome";

    TBytesOnly bean = new TBytesOnly();
    bean.setId(42);
    bean.setContent(stringContent.getBytes("UTF-8"));

    BeanDescriptor<TBytesOnly> desc = getBeanDescriptor(TBytesOnly.class);
    CachedBeanData extract = CachedBeanDataFromBean.extract(desc, (EntityBean) bean);

    ByteArrayOutputStream os = new ByteArrayOutputStream();
    writeToStream(extract, os);
    byte[] bytes = os.toByteArray();

    CachedBeanData read = readFromStream(bytes);
    byte[] extraContent = (byte[]) extract.getData("content");

    assertEquals(stringContent, new String(extraContent));
    assertTrue(Arrays.equals(bean.getContent(), extraContent));

    TBytesOnly loadBean = new TBytesOnly();
    CachedBeanDataToBean.load(desc, (EntityBean) loadBean, read, new DefaultPersistenceContext());

    assertEquals(loadBean.getId(), bean.getId());
    assertTrue(Arrays.equals(loadBean.getContent(), bean.getContent()));
  }

  private CachedBeanData readFromStream(byte[] bytes) throws IOException, ClassNotFoundException {

    ByteArrayInputStream is = new ByteArrayInputStream(bytes);
    ObjectInputStream ois = new ObjectInputStream(is);
    return (CachedBeanData) ois.readObject();
  }

  private void writeToStream(CachedBeanData extract, ByteArrayOutputStream os) throws IOException {

    ObjectOutputStream oos = new ObjectOutputStream(os);
    oos.writeObject(extract);
    oos.flush();
    oos.close();
  }

}
