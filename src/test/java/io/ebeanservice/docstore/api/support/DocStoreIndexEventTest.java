package io.ebeanservice.docstore.api.support;

import io.ebean.DocStoreQueueEntry;
import io.ebean.Ebean;
import io.ebean.EbeanServer;
import io.ebean.plugin.BeanDocType;
import io.ebean.plugin.BeanType;
import io.ebeanservice.docstore.api.DocStoreUpdates;
import org.tests.model.basic.Order;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class DocStoreIndexEventTest {

  static EbeanServer server = Ebean.getDefaultServer();

  <T> BeanType<T> beanType(Class<T> cls) {
    return server.getPluginApi().getBeanType(cls);
  }

  BeanType<Order> orderType() {
    return beanType(Order.class);
  }

  @Test
  @SuppressWarnings("unchecked")
  public void docStoreUpdate() throws Exception {

    BeanType<Order> mock = (BeanType<Order>) Mockito.mock(BeanType.class);
    BeanDocType<Order> mockDocType = (BeanDocType<Order>) Mockito.mock(BeanDocType.class);
    when(mock.docStore()).thenReturn(mockDocType);

    Order bean = new Order();
    DocStoreIndexEvent<Order> event = new DocStoreIndexEvent<>(mock, 42, bean);

    event.docStoreUpdate(null);

    verify(mock, times(1)).docStore();
    verify(mockDocType, times(1)).index(42, bean, null);
  }

  @Test
  public void addToQueue() throws Exception {

    Order bean = new Order();

    DocStoreIndexEvent<Order> event = new DocStoreIndexEvent<>(orderType(), 42, bean);

    DocStoreUpdates updates = new DocStoreUpdates();
    event.addToQueue(updates);

    List<DocStoreQueueEntry> queueEntries = updates.getQueueEntries();
    assertThat(queueEntries).hasSize(1);

    DocStoreQueueEntry entry = queueEntries.get(0);
    assertThat(entry.getBeanId()).isEqualTo(42);
    assertThat(entry.getQueueId()).isEqualTo("order");
    assertThat(entry.getPath()).isNull();
    assertThat(entry.getType()).isEqualTo(DocStoreQueueEntry.Action.INDEX);
  }
}
