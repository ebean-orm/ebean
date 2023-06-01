package io.ebeaninternal.server.deploy;

import io.ebean.bean.BeanCollection;
import io.ebean.bean.EntityBean;
import io.ebean.common.BeanList;
import io.ebean.common.BeanSet;
import io.ebeaninternal.api.SpiQuery;
import io.ebeaninternal.server.core.OrmQueryRequest;
import io.ebeaninternal.server.el.ElPropertyValue;
import io.ebeaninternal.server.query.CQueryCollectionAdd;


/**
 * Creates Helpers specific to the type of the property (List Set or Map).
 */
public final class BeanCollectionHelpFactory {

  @SuppressWarnings("rawtypes")
  private static final CQueryCollectionAdd LIST_HELP = new ListAdd();

  @SuppressWarnings("rawtypes")
  private static final CQueryCollectionAdd SET_HELP = new SetAdd();

  /**
   * Create the helper based on the many property.
   */
  public static <T> BeanCollectionHelp<T> create(BeanPropertyAssocMany<T> many) {
    boolean elementCollection = many.isElementCollection();
    switch (many.manyType()) {
      case LIST:
        return elementCollection ? new BeanListHelpElement<>(many) : new BeanListHelp<>(many);
      case SET:
        return elementCollection ? new BeanSetHelpElement<>(many) : new BeanSetHelp<>(many);
      case MAP:
        return elementCollection ? new BeanMapHelpElement<>(many) : new BeanMapHelp<>(many);
      default:
        throw new RuntimeException("Invalid type " + many.manyType());
    }
  }

  @SuppressWarnings("unchecked")
  public static <T> CQueryCollectionAdd<T> create(SpiQuery.Type manyType, OrmQueryRequest<T> request) {
    if (manyType == SpiQuery.Type.LIST) {
      return LIST_HELP;

    } else if (manyType == SpiQuery.Type.SET) {
      return SET_HELP;

    } else if (manyType == SpiQuery.Type.MAP) {
      BeanDescriptor<T> target = request.descriptor();
      ElPropertyValue elProperty = target.elGetValue(request.query().mapKey());
      return new BeanMapQueryHelp<>(elProperty);

    } else {
      return null;
    }
  }

  private static final class ListAdd<T> implements CQueryCollectionAdd<T> {

    @Override
    public BeanCollection<T> createEmptyNoParent() {
      return new BeanList<>();
    }

    @Override
    public void add(BeanCollection<?> collection, EntityBean bean, boolean withCheck) {
      collection.internalAdd(bean);
    }
  }

  private static final class SetAdd<T> implements CQueryCollectionAdd<T> {

    @Override
    public BeanCollection<T> createEmptyNoParent() {
      return new BeanSet<>();
    }

    @Override
    public void add(BeanCollection<?> collection, EntityBean bean, boolean withCheck) {
      collection.internalAdd(bean);
    }
  }

}
