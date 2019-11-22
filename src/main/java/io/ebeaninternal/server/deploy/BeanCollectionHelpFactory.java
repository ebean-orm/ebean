package io.ebeaninternal.server.deploy;

import io.ebeaninternal.api.SpiQuery;
import io.ebeaninternal.server.core.OrmQueryRequest;
import io.ebeaninternal.server.el.ElPropertyValue;
import io.ebeaninternal.server.query.CQueryCollectionAdd;


/**
 * Creates Helpers specific to the type of the property (List Set or Map).
 */
public class BeanCollectionHelpFactory {

  @SuppressWarnings("rawtypes")
  private static final BeanListHelp LIST_HELP = new BeanListHelp();

  @SuppressWarnings("rawtypes")
  private static final BeanSetHelp SET_HELP = new BeanSetHelp();

  /**
   * Create the helper based on the many property.
   */
  public static <T> BeanCollectionHelp<T> create(BeanPropertyAssocMany<T> many) {

    boolean elementCollection = many.isElementCollection();
    ManyType manyType = many.getManyType();
    switch (manyType) {
      case LIST:
        return elementCollection ? new BeanListHelpElement<>(many) : new BeanListHelp<>(many);
      case SET:
        return elementCollection ? new BeanSetHelpElement<>(many) : new BeanSetHelp<>(many);
      case MAP:
        return elementCollection ? new BeanMapHelpElement<>(many) :new BeanMapHelp<>(many);
      default:
        throw new RuntimeException("Invalid type " + manyType);
    }

  }

  @SuppressWarnings("unchecked")
  public static <T> CQueryCollectionAdd<T> create(SpiQuery.Type manyType, OrmQueryRequest<T> request) {

    if (manyType == SpiQuery.Type.LIST) {
      return LIST_HELP;

    } else if (manyType == SpiQuery.Type.SET) {
      return SET_HELP;

    } else if (manyType == SpiQuery.Type.MAP) {
      BeanDescriptor<T> target = request.getBeanDescriptor();
      ElPropertyValue elProperty = target.getElGetValue(request.getQuery().getMapKey());
      return new BeanMapQueryHelp<>(elProperty);

    } else {
      return null;
    }
  }


}
