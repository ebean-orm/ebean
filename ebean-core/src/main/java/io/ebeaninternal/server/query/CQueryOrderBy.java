package io.ebeaninternal.server.query;

import io.ebean.OrderBy;
import io.ebean.OrderBy.Property;
import io.ebeaninternal.server.deploy.BeanDescriptor;
import io.ebeaninternal.server.deploy.BeanProperty;
import io.ebeaninternal.server.deploy.BeanPropertyAssoc;
import io.ebeaninternal.server.deploy.DeployParser;
import io.ebeaninternal.server.deploy.id.IdBinder;
import io.ebeaninternal.server.el.ElPropertyValue;

import java.util.List;

/**
 * Creates the order by expression clause.
 */
final class CQueryOrderBy {

  private final BeanDescriptor<?> desc;
  private final OrderBy<?> orderBy;
  private final DeployParser parser;

  /**
   * Create the logical order by clause.
   */
  static String parse(DeployParser parser, BeanDescriptor<?> desc, OrderBy<?> orderBy) {
    return new CQueryOrderBy(parser, desc, orderBy).parseInternal();
  }

  private CQueryOrderBy(DeployParser parser, BeanDescriptor<?> desc, OrderBy<?> orderBy) {
    this.desc = desc;
    this.parser = parser;
    this.orderBy = orderBy;
  }

  private String parseInternal() {
    List<Property> properties = orderBy.getProperties();
    if (properties.isEmpty()) {
      // order by clause removed by filterMany()
      return null;
    }
    var append = new StringAppend(parser);
    for (int i = 0; i < properties.size(); i++) {
      if (i > 0) {
        append.append(", ");
      }
      parseProperty(properties.get(i), append);
    }
    return append.toString();
  }

  private void parseProperty(Property p, StringAppend append) {
    ElPropertyValue el = desc.elGetValue(p.getProperty());
    if (el != null) {
      BeanProperty beanProperty = el.beanProperty();
      if (beanProperty instanceof BeanPropertyAssoc<?>) {
        BeanPropertyAssoc<?> ap = (BeanPropertyAssoc<?>) beanProperty;
        IdBinder idBinder = ap.targetDescriptor().idBinder();
        append.parse(idBinder.orderBy(el.elName(), p.isAscending()));
        return;
      }
    }
    p.toStringFormat(append);
  }

  private static class StringAppend implements OrderBy.Append {

    private final StringBuilder builder = new StringBuilder();
    private final DeployParser parser;

    StringAppend(DeployParser parser) {
      this.parser = parser;
    }

    @Override
    public String toString() {
      return builder.toString();
    }

    @Override
    public OrderBy.Append property(String property) {
      builder.append(parser.property(property));
      return this;
    }

    @Override
    public OrderBy.Append append(String literal) {
      builder.append(literal);
      return this;
    }

    @Override
    public OrderBy.Append parse(String raw) {
      builder.append(parser.parse(raw));
      return this;
    }
  }
}
