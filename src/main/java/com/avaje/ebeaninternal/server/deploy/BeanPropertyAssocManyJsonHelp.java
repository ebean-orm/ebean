package com.avaje.ebeaninternal.server.deploy;

import com.avaje.ebean.bean.BeanCollection;
import com.avaje.ebean.bean.BeanCollectionAdd;
import com.avaje.ebean.bean.EntityBean;
import com.avaje.ebeaninternal.api.ClassUtil;
import com.avaje.ebeaninternal.server.text.json.ReadJson;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import java.io.IOException;

/**
 * Help BeanPropertyAssocMany with JSON processing.
 */
public class BeanPropertyAssocManyJsonHelp {

  /**
   * The associated many property.
   */
  private final BeanPropertyAssocMany<?> many;

  /**
   * Helper used to read json for transient 'many' properties.
   */
  private final BeanPropertyAssocManyJsonTransient jsonTransient;

  /**
   * Construct for the owning many property.
   */
  public BeanPropertyAssocManyJsonHelp(BeanPropertyAssocMany<?> many) {
    this.many = many;
    this.jsonTransient = new BeanPropertyAssocManyJsonTransient();
  }

  /**
   * Read the JSON for this property.
   */
  public void jsonRead(ReadJson readJson, EntityBean parentBean) throws IOException {
    
    if (!this.many.jsonDeserialize) {
      return;
    }

    JsonParser parser = readJson.getParser();
    JsonToken event = parser.nextToken();
    if (JsonToken.VALUE_NULL == event) {
      return;
    }
    if (JsonToken.START_ARRAY != event) {
      throw new JsonParseException("Unexpected token " + event + " - expecting start_array ", parser.getCurrentLocation());
    }

    if (many.isTransient()) {
      jsonReadTransientUsingObjectMapper(readJson, parentBean);
      return;
    }

    BeanCollection<?> collection = many.createEmpty(parentBean);
    BeanCollectionAdd add = many.getBeanCollectionAdd(collection, null);
    do {
      EntityBean detailBean = (EntityBean) many.targetDescriptor.jsonRead(readJson, many.name);
      if (detailBean == null) {
        // read the entire array
        break;
      }
      add.addEntityBean(detailBean);

      if (parentBean != null && many.childMasterProperty != null) {
        // bind detail bean back to master via mappedBy property
        many.childMasterProperty.setValue(detailBean, parentBean);
      }
    } while (true);

    many.setValue(parentBean, collection);
  }

  /**
   * Read a Transient property using Jackson ObjectMapper.
   */
  private void jsonReadTransientUsingObjectMapper(ReadJson readJson, EntityBean parentBean) throws IOException {

    jsonTransient.jsonReadUsingObjectMapper(many, readJson, parentBean);
  }
}
