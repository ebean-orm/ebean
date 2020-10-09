package io.ebeanservice.docstore.none;

import io.ebean.docstore.DocUpdateContext;
import io.ebeaninternal.server.core.PersistRequestBean;
import io.ebeaninternal.server.deploy.BeanDescriptor;
import io.ebeaninternal.server.deploy.meta.DeployBeanDescriptor;
import io.ebeanservice.docstore.api.DocStoreUpdateContext;
import io.ebeanservice.docstore.api.support.DocStoreBeanBaseAdapter;

import java.io.IOException;

/**
 * DocStoreBeanBaseAdapter that barfs if it is used.
 */
public class NoneDocStoreBeanAdapter<T> extends DocStoreBeanBaseAdapter<T> {

  public NoneDocStoreBeanAdapter(BeanDescriptor<T> desc, DeployBeanDescriptor<T> deploy) {
    super(desc, deploy);
  }

  @Override
  public boolean isMapped() {
    return false;
  }

  @Override
  public void deleteById(Object idValue, DocUpdateContext txn) throws IOException {
    throw NoneDocStore.implementationNotInClassPath();
  }

  @Override
  public void index(Object idValue, T entityBean, DocUpdateContext txn) throws IOException {
    throw NoneDocStore.implementationNotInClassPath();
  }

  @Override
  public void insert(Object idValue, PersistRequestBean<T> persistRequest, DocStoreUpdateContext txn) throws IOException {
    throw NoneDocStore.implementationNotInClassPath();
  }

  @Override
  public void update(Object idValue, PersistRequestBean<T> persistRequest, DocStoreUpdateContext txn) throws IOException {
    throw NoneDocStore.implementationNotInClassPath();
  }

  @Override
  public void updateEmbedded(Object idValue, String embeddedProperty, String embeddedRawContent, DocUpdateContext txn) throws IOException {
    throw NoneDocStore.implementationNotInClassPath();
  }
}
