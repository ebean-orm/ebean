package io.ebeaninternal.server.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.ebean.BeanMergeOptions;
import io.ebean.bean.EntityBean;
import io.ebean.bean.EntityBeanIntercept;
import io.ebean.bean.PersistenceContext;
import io.ebean.text.json.JsonReadBeanVisitor;
import io.ebean.text.json.JsonReadOptions;
import io.ebeaninternal.api.LoadContext;
import io.ebeaninternal.api.json.SpiJsonReader;
import io.ebeaninternal.server.deploy.BeanDescriptor;
import io.ebeaninternal.server.loadcontext.DLoadContext;
import io.ebeaninternal.server.transaction.DefaultPersistenceContext;

import java.io.IOException;
import java.util.Map;

/**
 * Context for JSON read processing.
 */
public final class ReadJson implements SpiJsonReader {

  private final BeanDescriptor<?> rootDesc;
  private final JsonParser parser;
  private final PathStack pathStack;
  /**
   * Map of the JsonReadBeanVisitor keyed by path.
   */
  private final Map<String, JsonReadBeanVisitor<?>> visitorMap;
  private final Object objectMapper;
  private final PersistenceContext persistenceContext;
  private final LoadContext loadContext;
  private final boolean enableLazyLoading;

  /**
   * Construct with parser and readOptions.
   */
  public ReadJson(BeanDescriptor<?> desc, JsonParser parser, JsonReadOptions readOptions, Object objectMapper) {
    this.rootDesc = desc;
    this.parser = parser;
    this.objectMapper = objectMapper;
    this.persistenceContext = initPersistenceContext(readOptions);
    this.loadContext = initLoadContext(desc, readOptions);
    this.enableLazyLoading = readOptions != null && readOptions.isEnableLazyLoading();
    // only create visitorMap, pathStack if needed ...
    this.visitorMap = (readOptions == null) ? null : readOptions.getVisitorMap();
    this.pathStack = (visitorMap == null && loadContext == null) ? null : new PathStack();
  }

  /**
   * Construct when transferring load context, persistence context, object mapper etc to a new ReadJson instance.
   */
  private ReadJson(JsonParser moreJson, ReadJson source) {
    this.parser = moreJson;
    this.rootDesc = source.rootDesc;
    this.pathStack = source.pathStack;
    this.visitorMap = source.visitorMap;
    this.objectMapper = source.objectMapper;
    this.persistenceContext = source.persistenceContext;
    this.loadContext = source.loadContext;
    this.enableLazyLoading = source.enableLazyLoading;
  }

  private LoadContext initLoadContext(BeanDescriptor<?> desc, JsonReadOptions readOptions) {
    if (readOptions == null) return null;
    if (readOptions.isEnableLazyLoading() && readOptions.getLoadContext() == null) {
      return new DLoadContext(desc, persistenceContext);
    } else {
      return (LoadContext) readOptions.getLoadContext();
    }
  }

  private PersistenceContext initPersistenceContext(JsonReadOptions readOptions) {
    if (readOptions != null && readOptions.getPersistenceContext() != null) {
      return readOptions.getPersistenceContext();
    }
    return new DefaultPersistenceContext();
  }

  /**
   * Return the persistence context being used if any.
   */
  @Override
  public PersistenceContext persistenceContext() {
    return persistenceContext;
  }

  /**
   * Return a new instance of ReadJson using the existing context but with a new JsonParser.
   */
  @Override
  public SpiJsonReader forJson(JsonParser moreJson) {
    return new ReadJson(moreJson, this);
  }

  /**
   * Add the bean to the persistence context.
   */
  @Override
  public <T> void persistenceContextPut(Object beanId, T currentBean) {
    persistenceContextPutIfAbsent(beanId, (EntityBean) currentBean, rootDesc);
  }

  /**
   * Put the bean into the persistence context. If there is already a matching bean in the
   * persistence context then return that instance else return null.
   */
  @Override
  public Object persistenceContextPutIfAbsent(Object id, EntityBean bean, BeanDescriptor<?> beanDesc) {
    if (!enableLazyLoading) {
      bean._ebean_getIntercept().setDisableLazyLoad(true);
    }
    if (persistenceContext == null) {
      // no persistenceContext means no lazy loading either
      return null;
    }
    EntityBean existing = beanDesc.contextPutIfAbsent(persistenceContext, id, bean);
    if (existing != null) {
      // we foind a bean in the persistence context AND we have deserialized the same bean
      // so copy every property to the existing bean
      BeanMergeOptions opts = new BeanMergeOptions();
      opts.setPersistenceContext(persistenceContext);
      opts.setMergeVersion(true);
      beanDesc.mergeBeans(bean, existing, opts);
    } else {
      if (loadContext != null) {
        EntityBeanIntercept ebi = bean._ebean_getIntercept();
        if (ebi.isPartial()) {
          // register for further lazy loading
          String path = pathStack.peekWithNull();
          loadContext.register(path, ebi);
          beanDesc.lazyLoadRegister(path, ebi, bean, loadContext);
        }
        ebi.setLoaded();
      }
      return null;
    }
    return existing;
  }

  /**
   * Return the objectMapper used for this request.
   */
  @Override
  public ObjectMapper mapper() {
    if (objectMapper == null) {
      throw new IllegalStateException(
        "Jackson ObjectMapper required but has not set. The ObjectMapper can be set on"
          + " either the DatabaseConfig or on JsonReadOptions.");
    }
    return (ObjectMapper) objectMapper;
  }

  /**
   * Return the JsonParser.
   */
  @Override
  public JsonParser parser() {
    return parser;
  }

  /**
   * Return the next JsonToken from the underlying parser.
   */
  @Override
  public JsonToken nextToken() throws IOException {
    return parser.nextToken();
  }

  /**
   * Push the path onto the stack (traversing a 1-M or M-1 etc)
   */
  @Override
  public void pushPath(String path) {
    if (pathStack != null) {
      pathStack.pushPathKey(path);
    }
  }

  /**
   * Pop the path stack.
   */
  @Override
  public void popPath() {
    if (pathStack != null) {
      pathStack.pop();
    }
  }

  /**
   * If there is a JsonReadBeanVisitor registered to the current path then
   * call it's visit method with the bean and unmappedProperties.
   */
  @Override
  @SuppressWarnings({"unchecked", "rawtypes"})
  public void beanVisitor(Object bean, Map<String, Object> unmappedProperties) {
    if (visitorMap != null) {
      JsonReadBeanVisitor visitor = visitorMap.get(pathStack.peekWithNull());
      if (visitor != null) {
        visitor.visit(bean, unmappedProperties);
      }
    }
  }

  /**
   * Read the property value using Jackson ObjectMapper.
   * <p/>
   * Typically this is used to read Transient properties where the type is unknown to Ebean.
   */
  @Override
  public Object readValueUsingObjectMapper(Class<?> propertyType) throws IOException {
    return mapper().readValue(parser, propertyType);
  }
}
