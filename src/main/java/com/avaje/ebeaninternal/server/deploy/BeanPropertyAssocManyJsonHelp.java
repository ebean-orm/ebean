package com.avaje.ebeaninternal.server.deploy;

import java.io.IOException;

import com.avaje.ebean.bean.BeanCollection;
import com.avaje.ebean.bean.BeanCollectionAdd;
import com.avaje.ebean.bean.EntityBean;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

public class BeanPropertyAssocManyJsonHelp {

  private final BeanPropertyAssocMany<?> many;

  public BeanPropertyAssocManyJsonHelp(BeanPropertyAssocMany<?> many) {
    this.many = many;
  }

  public void jsonRead(JsonParser parser, EntityBean parentBean) throws IOException {
    
    if (!this.many.jsonDeserialize) {
      return;
    }

    JsonToken event = parser.nextToken();
    if (JsonToken.VALUE_NULL == event) {
      return;
    }
    if (JsonToken.START_ARRAY != event) {
      throw new JsonParseException("Unexpected token " + event + " - expecting start_array ", parser.getCurrentLocation());
    }

    BeanCollection<?> collection = many.createEmpty(parentBean);
    BeanCollectionAdd add = many.getBeanCollectionAdd(collection, null);
    do {
      EntityBean detailBean = (EntityBean) many.targetDescriptor.jsonRead(parser, many.name);
      if (detailBean == null) {
        // read the entire array
        break;
      }
      add.addBean(detailBean);

      if (parentBean != null && many.childMasterProperty != null) {
        // bind detail bean back to master via mappedBy property
        many.childMasterProperty.setValue(detailBean, parentBean);
      }
    } while (true);

    many.setValue(parentBean, collection);
  }
}
